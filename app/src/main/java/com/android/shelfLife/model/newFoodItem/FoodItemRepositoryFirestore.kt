package com.android.shelfLife.repository.newFoodItem

import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.toFoodItem
import com.android.shelfLife.model.newFoodItem.toMap
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodItemRepositoryFirestore(private val db: FirebaseFirestore) : FoodItemRepository {

    override suspend fun addFoodItem(householdId: String, foodItem: FoodItem) {
        db.collection("foodItems")
            .document(householdId)
            .collection("items")
            .document(foodItem.uid)
            .set(foodItem.toMap())
            .await()
    }

    override suspend fun getFoodItems(householdId: String): List<FoodItem> {
        val snapshot = db.collection("foodItems")
            .document(householdId)
            .collection("items")
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toFoodItem() }
    }

    override suspend fun updateFoodItem(householdId: String, foodItem: FoodItem) {
        db.collection("foodItems")
            .document(householdId)
            .collection("items")
            .document(foodItem.uid)
            .set(foodItem.toMap())
            .await()
    }

    override suspend fun deleteFoodItem(householdId: String, foodItemId: String) {
        db.collection("foodItems")
            .document(householdId)
            .collection("items")
            .document(foodItemId)
            .delete()
            .await()
    }
}