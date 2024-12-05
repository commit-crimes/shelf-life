package com.android.shelfLife.model.newInvitations

import android.util.Log
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.user.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

open class InvitationRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : InvitationRepository {

  internal var listenerRegistration: ListenerRegistration? = null
  private val _invitations: MutableStateFlow<List<Invitation>> = MutableStateFlow(emptyList())
  override val invitations: StateFlow<List<Invitation>> = _invitations.asStateFlow()
  private val invitationPath = "invitations"

  /** Removes the real-time listener for invitations. */
  override fun removeInvitationListener() {
    listenerRegistration?.remove()
    listenerRegistration = null
  }

  /**
   * Sends an invitation to a user to join a household.
   *
   * @param household The household to invite the user to.
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
  }

  /**
   * Fetches all invitations for the current user.
   *
   * @param listOfInvitationUids The list of invitation UIDs to fetch.
   * @return The list of invitations.
   */
  override suspend fun getInvitations(listOfInvitationUids: List<String>): List<Invitation> {
    if (listOfInvitationUids.isEmpty()) {
      return emptyList()
    }
    return try {
      _invitations.value =
          db.collection(invitationPath)
              .whereIn(FieldPath.documentId(), listOfInvitationUids)
              .get()
              .await()
              .documents
              .mapNotNull { convertToInvitation(it) }
      invitations.value
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error fetching households", e)
      emptyList()
    }
  }

  /**
   * Accepts an invitation.
   *
   * @param invitation The invitation to accept.
   */
  override suspend fun acceptInvitation(invitation: Invitation) {
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
  override suspend fun declineInvitation(invitation: Invitation) {
    db.collection(invitationPath).document(invitation.invitationId).delete()
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
