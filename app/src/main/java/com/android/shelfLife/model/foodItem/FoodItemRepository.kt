package com.android.shelfLife.model.foodItem

interface FoodItemRepository {
  /** Generates a new unique ID for a food item. */
  fun getNewUid(): String

  /**
   * Initializes the repository (e.g., setting up database connections or initial data).
   *
   * @param onSuccess - Called when the initialization is successful.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Fetches all food items from the repository.
   *
   * @param onSuccess - Called when the list of food items is successfully retrieved.
   * @param onFailure - Called when there is an error retrieving the food items.
   */
  fun getFoodItems(onSuccess: (List<FoodItem>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Adds a new food item to the repository.
   *
   * @param foodItem - The food item to be added.
   * @param onSuccess - Called when the food item is successfully added.
   * @param onFailure - Called when there is an error adding the food item.
   */
  fun addFoodItem(foodItem: FoodItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates an existing food item in the repository.
   *
   * @param foodItem - The food item with updated data.
   * @param onSuccess - Called when the food item is successfully updated.
   * @param onFailure - Called when there is an error updating the food item.
   */
  fun updateFoodItem(foodItem: FoodItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a food item by its unique ID.
   *
   * @param id - The unique ID of the food item to delete.
   * @param onSuccess - Called when the food item is successfully deleted.
   * @param onFailure - Called when there is an error deleting the food item.
   */
  fun deleteFoodItemById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
