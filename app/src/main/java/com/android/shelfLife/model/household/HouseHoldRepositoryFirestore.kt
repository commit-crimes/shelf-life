package com.android.shelfLife.model.household

import android.util.Log
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class HouseholdRepositoryFirestore(private val db: FirebaseFirestore) {

    private val collectionPath = "households"
    private val auth = FirebaseAuth.getInstance()
    private val foodItemRepository = FoodItemRepositoryFirestore(db)

    fun getHouseholds(onSuccess: (List<HouseHold>) -> Unit, onFailure: (Exception) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection(collectionPath)
                .whereArrayContains("members", currentUser.uid) // Assuming you filter by user ID
                .get()
                .addOnSuccessListener { result ->
                    val householdList = result.documents.mapNotNull { convertToHousehold(it) }
                    onSuccess(householdList)
                }
                .addOnFailureListener { exception ->
                    Log.e("HouseholdRepository", "Error fetching households", exception)
                    onFailure(exception)
                }
        } else {
            Log.e("HouseholdRepository", "User not logged in")
            onFailure(Exception("User not logged in"))
        }
    }

    private fun convertToHousehold(doc: DocumentSnapshot): HouseHold? {
        return try {
            val uid = doc.getString("uid") ?: return null
            val name = doc.getString("name") ?: return null
            val members = doc.get("members") as? List<String> ?: emptyList()
            val foodItems = doc.get("foodItems") as? List<Map<String, Any>>

            // Convert the list of food items from Firestore into a list of FoodItem objects
            val foodItemList = foodItems?.mapNotNull { foodItemMap ->
                foodItemRepository.convertToFoodItemFromMap(foodItemMap)
            } ?: emptyList()

            HouseHold(uid = uid, name = name, members = members, foodItems = foodItemList)
        } catch (e: Exception) {
            Log.e("HouseholdRepository", "Error converting document to HouseHold", e)
            null
        }
    }
}