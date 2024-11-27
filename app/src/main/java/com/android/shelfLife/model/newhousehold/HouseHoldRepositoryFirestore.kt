package com.android.shelfLife.model.newhousehold

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HouseholdRepositoryFirestore(private val db: FirebaseFirestore) : HouseHoldRepository {

    private val collectionPath = "households"

    /**
     * Generates a new unique ID for a household.
     *
     * @return A new unique ID.
     */
    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
    }

    override suspend fun getHouseholds(listOfHouseHoldUid: List<String>): List<HouseHold> {
        if (listOfHouseHoldUid.isEmpty()) return emptyList()

        val querySnapshot = db.collection(collectionPath)
            .whereIn(FieldPath.documentId(), listOfHouseHoldUid)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { convertToHousehold(it) }
    }

    /**
     * Adds a new household to the repository.
     */
    override suspend fun addHousehold(household: HouseHold) {
        val householdData = mapOf(
            "name" to household.name,
            "members" to household.members,
            "sharedRecipes" to household.sharedRecipes
        )
        db.collection(collectionPath)
            .document(household.uid) // Use the household UID as the document ID
            .set(householdData)
            .await()
    }

    /**
     * Updates an existing household in the repository.
     */
    override suspend fun updateHousehold(household: HouseHold) {
        val householdData = mapOf(
            "name" to household.name,
            "members" to household.members,
            "sharedRecipes" to household.sharedRecipes
        )
        db.collection(collectionPath)
            .document(household.uid) // Use the household UID as the document ID
            .set(householdData) // Use set to overwrite or update
            .await()
    }

    /**
     * Deletes a household by its unique ID.
     */
    override suspend fun deleteHouseholdById(id: String) {
        db.collection(collectionPath)
            .document(id)
            .delete()
            .await()
    }

    override suspend fun getHouseholdMembers(householdId: String): List<String> {
        return try {
            val document = db.collection(collectionPath)
                .document(householdId)
                .get()
                .await()

            document.get("members") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("HouseholdRepository", "Error fetching members for household: $householdId", e)
            emptyList()
        }
    }

    /**
     * Converts a Firestore document to a HouseHold object.
     *
     * @param doc The Firestore document to convert.
     */
    private fun convertToHousehold(doc: DocumentSnapshot): HouseHold? {
        return try {
            val uid = doc.id // Use the document ID as the UID
            val name = doc.getString("name") ?: return null
            val members = doc.get("members") as? List<String> ?: emptyList()
            val sharedRecipes = doc.get("sharedRecipes") as? List<String> ?: emptyList()

            HouseHold(uid = uid, name = name, members = members, sharedRecipes = sharedRecipes)
        } catch (e: Exception) {
            Log.e("HouseholdRepository", "Error converting document to HouseHold", e)
            null
        }
    }
}