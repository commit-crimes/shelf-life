package com.android.shelfLife.model.recipe

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing recipes within the repository.
 *
 * This interface defines the contract for a repository that handles the operations related to recipes,
 * including fetching, adding, updating, deleting, and selecting recipes.
 */
interface RecipeRepository {

  /** A `StateFlow` that emits the current list of recipes in the repository. */
  val recipes: StateFlow<List<Recipe>>

  /** A `StateFlow` that emits the currently selected recipe, or `null` if none is selected. */
  val selectedRecipe: StateFlow<Recipe?>

  /**
   * Generates a new unique ID for a recipe.
   *
   * @return A new unique ID as a String.
   */
  fun getUid(): String

  /**
   * Initializes recipes by fetching them from Firestore and updating the local cache.
   *
   * @param recipeIds List of recipe IDs to fetch.
   * @param selectedRecipeId An optional ID of a recipe to select after initialization.
   */
  suspend fun initializeRecipes(recipeIds: List<String>, selectedRecipeId: String?)

  /**
   * Fetches recipes by their UIDs.
   *
   * @param listUserRecipeUid A list of recipe UIDs to fetch.
   * @return A list of `Recipe` objects corresponding to the provided UIDs.
   */
  suspend fun getRecipes(listUserRecipeUid: List<String>): List<Recipe>

  /**
   * Adds a new recipe to the repository. Updates the local cache and Firestore.
   *
   * @param recipe The `Recipe` object to add.
   */
  fun addRecipe(recipe: Recipe)

  /**
   * Updates an existing recipe in the repository and Firestore.
   *
   * @param recipe The `Recipe` object with updated data.
   */
  fun updateRecipe(recipe: Recipe)

  /**
   * Deletes a recipe from the repository and Firestore by its unique ID.
   *
   * @param recipeId The unique ID of the recipe to delete.
   * @param deleteUID A function to execute after deleting the recipe, default is an empty function.
   */
  fun deleteRecipe(recipeId: String, deleteUID: (String) -> Unit = {})

  /**
   * Selects a recipe in the local state. This does not affect Firestore.
   *
   * @param recipe The recipe to select, or `null` to deselect.
   */
  fun selectRecipe(recipe: Recipe?)
}