package com.android.shelfLife.model.foodItem

import android.util.Log
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FoodItemRepositoryFirestore(private val db: FirebaseFirestore) : FoodItemRepository {

  companion object {
    private const val COLLECTION_PATH = "foodItems"
  }

  private val auth = FirebaseAuth.getInstance()
  /**
   * Generates a new unique ID for a food item.
   *
   * @return A new unique ID.
   */
  override fun getNewUid(): String {
    return db.collection(COLLECTION_PATH).document().id
  }

  /**
   * Initializes the repository (e.g., setting up database connections or initial data).
   *
   * @param onSuccess - Called when the initialization is successful.
   */
  override fun init(onSuccess: () -> Unit) {
    auth.addAuthStateListener { authVal ->
      val currentUser = authVal.currentUser
      if (currentUser != null) {
        db.collection(COLLECTION_PATH).get().addOnCompleteListener { task ->
          if (task.isSuccessful) {
            onSuccess()
          } else {
            Log.e("FoodItemRepoFire", "init failed: could not get collection : ${task.exception}")
          }
        }
      } else {
        Log.e("FoodItemRepoFire", "init failed: user not logged in")
      }
    }
  }

  /**
   * Fetches all food items from the repository.
   *
   * @param onSuccess - Called when the list of food items is successfully retrieved.
   * @param onFailure - Called when there is an error retrieving the food items.
   */
  override fun getFoodItems(onSuccess: (List<FoodItem>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(COLLECTION_PATH)
        .get()
        .addOnSuccessListener { result ->
          val foodItemList = mutableListOf<FoodItem>()
          for (document in result) {
            val foodItem = convertToFoodItem(document)
            if (foodItem != null) foodItemList.add(foodItem)
          }
          onSuccess(foodItemList)
        }
        .addOnFailureListener { exception ->
          Log.e("FoodItemRepository", "Error fetching food items", exception)
          onFailure(exception)
        }
  }

  /**
   * Adds a new food item to the repository.
   *
   * @param foodItem - The food item to be added.
   * @param onSuccess - Called when the food item is successfully added.
   * @param onFailure - Called when there is an error adding the food item.
   */
  override fun addFoodItem(
      foodItem: FoodItem,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_PATH)
        .document(foodItem.uid)
        .set(foodItem)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("FoodItemRepository", "Error adding food item", exception)
          onFailure(exception)
        }
  }

  /**
   * Updates an existing food item in the repository.
   *
   * @param foodItem - The food item with updated data.
   * @param onSuccess - Called when the food item is successfully updated.
   * @param onFailure - Called when there is an error updating the food item.
   */
  override fun updateFoodItem(
      foodItem: FoodItem,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_PATH)
        .document(foodItem.uid)
        .set(foodItem)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("FoodItemRepository", "Error updating food item", exception)
          onFailure(exception)
        }
  }

  /**
   * Deletes a food item by its unique ID.
   *
   * @param id - The unique ID of the food item to delete.
   * @param onSuccess - Called when the food item is successfully deleted.
   * @param onFailure - Called when there is an error deleting the food item.
   */
  override fun deleteFoodItemById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(COLLECTION_PATH)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("FoodItemRepository", "Error deleting food item", exception)
          onFailure(exception)
        }
  }

  // Helper function to convert Firestore DocumentSnapshot into a FoodItem object
  fun convertToFoodItem(doc: DocumentSnapshot): FoodItem? {
    return try {
      val uid = doc.getString("uid") ?: return null

      // Extract FoodFacts properties from the Firestore document
      val name = doc.getString("name") ?: return null
      val barcode = doc.getString("barcode") ?: return null

      val quantityMap = doc["quantity"] as? Map<*, *> ?: return null
      val quantity =
          Quantity(
              amount = quantityMap["amount"] as? Double ?: 0.0,
              unit = FoodUnit.valueOf(quantityMap["unit"] as? String ?: "GRAM"))

      val nutritionMap = doc["nutritionFacts"] as? Map<*, *>
      val nutritionFacts =
          if (nutritionMap != null) {
            NutritionFacts(
                energyKcal = (nutritionMap["energyKcal"] as? Long)?.toInt() ?: 0,
                fat = (nutritionMap["fat"] as? Double) ?: 0.0,
                saturatedFat = (nutritionMap["saturatedFat"] as? Double) ?: 0.0,
                carbohydrates = (nutritionMap["carbohydrates"] as? Double) ?: 0.0,
                sugars = (nutritionMap["sugars"] as? Double) ?: 0.0,
                proteins = (nutritionMap["proteins"] as? Double) ?: 0.0,
                salt = (nutritionMap["salt"] as? Double) ?: 0.0)
          } else {
            NutritionFacts() // default empty values
          }

      val categoryString = doc.getString("category") ?: FoodCategory.OTHER.name
      val foodCategory = FoodCategory.valueOf(categoryString)

      // Create the FoodFacts object
      val foodFacts =
          FoodFacts(
              name = name,
              barcode = barcode,
              quantity = quantity,
              category = foodCategory,
              nutritionFacts = nutritionFacts)

      // Extract FoodItem-specific properties
      val expiryDate = doc.getTimestamp("expiryDate") ?: Timestamp.now()
      val buyDate = doc.getTimestamp("buyDate") ?: Timestamp.now()
      val status = doc.getString("status") ?: FoodStatus.CLOSED.name
      val foodStatus = FoodStatus.valueOf(status)

      val locationMap = doc["location"] as? Map<*, *>
      val foodStorageLocation =
          if (locationMap != null) {
            FoodStorageLocation.valueOf(
                locationMap["location"] as? String ?: FoodStorageLocation.PANTRY.name)
          } else {
            FoodStorageLocation.PANTRY
          }

      // Create the FoodItem object using FoodFacts
      FoodItem(
          uid = uid,
          foodFacts = foodFacts,
          status = foodStatus,
          location = foodStorageLocation,
          expiryDate = expiryDate,
          buyDate = buyDate)
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error converting document to FoodItem", e)
      null
    }
  }

  /**
   * Converts a Firestore document to a FoodItem object.
   *
   * @param map The Firestore document to convert.
   * @return A FoodItem object.
   */
  fun convertToFoodItemFromMap(map: Map<String, Any?>): FoodItem? {
    return try {
      val uid = map["uid"] as? String ?: return null
      val name = map["name"] as? String ?: return null
      val barcode = map["barcode"] as? String ?: ""
      val quantityMap = map["quantity"] as? Map<*, *>

      val quantity =
          Quantity(
              amount = quantityMap?.get("amount") as? Double ?: 0.0,
              unit = FoodUnit.valueOf(quantityMap?.get("unit") as? String ?: "GRAM"))

      val foodFacts = FoodFacts(name = name, barcode = barcode, quantity = quantity)
      val expiryDate = map["expiryDate"] as? Timestamp ?: Timestamp.now()
      val status = map["status"] as? String ?: FoodStatus.CLOSED.name
      val foodStatus = FoodStatus.valueOf(status)

      FoodItem(uid = uid, foodFacts = foodFacts, expiryDate = expiryDate, status = foodStatus)
    } catch (e: Exception) {
      Log.e("FoodItemRepository", "Error converting map to FoodItem", e)
      null
    }
  }

  /**
   * Converts a FoodItem object to a Firestore document.
   *
   * @param foodItem The FoodItem object to convert.
   * @return A Firestore document.
   */
  fun convertFoodItemToMap(foodItem: FoodItem): Map<String, Any?> {
    return mapOf(
        "uid" to foodItem.uid,
        "name" to foodItem.foodFacts.name,
        "barcode" to foodItem.foodFacts.barcode,
        "quantity" to
            mapOf(
                "amount" to foodItem.foodFacts.quantity.amount,
                "unit" to foodItem.foodFacts.quantity.unit.name),
        "expiryDate" to foodItem.expiryDate,
        "buyDate" to foodItem.buyDate,
        "status" to foodItem.status.name,
        "location" to foodItem.location.name,
        "nutritionFacts" to
            mapOf(
                "energyKcal" to foodItem.foodFacts.nutritionFacts.energyKcal,
                "fat" to foodItem.foodFacts.nutritionFacts.fat,
                "saturatedFat" to foodItem.foodFacts.nutritionFacts.saturatedFat,
                "carbohydrates" to foodItem.foodFacts.nutritionFacts.carbohydrates,
                "sugars" to foodItem.foodFacts.nutritionFacts.sugars,
                "proteins" to foodItem.foodFacts.nutritionFacts.proteins,
                "salt" to foodItem.foodFacts.nutritionFacts.salt),
        "category" to foodItem.foodFacts.category.name)
  }
}
