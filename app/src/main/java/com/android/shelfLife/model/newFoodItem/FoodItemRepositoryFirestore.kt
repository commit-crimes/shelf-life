package com.android.shelfLife.model.newFoodItem

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FoodItemRepositoryFirestore
@Inject
constructor
  (private val db: FirebaseFirestore) : FoodItemRepository {

  private val collectionPath = "foodItems"

  // Local cache for food items per household
  private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
  override val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

  private val _selectedFoodItem = MutableStateFlow<FoodItem?>(null)
  override val selectedFoodItem: StateFlow<FoodItem?> = _selectedFoodItem.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Listener registration
  private var foodItemsListenerRegistration: ListenerRegistration? = null

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override suspend fun addFoodItem(householdId: String, foodItem: FoodItem) {
    try {
      // Update local cache
      val currentFoodItems = _foodItems.value.toMutableList()
      currentFoodItems.add(foodItem)
      _foodItems.value = currentFoodItems

      db.collection(collectionPath)
          .document(householdId)
          .collection("items")
          .document(foodItem.uid)
          .set(foodItem.toMap())
          .await()
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error adding food item", e)
      val updatedFoodItems = _foodItems.value.filterNot { it.uid == foodItem.uid }
      _foodItems.value = updatedFoodItems
      // Notify the user about the error
      _errorMessage.value = "Failed to add item. Please try again."
    }
  }

  override suspend fun getFoodItems(householdId: String): List<FoodItem> {
    return try {
      val snapshot =
          db.collection(collectionPath).document(householdId).collection("items").get().await()

      val fetchedFoodItems = snapshot.documents.mapNotNull { convertToFoodItem(it) }
      _foodItems.value = fetchedFoodItems
      fetchedFoodItems
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error fetching food items", e)
      emptyList()
    }
  }

  override fun selectFoodItem(foodItem: FoodItem?) {
    _selectedFoodItem.value = foodItem
  }

  override suspend fun updateFoodItem(householdId: String, foodItem: FoodItem) {
    var originalItem: FoodItem? = null
    try {
      // Find the index of the item to be updated
      val currentFoodItems = _foodItems.value.toMutableList()
      val index = currentFoodItems.indexOfFirst { it.uid == foodItem.uid }
      if (index != -1) {
        // Store a copy of the original item
        originalItem = currentFoodItems[index]
        // Update the local cache with the new item
        currentFoodItems[index] = foodItem
        _foodItems.value = currentFoodItems
      } else {
        // Item not found, add it to the list
        currentFoodItems.add(foodItem)
        _foodItems.value = currentFoodItems
      }

      // Perform Firebase operation asynchronously
      db.collection(collectionPath)
          .document(householdId)
          .collection("items")
          .document(foodItem.uid)
          .set(foodItem)
          .await()
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error updating food item", e)
      // Rollback: Restore the original item in the local cache
      originalItem?.let {
        val currentFoodItems = _foodItems.value.toMutableList()
        val index = currentFoodItems.indexOfFirst { it.uid == foodItem.uid }
        if (index != -1) {
          currentFoodItems[index] = it
          _foodItems.value = currentFoodItems
        } else if (index == currentFoodItems.lastIndex) {
          currentFoodItems.removeAt(index)
          _foodItems.value = currentFoodItems
        }
      }
      // Notify the user about the error
      _errorMessage.value = "Failed to update item. Please try again."
    }
  }

  override suspend fun deleteFoodItem(householdId: String, foodItemId: String) {
    var deletedItem: FoodItem? = null
    try {
      // Find the item to be deleted
      deletedItem = _foodItems.value.find { it.uid == foodItemId }

      // Update local cache immediately
      val currentFoodItems = _foodItems.value.filterNot { it.uid == foodItemId }
      _foodItems.value = currentFoodItems

      // Perform Firebase operation asynchronously
      db.collection(collectionPath)
          .document(householdId)
          .collection("items")
          .document(foodItemId)
          .delete()
          .await()
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error deleting food item", e)
      // Rollback: Restore the deleted item in the local cache
      deletedItem?.let {
        val currentFoodItems = _foodItems.value.toMutableList()
        currentFoodItems.add(it)
        _foodItems.value = currentFoodItems
      }
      // Notify the user about the error
      _errorMessage.value = "Failed to delete item. Please try again."
    }
  }

  /**
   * Starts listening for real-time updates to the food items collection.
   *
   * @param householdId The ID of the household.
   */
  fun startListeningForFoodItems(householdId: String) {
    // Remove any existing listener
    foodItemsListenerRegistration?.remove()

    foodItemsListenerRegistration =
        db.collection(collectionPath)
            .document(householdId)
            .collection("items")
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                Log.e("FoodItemRepository", "Error fetching food items", error)
                _foodItems.value = emptyList()
                return@addSnapshotListener
              }
              if (snapshot != null) {
                val updatedFoodItems =
                    snapshot.documents.mapNotNull { it.toObject(FoodItem::class.java) }
                _foodItems.value = updatedFoodItems
              }
            }
  }

  /** Stops listening for real-time updates. */
  fun stopListeningForFoodItems() {
    foodItemsListenerRegistration?.remove()
    foodItemsListenerRegistration = null
  }

  /**
   * Converts a Firestore document to a FoodItem object.
   *
   * @param doc The Firestore document to convert.
   * @return A FoodItem object or null if conversion fails.
   */
  internal fun convertToFoodItem(doc: DocumentSnapshot): FoodItem? {
    return try {
      doc.toFoodItem()
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error converting document to FoodItem", e)
      null
    }
  }
}
