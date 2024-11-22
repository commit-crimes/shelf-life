package com.android.shelfLife.model.household

import android.util.Log
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.model.invitations.InvitationStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class HouseholdRepositoryFirestore(private val db: FirebaseFirestore) : HouseHoldRepository {

  private val houseHoldsCollectionPath = "households"
  var auth = FirebaseAuth.getInstance()
  var foodItemRepository = FoodItemRepositoryFirestore(db)

  /**
   * Generates a new unique ID for a household.
   *
   * @return A new unique ID.
   */
  override fun getNewUid(): String {
    return db.collection(houseHoldsCollectionPath).document().id
  }

  /**
   * Fetches all households from the repository associated with the current user.
   *
   * @param onSuccess - The callback to be invoked on success.
   * @param onFailure - The callback to be invoked on failure.
   */
  override fun getHouseholds(onSuccess: (List<HouseHold>) -> Unit, onFailure: (Exception) -> Unit) {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      db.collection(houseHoldsCollectionPath)
          .whereArrayContains("members", currentUser.uid)
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
      val householdData =
          mapOf(
              "uid" to household.uid,
              "name" to household.name,
              "members" to household.members,
              "foodItems" to
                  household.foodItems.map { foodItem ->
                    foodItemRepository.convertFoodItemToMap(foodItem)
                  })
      db.collection(houseHoldsCollectionPath)
          .document(household.uid)
          .set(householdData)
          .addOnSuccessListener { onSuccess() }
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

  /**
   * Updates an existing household in the repository.
   *
   * @param household - The household with updated data.
   * @param onSuccess - The callback to be invoked on success.
   * @param onFailure - The callback to be invoked on failure.
   */
  override fun updateHousehold(
      household: HouseHold,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      val householdData =
          mapOf(
              "uid" to household.uid,
              "name" to household.name,
              "members" to household.members,
              "foodItems" to
                  household.foodItems.map { foodItem ->
                    foodItemRepository.convertFoodItemToMap(foodItem)
                  })
      db.collection(houseHoldsCollectionPath)
          .document(household.uid)
          .update(householdData)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              onSuccess()
            } else {
              Log.e("HouseholdRepository", "Error updating household", task.exception)
              task.exception?.let { onFailure(it) }
            }
          }
    } else {
      Log.e("HouseholdRepository", "User not logged in")
      onFailure(Exception("User not logged in"))
    }
  }

  /**
   * Deletes a household by its unique ID.
   *
   * @param id - The unique ID of the household to delete.
   * @param onSuccess - The callback to be invoked on success.
   * @param onFailure - The callback to be invoked on failure.
   */
  override fun deleteHouseholdById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      db.collection(houseHoldsCollectionPath)
          .document(id)
          .delete()
          .addOnSuccessListener { onSuccess() }
          .addOnFailureListener { exception ->
            Log.e("HouseholdRepository", "Error deleting household", exception)
            onFailure(exception)
          }
    } else {
      Log.e("HouseholdRepository", "User not logged in")
      onFailure(Exception("User not logged in"))
    }
  }

  override fun getUserIds(users: List<String>, callback: (Map<String, String>) -> Unit) {
    if (users.isEmpty()) {
      callback(emptyMap())
      return
    }

    val emailBatches = users.chunked(10) // Firestore allows up to 10 values in 'whereIn'
    val emailToUserId = mutableMapOf<String, String>()
    var batchesProcessed = 0

    for (emailBatch in emailBatches) {
      db.collection("users")
          .whereIn("email", emailBatch)
          .get()
          .addOnSuccessListener { querySnapshot ->
            for (doc in querySnapshot.documents) {
              val email = doc.getString("email")
              val userId = doc.id
              if (email != null) {
                emailToUserId[email] = userId
              }
            }
            batchesProcessed++
            if (batchesProcessed == emailBatches.size) {
              callback(emailToUserId)
            }
          }
          .addOnFailureListener { exception ->
            Log.e("HouseholdRepository", "Error fetching user IDs by emails", exception)
            batchesProcessed++
            if (batchesProcessed == emailBatches.size) {
              callback(emailToUserId)
            }
          }
    }
  }

  override fun getUserEmails(userIds: List<String>, callback: (Map<String, String>) -> Unit) {
    if (userIds.isEmpty()) {
      callback(emptyMap())
      return
    }

    val uidBatches = userIds.chunked(10) // Firestore allows up to 10 values in 'whereIn'
    val uidToEmail = mutableMapOf<String, String>()
    var batchesProcessed = 0

    for (uidBatch in uidBatches) {
      db.collection("users")
          .whereIn(FieldPath.documentId(), uidBatch)
          .get()
          .addOnSuccessListener { querySnapshot ->
            for (doc in querySnapshot.documents) {
              val email = doc.getString("email")
              val userId = doc.id
              if (email != null) {
                uidToEmail[userId] = email
              }
            }
            batchesProcessed++
            if (batchesProcessed == uidBatches.size) {
              callback(uidToEmail)
            }
          }
          .addOnFailureListener { exception ->
            Log.e("HouseholdRepository", "Error fetching emails by user IDs", exception)
            batchesProcessed++
            if (batchesProcessed == uidBatches.size) {
              callback(uidToEmail)
            }
          }
    }
  }

  /**
   * Converts a Firestore document to a HouseHold object.
   *
   * @param doc The Firestore document to convert.
   */
  fun convertToHousehold(doc: DocumentSnapshot): HouseHold? {
    return try {
      val uid = doc.getString("uid") ?: return null
      val name = doc.getString("name") ?: return null
      val members = doc.get("members") as? List<String> ?: emptyList()
      val foodItems = doc.get("foodItems") as? List<Map<String, Any>> ?: emptyList()

      // Convert the list of food items from firestore into a list of FoodItem objects
      val foodItemList =
          foodItems.mapNotNull { foodItemMap ->
            foodItemRepository.convertToFoodItemFromMap(foodItemMap)
          }

      HouseHold(uid = uid, name = name, members = members, foodItems = foodItemList)
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error converting document to HouseHold", e)
      null
    }
  }

  /**
   * Sends an invitation to a user to join a household.
   *
   * @param household The household to invite the user to.
   * @param invitedUserEmail The email of the user to invite.
   * @param onSuccess The callback to be invoked on success.
   * @param onFailure The callback to be invoked on failure.
   */
  override fun sendInvitation(
      household: HouseHold,
      invitedUserEmail: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Get the user ID of the invited user by email
    db.collection("users")
        .whereEqualTo("email", invitedUserEmail)
        .get()
        .addOnSuccessListener { querySnapshot ->
          if (!querySnapshot.isEmpty) {
            val invitedUserDoc = querySnapshot.documents[0]
            val invitedUserId = invitedUserDoc.id

            val invitationId = db.collection("invitations").document().id
            val invitationData =
                mapOf(
                    "invitationId" to invitationId,
                    "householdId" to household.uid,
                    "householdName" to household.name,
                    "invitedUserId" to invitedUserId,
                    "inviterUserId" to FirebaseAuth.getInstance().currentUser?.uid,
                    "status" to "pending",
                    "timestamp" to Timestamp.now())

            db.collection("invitations")
                .document(invitationId)
                .set(invitationData)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception ->
                  Log.e("HouseholdRepository", "Error sending invitation", exception)
                  onFailure(exception)
                }
          } else {
            Log.e("HouseholdRepository", "No user found with email: $invitedUserEmail")
            onFailure(Exception("No user found with email: $invitedUserEmail"))
          }
        }
        .addOnFailureListener { exception ->
          Log.e("HouseholdRepository", "Error finding user by email", exception)
          onFailure(exception)
        }
  }

  /**
   * Fetches all invitations for the current user.
   *
   * @param onSuccess The callback to be invoked on success.
   * @param onFailure The callback to be invoked on failure.
   * @return The list of invitations.
   */
  override fun getInvitations(
      onSuccess: (List<Invitation>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
      db.collection("invitations")
          .whereEqualTo("invitedUserId", currentUser.uid)
          .whereEqualTo("status", "pending")
          .get()
          .addOnSuccessListener { querySnapshot ->
            val invitations = querySnapshot.documents.mapNotNull { doc -> convertToInvitation(doc) }
            onSuccess(invitations)
          }
          .addOnFailureListener { exception ->
            Log.e("HouseholdRepository", "Error fetching invitations", exception)
            onFailure(exception)
          }
    } else {
      Log.e("HouseholdRepository", "User not logged in")
      onFailure(Exception("User not logged in"))
    }
  }

  /**
   * Accepts an invitation.
   *
   * @param invitation The invitation to accept.
   * @param onSuccess The callback to be invoked on success.
   * @param onFailure The callback to be invoked on failure.
   */
  override fun acceptInvitation(
      invitation: Invitation,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
        db.collection("invitations")
            .whereEqualTo("invitedUserId", currentUser.uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener {
                // delete the invitation
                db.collection("invitations").
                document(invitation.invitationId).
                delete()
                // add the user to the household
                db.collection("households").
                document(invitation.householdId).
                update("members", FieldValue.arrayUnion(currentUser.uid))
                Log.d("HouseholdRepository", "Invitation accepted")
              onSuccess()
            }
            .addOnFailureListener { exception ->
              Log.e("HouseholdRepository", "Error fetching invitations", exception)
              onFailure(exception)
            }
    } else {
      Log.e("HouseholdRepository", "User not logged in")
      onFailure(Exception("User not logged in"))
    }
  }

  /**
   * Declines an invitation.
   *
   * @param invitation The invitation to decline.
   * @param onSuccess The callback to be invoked on success.
   * @param onFailure The callback to be invoked on failure.
   */
  override fun declineInvitation(
      invitation: Invitation,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
      // delete the invitation
      db.collection("invitations")
        .document(invitation.invitationId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("HouseholdRepository", "Error declining invitation", exception)
          onFailure(exception)
        }
  }

  /**
   * Converts a Firestore document to an Invitation object.
   *
   * @param doc The Firestore document to convert.
   * @return The corresponding Invitation object, or null if the document is invalid.
   */
  private fun convertToInvitation(doc: DocumentSnapshot): Invitation? {
    return try {
      Invitation(
          invitationId = doc.getString("invitationId") ?: return null,
          householdId = doc.getString("householdId") ?: return null,
          householdName = doc.getString("householdName") ?: return null,
          invitedUserId = doc.getString("invitedUserId") ?: return null,
          inviterUserId = doc.getString("inviterUserId") ?: return null,
          timestamp = doc.getTimestamp("timestamp"))
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error converting document to Invitation", e)
      null
    }
  }
}
