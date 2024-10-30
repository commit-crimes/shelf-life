package com.android.shelfLife.model.recipe

import com.android.shelfLife.model.foodItem.FoodItem

interface RecipesRepository {

  enum class SearchRecipeType {
    USE_SOON_TO_EXPIRE,
    USE_ONLY_HOUSEHOLD_ITEMS,
    HIGH_PROTEIN,
    LOW_CALORIE,
  }

  /**
   * Generates a list of recipes based on the provided search input.
   *
   * @param listFoodItems The list of food items to use for generating recipes.
   * @param searchRecipeType The type of recipe search to perform (default is USE_SOON_TO_EXPIRE).
   * @param onSuccess Callback function to handle successful recipe generation.
   * @param onFailure Callback function to handle recipe generation failures.
   */
  fun generateRecipes(
      listFoodItems: List<FoodItem>,
      searchRecipeType: SearchRecipeType = SearchRecipeType.USE_SOON_TO_EXPIRE,
      onSuccess: (List<Recipe>) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
