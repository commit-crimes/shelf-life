package com.android.shelfLife.model.recipe

interface RecipeRepository {
  /** Generates a new unique ID for a recipe. */
  fun getUid(): String

  /**
   * Initializes the repository (e.g., setting up database connections or initial data).
   *
   * @param onSuccess - Called when the initialization is successful.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Fetches all recipes from the repository.
   *
   * @param onSuccess - Called when the list of recipes is successfully retrieved.
   * @param onFailure - Called when there is an error retrieving the recipes.
   */
  fun getRecipes(onSuccess: (List<Recipe>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Fetches a recipe by its unique ID.
   *
   * @param recipeId - The unique ID of the recipe to retrieve.
   * @param onSuccess - Called when the recipe is successfully retrieved.
   * @param onFailure - Called when there is an error retrieving the recipe.
   */
  fun getRecipe(recipeId: String, onSuccess: (Recipe) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Adds a new recipe to the repository.
   *
   * @param recipe - The recipe to be added.
   * @param onSuccess - Called when the recipe is successfully added.
   * @param onFailure - Called when there is an error adding the recipe.
   */
  fun addRecipe(recipe: Recipe, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates an existing recipe in the repository.
   *
   * @param recipe - The recipe with updated data.
   * @param onSuccess - Called when the recipe is successfully updated.
   * @param onFailure - Called when there is an error updating the recipe.
   */
  fun updateRecipe(recipe: Recipe, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a recipe by its unique ID.
   *
   * @param recipeId - The unique ID of the recipe to delete.
   * @param onSuccess - Called when the recipe is successfully deleted.
   * @param onFailure - Called when there is an error deleting the recipe.
   */
  fun deleteRecipe(recipeId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
