package com.android.shelfLife.model.newFoodItem

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FoodItemRepositoryFirestore(private val db: FirebaseFirestore) : FoodItemRepository {

  private val collectionPath = "foodItems"

  // Local cache for food items per household
  private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
  override val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

  // Selected food item for detail view
  private val _selectedFoodItem = MutableStateFlow<FoodItem?>(null)
  val selectedFoodItem: StateFlow<FoodItem?> = _selectedFoodItem.asStateFlow()

  // Listener registration
  private var foodItemsListenerRegistration: ListenerRegistration? = null

  override suspend fun addFoodItem(householdId: String, foodItem: FoodItem) {
    try {
      db.collection(collectionPath)
          .document(householdId)
          .collection("items")
          .document(foodItem.uid)
          .set(foodItem.toMap())
          .await()

      // Update local cache
      val currentFoodItems = _foodItems.value.toMutableList()
      currentFoodItems.add(foodItem)
      _foodItems.value = currentFoodItems
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error adding food item", e)
    }
  }

  override suspend fun getFoodItems(householdId: String): List<FoodItem> {
    return try {
      val snapshot =
          db.collection(collectionPath).document(householdId).collection("items").get().await()

      val fetchedFoodItems = snapshot.documents.mapNotNull { it.toObject(FoodItem::class.java) }
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
    try {
      db.collection(collectionPath)
          .document(householdId)
          .collection("items")
          .document(foodItem.uid)
          .set(foodItem.toMap())
          .await()

      // Update local cache
      val currentFoodItems = _foodItems.value.toMutableList()
      val index = currentFoodItems.indexOfFirst { it.uid == foodItem.uid }
      if (index != -1) {
        currentFoodItems[index] = foodItem
      } else {
        currentFoodItems.add(foodItem)
      }
      _foodItems.value = currentFoodItems
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error updating food item", e)
    }
  }

  override suspend fun deleteFoodItem(householdId: String, foodItemId: String) {
    try {
      db.collection(collectionPath)
          .document(householdId)
          .collection("items")
          .document(foodItemId)
          .delete()
          .await()

      // Update local cache
      val currentFoodItems = _foodItems.value.filterNot { it.uid == foodItemId }
      _foodItems.value = currentFoodItems
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error deleting food item", e)
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
  private fun convertToFoodItem(doc: DocumentSnapshot): FoodItem? {
    return try {
      doc.toFoodItem()
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error converting document to FoodItem", e)
      null
    }
  }
}
