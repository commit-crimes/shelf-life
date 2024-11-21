package com.android.shelfLife.model.recipe

import android.util.Log
import androidx.lifecycle.ViewModel
import coil3.compose.AsyncImagePainter
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class ListRecipesViewModel(private val recipeRepository: RecipeRepository, private val recipeGeneratorRepository: RecipeGeneratorRepository) : ViewModel() {
  private val _recipes =  MutableStateFlow<List<Recipe>>(emptyList())
  val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

  // Selected recipe, i.e the recipe for the detail view
  private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
  open val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

  init {
    recipeRepository.init(
      onSuccess = {
        getRecipes()
      })
  }

  /**
   * Handles a failure in fetching recipes.
   *
   * @param exception The exception that occurred.
   */
  private fun _onFail(exception: Exception) {
    // TODO: proper error Handling (use a global Error PopUp?)
    Log.e("ListRecipesViewModel", "Error fetching Recipes: $exception")
  }

  fun getUID(): String {
    return recipeRepository.getUid()
  }

  fun getRecipes(){
    return recipeRepository.getRecipes(onSuccess = { _recipes.value = it }, onFailure = ::_onFail)
  }

  /**
   * Save a recipe to Firebase Firestore.
   *
   * @param recipe The Recipe to be saved.
   */
  fun saveRecipe(recipe: Recipe) {
    recipeRepository.addRecipe(
      recipe = recipe,
      onSuccess = {
        getRecipes()
      },
      onFailure = ::_onFail
    )
  }


  /**
   * Generate a recipe using the RecipeGeneratorRepository.
   * WARNING!: This function does NOT save the recipe to the repository. Call saveRecipe for that.
   *
   * @param recipePrompt The RecipePrompt to generate the recipe from.
   * @param onSuccess The callback to be called when the recipe is successfully generated.
   */
  fun generateRecipe(recipePrompt: RecipePrompt, onSuccess: (Recipe) -> Unit) {
    recipeGeneratorRepository.generateRecipe(
      recipePrompt = recipePrompt,
      onSuccess = onSuccess, //NOT SAVED DIRECTLY (maybe user wants to regenerate, or scrap the recipe)
      onFailure = ::_onFail
    )
  }

  /**
   * Selects a recipe.
   *
   * @param recipe The Recipe to be selected.
   */
  fun selectRecipe(recipe: Recipe) {
    _selectedRecipe.value = recipe
  }

}
