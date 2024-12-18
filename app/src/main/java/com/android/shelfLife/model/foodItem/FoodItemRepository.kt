package com.android.shelfLife.model.foodItem

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface FoodItemRepository {

  val foodItems: StateFlow<List<FoodItem>>
  val selectedFoodItem: StateFlow<FoodItem?>
  val errorMessage: StateFlow<String?>
  val isGenerated: StateFlow<Boolean>

  /** Generates a new unique ID for a food item. */
  fun getNewUid(): String

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

  /** Selects a FoodItem document for individual view */
  fun selectFoodItem(foodItem: FoodItem?)

  fun setFoodItems(householdId: String, value: List<FoodItem>)

  fun setIsGenerated(value: Boolean)

  suspend fun uploadImageToFirebaseStorage(uri: Uri, context: Context ):String?
}
