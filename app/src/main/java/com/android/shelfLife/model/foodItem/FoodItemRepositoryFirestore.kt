package com.android.shelfLife.model.foodItem
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FoodItemRepositoryFirestore(private val db: FirebaseFirestore) : FoodItemRepository {

    private val collectionPath = "foodItems"
    private val auth = FirebaseAuth.getInstance()

    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
    }

    override fun init(onSuccess: () -> Unit) {
        auth.addAuthStateListener { authVal ->
            val currentUser = authVal.currentUser
            if (currentUser != null) {
                db.collection(collectionPath).get().addOnCompleteListener { task ->
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
        Log.d("FoodItemRepoFire", "init done")
    }

    override fun getFoodItems(onSuccess: (List<FoodItem>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { result ->
                val foodItemList = mutableListOf<FoodItem>()
                Log.e("FoodItemRepository", "list: $foodItemList")
                Log.e("FoodItemRepository", "res: $result")

                for (document in result) {
                    Log.e("FoodItemRepository", "Doc: $document")
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

    override fun addFoodItem(foodItem: FoodItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(foodItem.uid)
            .set(foodItem)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e("FoodItemRepository", "Error adding food item", exception)
                onFailure(exception)
            }
    }

    override fun updateFoodItem(foodItem: FoodItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(foodItem.uid)
            .set(foodItem)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e("FoodItemRepository", "Error updating food item", exception)
                onFailure(exception)
            }
    }

    override fun deleteFoodItemById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e("FoodItemRepository", "Error deleting food item", exception)
                onFailure(exception)
            }
    }

    // Helper function to convert Firestore DocumentSnapshot into a FoodItem object
    private fun convertToFoodItem(doc: DocumentSnapshot): FoodItem? {
        return try {
            val uid = doc.getString("uid") ?: return null
            val name = doc.getString("name") ?: return null
            val barcode = doc.getString("barcode") ?: return null

            val expiryDate = doc.getTimestamp("expiryDate") ?: Timestamp.now()
            val buyDate = doc.getTimestamp("buyDate") ?: Timestamp.now()
            val status = doc.getString("status") ?: FoodItemStatus.CLOSED.name

            val quantityMap = doc.get("quantity") as? Map<*,*> ?: return null
            val quantity = Quantity(
                amount = quantityMap["amount"] as? Double ?: 0.0,
                unit = Unit.valueOf(quantityMap["unit"] as? String ?: "GRAM")
            )

            val nutritionMap = doc.get("nutritionFacts") as? Map<*, *>
            val nutritionFacts = if (nutritionMap != null) {
                NutritionFacts(
                    energyKcal = (nutritionMap["energyKcal"] as? Long)?.toInt() ?: 0,
                    fat = (nutritionMap["fat"] as? Double) ?: 0.0,
                    saturatedFat = (nutritionMap["saturatedFat"] as? Double) ?: 0.0,
                    carbohydrates = (nutritionMap["carbohydrates"] as? Double) ?: 0.0,
                    sugars = (nutritionMap["sugars"] as? Double) ?: 0.0,
                    proteins = (nutritionMap["proteins"] as? Double) ?: 0.0,
                    salt = (nutritionMap["salt"] as? Double) ?: 0.0,
                )
            } else {
                NutritionFacts() // default empty values
            }

            FoodItem(
                uid = uid,
                name = name,
                barcode = barcode,
                quantity = quantity,
                expiryDate = expiryDate,
                buyDate = buyDate,
                status = FoodItemStatus.valueOf(status),
                nutritionFacts = nutritionFacts
            )
        } catch (e: Exception) {
            Log.e("FoodItemRepository", "Error converting document to FoodItem", e)
            null
        }
    }
}
