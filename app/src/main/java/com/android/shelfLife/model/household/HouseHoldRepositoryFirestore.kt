package com.android.shelfLife.model.household

import android.util.Log
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class HouseholdRepositoryFirestore(private val db: FirebaseFirestore) : HouseHoldRepository{

    private val collectionPath = "households"
    private val auth = FirebaseAuth.getInstance()
    private val foodItemRepository = FoodItemRepositoryFirestore(db)

    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
    }

    override fun getHouseholds(onSuccess: (List<HouseHold>) -> Unit, onFailure: (Exception) -> Unit) {
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

    /**
     * Adds a new household to the repository.
     *
     * @param household - The household to be added.
     * @param onSuccess - The callback to be invoked on success.
     * @param onFailure - The callback to be invoked on failure.
     */
    override fun addHousehold(
        household: HouseHold,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val householdData = mapOf(
                "uid" to household.uid,
                "name" to household.name,
                "members" to listOf(currentUser.uid),
                "foodItems" to household.foodItems.map { foodItem ->
                    foodItemRepository.convertFoodItemToMap(foodItem)
                }
            )
            db.collection(collectionPath)
                .document(household.uid)
                .set(householdData)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.e("HouseholdRepository", "Error adding household", exception)
                    onFailure(exception)
                }
        } else {
            Log.e("HouseholdRepository", "User not logged in")
            onFailure(Exception("User not logged in"))
        }
        getHouseholds({}, {})
    }

    override fun updateHousehold(
        household: HouseHold,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteHouseholdById(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        TODO("Not yet implemented")
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