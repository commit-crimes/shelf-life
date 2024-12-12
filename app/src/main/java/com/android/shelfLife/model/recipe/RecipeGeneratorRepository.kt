package com.android.shelfLife.model.recipe

import kotlinx.coroutines.flow.StateFlow


/** Interface describing a model for generating recipes */
interface RecipeGeneratorRepository {
  /**
   * Generates a recipe based on the provided search input.
   *
   * @param listFoodItems The list of food items to use for generating recipes.
   * @param searchRecipeType The type of recipe search to perform (default is USE_SOON_TO_EXPIRE).
   * @return The generated recipe, or null if there is an error.
   */
  suspend fun generateRecipe(
      recipePrompt: RecipePrompt
  ) : Recipe?
}
