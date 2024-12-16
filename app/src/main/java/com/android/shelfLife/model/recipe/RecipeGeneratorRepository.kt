package com.android.shelfLife.model.recipe

/**
 * Interface representing a repository for generating recipes.
 *
 * This repository provides functionality to generate recipes dynamically
 * based on the provided input, such as available food items or recipe preferences.
 */
interface RecipeGeneratorRepository {

  /**
   * Generates a recipe based on the provided recipe prompt.
   *
   * The recipe prompt includes details such as desired ingredients, recipe type,
   * servings, and special instructions. The repository uses this information to
   * create a recipe tailored to the user's preferences or constraints.
   *
   * @param recipePrompt The [RecipePrompt] containing the criteria for recipe generation.
   * @return The generated [Recipe], or `null` if recipe generation fails.
   */
  suspend fun generateRecipe(recipePrompt: RecipePrompt): Recipe?
}