package com.android.shelfLife.model.newRecipe

import com.android.shelfLife.model.recipe.Recipe
import kotlinx.coroutines.flow.StateFlow

interface RecipeRepository {

  // local cache for recipes list (keep data integrity between screens)
  val recipes: StateFlow<List<Recipe>>

  val selectedRecipe: StateFlow<Recipe?>

  /** Generates a new unique ID for a recipe. */
  fun getUid(): String

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

  /**
   * Selects a recipe
   *
   * @param recipe - The recipe we want to select
   */
  fun selectRecipe(recipe: Recipe)
}
