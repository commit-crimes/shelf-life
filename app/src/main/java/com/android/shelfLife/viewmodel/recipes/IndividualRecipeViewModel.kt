package com.android.shelfLife.viewmodel.recipes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository

class IndividualRecipeViewModel(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

  var selectedRecipe by mutableStateOf<Recipe?>(null)
  var selectedRecipeIsNonEmpty by mutableStateOf<Boolean>(true)

  /**
   * Initializes the ViewModel, setting the selected recipe from the repository. If no recipe is
   * selected, it marks the recipe as empty.
   */
  init {
    selectedRecipe = recipeRepository.selectedRecipe.value
    if (selectedRecipe == null) {
      selectedRecipeIsNonEmpty = false
    }
  }

  fun getRecipeName(): String {
    /**
     * Returns the name of the selected recipe.
     *
     * @return The name of the recipe as a string.
     */
    return selectedRecipe!!.name
  }

  /**
   * Returns the number of servings for the selected recipe.
   *
   * @return The servings amount of the recipe as a Float.
   */
  fun getRecipeServing(): Float {
    return selectedRecipe!!.servings
  }

  /**
   * Returns the cooking time of the selected recipe in minutes.
   *
   * @return The time in minutes as a Long.
   */
  fun getRecipeTime(): Long {
    return selectedRecipe!!.time.inWholeMinutes
  }

  /**
   * Returns a list of ingredients required for the selected recipe.
   *
   * @return A list of [Ingredient] objects.
   */
  fun getRecipeIngredients(): List<Ingredient> {
    return selectedRecipe!!.ingredients
  }

  /**
   * Returns a list of instructions for the selected recipe.
   *
   * @return A list of instructions as strings.
   */
  fun getRecipeInstruction(): List<String> {
    return selectedRecipe!!.instructions
  }
}
