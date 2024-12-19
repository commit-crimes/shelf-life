package com.android.shelfLife.model.invitations

import android.util.Log
import com.android.shelfLife.model.household.HouseHold
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
 * Repository class for managing invitations in Firestore.
 *
 * @property db The Firestore database instance.
 * @property auth The FirebaseAuth instance for authentication.
 */
@Singleton
open class InvitationRepositoryFirestore
@Inject
constructor(private val db: FirebaseFirestore, private val auth: FirebaseAuth) :
    InvitationRepository {

  private val invitationPath = "invitations"

  /**
   * Sends an invitation to a user to join a household.
   *
   * @param household The household to invite the user to.
   * @param invitedUserID The ID of the user to invite.
   */
  override fun sendInvitation(household: HouseHold, invitedUserID: String) {
    val inviterUserId =
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    val invitationId = db.collection(invitationPath).document().id
    val invitationData =
        Invitation(
                invitationId = invitationId,
                householdId = household.uid,
                householdName = household.name,
                invitedUserId = invitedUserID,
                inviterUserId = inviterUserId,
                timestamp = Timestamp.now())
            .toMap()
    db.collection("invitations").document(invitationId).set(invitationData)
    db.collection("users")
        .document(invitedUserID)
        .update("invitationUIDs", FieldValue.arrayUnion(invitationId))
  }

  /**
   * Accepts an invitation.
   *
   * @param invitation The invitation to accept.
   */
  override fun acceptInvitation(invitation: Invitation) {
    db.collection(invitationPath).document(invitation.invitationId).delete()
    db.collection("households")
        .document(invitation.householdId)
        .update("members", FieldValue.arrayUnion(invitation.invitedUserId))
    Log.d("HouseholdRepository", "Invitation accepted")
  }

  /**
   * Declines an invitation.
   *
   * @param invitation The invitation to decline.
   */
  override fun declineInvitation(invitation: Invitation) {
    db.collection(invitationPath).document(invitation.invitationId).delete()
  }

  /**
   * Gets a batch of invitations by their UIDs.
   *
   * @param invitationUIDs The list of UIDs of the invitations to get.
   * @return A list of invitations.
   */
  override suspend fun getInvitationsBatch(invitationUIDs: List<String>): List<Invitation> {
    if (invitationUIDs.isEmpty()) {
      return emptyList()
    }
    val querySnapshot =
        db.collection("invitations").whereIn(FieldPath.documentId(), invitationUIDs).get().await()
    return querySnapshot.documents.mapNotNull { doc -> convertToInvitation(doc) }
  }

  /**
   * Gets a specific invitation by its UID.
   *
   * @param uid The UID of the invitation to get.
   * @return The invitation, or null if not found.
   */
  override suspend fun getInvitation(uid: String): Invitation? {
    return convertToInvitation(db.collection("invitations").document(uid).get().await())
  }

  /**
   * Converts a Firestore document to an Invitation object.
   *
   * @param doc The Firestore document to convert.
   * @return The corresponding Invitation object, or null if the document is invalid.
   */
  override fun convertToInvitation(doc: DocumentSnapshot): Invitation? {
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

  /**
   * Converts an Invitation object to a map.
   *
   * @return A map representation of the Invitation object.
   */
  private fun Invitation.toMap(): Map<String, Any?> {
    return mapOf(
        "invitationId" to invitationId,
        "householdId" to householdId,
        "householdName" to householdName,
        "invitedUserId" to invitedUserId,
        "inviterUserId" to inviterUserId,
        "timestamp" to timestamp)
  }
}
