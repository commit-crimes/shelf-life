package com.android.shelfLife.model.foodItem

import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.content.Context
import android.net.Uri

class FoodItemRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    FoodItemRepository {

  private val collectionPath = "foodItems"

  // Local cache for food items per household
  private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
  override val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

  private val _selectedFoodItem = MutableStateFlow<FoodItem?>(null)
  override val selectedFoodItem: StateFlow<FoodItem?> = _selectedFoodItem.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _isQuickAdd = MutableStateFlow<Boolean>(false)
  override val isQuickAdd: StateFlow<Boolean> = _isQuickAdd.asStateFlow()
  // Listener registration
  private var foodItemsListenerRegistration: ListenerRegistration? = null

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun addFoodItem(householdId: String, foodItem: FoodItem) {
    // Update local cache optimistically
    val currentFoodItems = _foodItems.value.toMutableList().apply { add(foodItem) }
    _foodItems.value = currentFoodItems

    // Perform Firestore operation using promises
    db.collection(collectionPath)
        .document(householdId)
        .collection("items")
        .document(foodItem.uid)
        .set(foodItem.toMap())
        .addOnFailureListener { exception ->
          Log.e("FoodItemRepository", "Error adding food item", exception)
          // Rollback: Remove the item from the local cache
          _foodItems.value = _foodItems.value.filterNot { it.uid == foodItem.uid }
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

  override fun setisQuickAdd(value: Boolean) {
    _isQuickAdd.value = value
  }


    override fun setFoodItems(householdId: String, value: List<FoodItem>) {
    try {
      // Update the local cache
      _foodItems.value = value

      // Clear the existing items in Firestore for the household
      val batch = db.batch()
      val householdCollection =
          db.collection(collectionPath).document(householdId).collection("items")

      // Retrieve all existing documents in the household's collection
      householdCollection
          .get()
          .addOnSuccessListener { snapshot ->
            snapshot.documents.forEach { document -> batch.delete(document.reference) }

            // Add the new items to Firestore
            value.forEach { foodItem ->
              val itemRef = householdCollection.document(foodItem.uid)
              batch.set(itemRef, foodItem.toMap()) // Assuming FoodItem has a toMap() method
            }

            // Commit the batch
            batch
                .commit()
                .addOnSuccessListener {
                  Log.d(
                      "FoodItemRepository",
                      "Successfully set food items for household: $householdId")
                }
                .addOnFailureListener { e ->
                  Log.e(
                      "FoodItemRepository", "Failed to commit batch for household: $householdId", e)
                }
          }
          .addOnFailureListener { e ->
            Log.e(
                "FoodItemRepository",
                "Failed to fetch existing items for household: $householdId",
                e)
          }
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error setting food items for household: $householdId", e)
      _errorMessage.value = "Failed to set food items. Please try again."
    }
  }

  override fun updateFoodItem(householdId: String, foodItem: FoodItem) {
    var originalItem: FoodItem? = null

    // Find the index of the item to be updated
    val currentFoodItems = _foodItems.value.toMutableList()
    val index = currentFoodItems.indexOfFirst { it.uid == foodItem.uid }

    if (index != -1) {
      // Store a copy of the original item
      originalItem = currentFoodItems[index]
      // Update the local cache with the new item
      currentFoodItems[index] = foodItem
    } else {
      // Item not found, add it to the list
      currentFoodItems.add(foodItem)
    }
    _foodItems.value = currentFoodItems

    // Perform Firebase operation asynchronously
    db.collection(collectionPath)
        .document(householdId)
        .collection("items")
        .document(foodItem.uid)
        .set(foodItem.toMap())
        .addOnSuccessListener {
          Log.d("FoodItemRepository", "Successfully updated food item: ${foodItem.uid}")
        }
        .addOnFailureListener { exception ->
          Log.e("FoodItemRepository", "Error updating food item", exception)
          // Rollback: Restore the original item in the local cache
          if (index != -1 && originalItem != null) {
            currentFoodItems[index] = originalItem
          } else if (index == -1) {
            currentFoodItems.remove(foodItem)
          }
          _foodItems.value = currentFoodItems
          // Notify the user about the error
          _errorMessage.value = "Failed to update item. Please try again."
        }
  }

  override fun deleteHouseholdDocument(householdId: String) {
    db.collection(collectionPath).document(householdId).delete()
  }

  override fun deleteFoodItem(householdId: String, foodItemId: String) {
    // Find the item to be deleted
    val deletedItem = _foodItems.value.find { it.uid == foodItemId }

    // Update local cache optimistically
    val currentFoodItems = _foodItems.value.filterNot { it.uid == foodItemId }
    _foodItems.value = currentFoodItems

    // Perform Firebase operation asynchronously
    db.collection(collectionPath)
        .document(householdId)
        .collection("items")
        .document(foodItemId)
        .delete()
        .addOnSuccessListener {
          Log.d("FoodItemRepository", "Successfully deleted food item: $foodItemId")
        }
        .addOnFailureListener { exception ->
          Log.e("FoodItemRepository", "Error deleting food item", exception)
          // Rollback: Restore the deleted item in the local cache
          if (deletedItem != null) {
            val rollbackItems = _foodItems.value.toMutableList().apply { add(deletedItem) }
            _foodItems.value = rollbackItems
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


    override suspend fun uploadImageToFirebaseStorage(uri: Uri, context: Context): String? {
        try {
            // Create a reference to Firebase Storage
            val storageReference = FirebaseStorage.getInstance("gs://shelf-life-687aa.firebasestorage.app").reference

            val fileName = "images/${System.currentTimeMillis()}.jpg"
            val imageReference: StorageReference = storageReference.child(fileName)

            // Upload the file to Firebase Storage
            imageReference.putFile(uri).await()

            // Get the download URL
            return imageReference.downloadUrl.await().toString()
        } catch (e: Exception) {
            Toast.makeText(context, "Error uploading image", Toast.LENGTH_SHORT).show()
            return null
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
