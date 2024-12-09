package com.android.shelfLife.model.recipe

/** Interface describing a model for generating recipes */
interface RecipeGeneratorRepository {

  /**
   * Generates a recipe based on the provided search input.
   *
   * @param listFoodItems The list of food items to use for generating recipes.
   * @param searchRecipeType The type of recipe search to perform (default is USE_SOON_TO_EXPIRE).
   * @param onSuccess Callback function to handle successful recipe generation.
   * @param onFailure Callback function to handle recipe generation failures.
   */
  fun generateRecipe(
      recipePrompt: RecipePrompt,
      onSuccess: (Recipe) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
