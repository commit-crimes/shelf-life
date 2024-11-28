package com.android.shelfLife.model.newFoodItem

interface FoodItemRepository {

  /**
   * Adds a new food item to a household.
   *
   * @param householdId The ID of the household.
   * @param foodItem The food item to add.
   */
  suspend fun addFoodItem(householdId: String, foodItem: FoodItem)

  /**
   * Retrieves all food items for a household.
   *
   * @param householdId The ID of the household.
   * @return A list of food items.
   */
  suspend fun getFoodItems(householdId: String): List<FoodItem>

  /**
   * Updates an existing food item in a household.
   *
   * @param householdId The ID of the household.
   * @param foodItem The updated food item.
   */
  suspend fun updateFoodItem(householdId: String, foodItem: FoodItem)

  /**
   * Deletes a food item from a household.
   *
   * @param householdId The ID of the household.
   * @param foodItemId The ID of the food item to delete.
   */
  suspend fun deleteFoodItem(householdId: String, foodItemId: String)
}
