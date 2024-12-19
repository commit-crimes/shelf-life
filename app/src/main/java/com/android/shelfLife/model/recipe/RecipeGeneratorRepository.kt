package com.android.shelfLife.model.recipe

/** Interface describing a model for generating recipes */
interface RecipeGeneratorRepository {
  /**
   * Generates a recipe based on the provided search input.
   *
   * @param recipePrompt The prompt containing user specifications for generating the recipe.
   * @return The generated recipe, or null if there is an error.
   */
  suspend fun generateRecipe(recipePrompt: RecipePrompt): Recipe?
}
