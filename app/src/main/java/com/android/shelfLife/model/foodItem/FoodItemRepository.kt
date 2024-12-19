package com.android.shelfLife.model.foodItem

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing food items within a household.
 *
 * This interface defines the contract for a repository that handles the operations related to food
 * items, including adding, updating, deleting, and retrieving food items, as well as managing their
 * state.
 */
interface FoodItemRepository {

  /** A StateFlow that emits the list of food items. */
  val foodItems: StateFlow<List<FoodItem>>

  /** A StateFlow that emits the currently selected food item. */
  val selectedFoodItem: StateFlow<FoodItem?>

  /** A StateFlow that emits error messages. */
  val errorMessage: StateFlow<String?>

  /** A StateFlow that indicates whether the quick add feature is enabled. */
  val isQuickAdd: StateFlow<Boolean>

  /** Generates a new unique ID for a food item. */
  fun getNewUid(): String

  /**
   * Adds a new food item to a household.
   *
   * @param householdId The ID of the household.
   * @param foodItem The food item to add.
   */
  fun addFoodItem(householdId: String, foodItem: FoodItem)

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
  fun updateFoodItem(householdId: String, foodItem: FoodItem)

  /**
   * Deletes a food item from a household.
   *
   * @param householdId The ID of the household.
   * @param foodItemId The ID of the food item to delete.
   */
  fun deleteFoodItem(householdId: String, foodItemId: String)

  /**
   * Selects a FoodItem document for individual view.
   *
   * @param foodItem The food item to select.
   */
  fun selectFoodItem(foodItem: FoodItem?)

  /**
   * Sets the list of food items for a household.
   *
   * @param householdId The ID of the household.
   * @param value The list of food items.
   */
  fun setFoodItems(householdId: String, value: List<FoodItem>)

  /**
   * Deletes all food items from a household.
   *
   * @param householdId The ID of the household.
   */
  fun deleteHouseholdDocument(householdId: String)

  /**
   * Sets the quick add feature state.
   *
   * @param value The new state of the quick add feature.
   */
  fun setisQuickAdd(value: Boolean)

  /**
   * Uploads an image to Firebase Storage.
   *
   * @param uri The URI of the image to upload.
   * @param context The context in which the upload is performed.
   * @return The URL of the uploaded image, or null if the upload fails.
   */
  suspend fun uploadImageToFirebaseStorage(uri: Uri, context: Context): String?
}
