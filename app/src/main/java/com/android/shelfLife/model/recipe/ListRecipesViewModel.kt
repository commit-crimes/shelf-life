package com.android.shelfLife.model.recipe

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class ListRecipesViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeGeneratorRepository: RecipeGeneratorRepository
) : ViewModel() {
  private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
  val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

  // Selected recipe, i.e the recipe for the detail view
  private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
  open val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

  init {
    recipeRepository.init(onSuccess = { getRecipes() })
  }

  /**
   * Handles a failure in fetching recipes.
   *
   * @param exception The exception that occurred.
   */
  private fun _onFail(exception: Exception) {
    // TODO: proper error Handling (use a global Error PopUp?)
    //Log.e("ListRecipesViewModel", "Error fetching Recipes: $exception")
  }

  fun getUID(): String {
    return recipeRepository.getUid()
  }

  fun getRecipes() {
    return recipeRepository.getRecipes(onSuccess = { _recipes.value = it }, onFailure = ::_onFail)
  }

  /**
   * Save a recipe to Firebase Firestore. This function will GIVE THE RECIPE A NEW UID
   *
   * @param recipe The Recipe to be saved.
   */
  fun saveRecipe(recipe: Recipe) {
    val newRecipe = recipe.copy(uid = getUID())
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
