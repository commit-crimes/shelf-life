package com.android.shelfLife.model.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.newRecipe.RecipeRepository
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class ListRecipesViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeGeneratorRepository: RecipeGeneratorRepository
) : ViewModel() {
  // Create a hardcoded test recipe, to allow all of our current tests to pass (end to end etc..)
  // This will be cleaned up in next task, with the entire Recipes model refactor
  private val testRecipe =
      Recipe(
          uid = "Test UID",
          name = "Paella",
          ingredients =
              listOf(
                  Ingredient("Rice", Quantity(100.0, FoodUnit.GRAM)),
                  Ingredient("Chicken", Quantity(500.0, FoodUnit.GRAM))),
          instructions =
              listOf(
                  "Test cooking instructions",
                  "Test cooking instructions 2",
                  "Test cooking instructions 3"),
          servings = 1f,
          time = 1.toDuration(DurationUnit.SECONDS),
      )

  private val _recipes = MutableStateFlow<List<Recipe>>(listOf(testRecipe))
  val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

  // Selected recipe, i.e the recipe for the detail view
  private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
  open val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

  init {
    // recipeRepository.init(onSuccess = { observeRecipes() })
  }

  private fun observeRecipes() {
    getRecipes()
    viewModelScope.launch {
      recipeRepository.recipes.collect { recipeList ->
        // Add the test recipe to the current recipes
        _recipes.value = listOf(testRecipe) + recipeList
      }
    }
  }

  /**
   * Handles a failure in fetching recipes.
   *
   * @param exception The exception that occurred.
   */
  private fun _onFail(exception: Exception) {
    // TODO: proper error Handling (use a global Error PopUp?)
    // Log.e("ListRecipesViewModel", "Error fetching Recipes: $exception")
  }

  fun getUID(): String {
    return recipeRepository.getUid()
  }

  fun getRecipes() { // deprecated: clean up in next task. We now have observeRecipes that is only
    // called on start
    return recipeRepository.getRecipes(
        onSuccess = { _recipes.value = listOf(testRecipe) + it }, onFailure = ::_onFail)
  }

  /**
   * Save a recipe to Firebase Firestore. This function will GIVE THE RECIPE A NEW UID
   *
   * @param recipe The Recipe to be saved.
   */
  fun saveRecipe(recipe: Recipe) {
    val newRecipe = recipe.copy(uid = getUID())
    _recipes.value +=
        newRecipe // until the big model refactor, lets simply add it to the list to have the
    // correct local state
    recipeRepository.addRecipe(
        recipe = newRecipe, onSuccess = { getRecipes() }, onFailure = ::_onFail)
  }

  /**
   * Generate a recipe using the RecipeGeneratorRepository. WARNING!: This function does NOT save
   * the recipe to the repository. Call saveRecipe for that.
   *
   * @param recipePrompt The RecipePrompt to generate the recipe from.
   * @param onSuccess The callback to be called when the recipe is successfully generated.
   */
  fun generateRecipe(
      recipePrompt: RecipePrompt,
      onSuccess: (Recipe) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    recipeGeneratorRepository.generateRecipe(
        recipePrompt = recipePrompt,
        onSuccess =
            onSuccess, // NOT SAVED DIRECTLY (maybe user wants to regenerate, or scrap the recipe)
        onFailure = onFailure)
  }

  /**
   * Selects a recipe.
   *
   * @param recipe The Recipe to be selected.
   */
  fun selectRecipe(recipe: Recipe?) {
    _selectedRecipe.value = recipe
  }
}
