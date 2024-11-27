package com.android.shelfLife.model.invitations

import android.util.Log
import com.android.shelfLife.model.household.HouseHold
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

open class InvitationRepositoryFirestore(private val db: FirebaseFirestore) : InvitationRepository {

  private var listenerRegistration: ListenerRegistration? = null

  /**
   * Sets up a real-time listener for invitations for the current user.
   *
   * @param onUpdate The callback to invoke with the updated list of invitations.
   * @param onError The callback to invoke in case of an error.
   */
  override fun addInvitationListener(
      onUpdate: (List<Invitation>) -> Unit,
      onError: (Exception) -> Unit
  ) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
      listenerRegistration =
          db.collection("invitations")
              .whereEqualTo("invitedUserId", currentUser.uid)
              .addSnapshotListener { snapshots, error ->
                if (error != null) {
                  onError(error)
                  return@addSnapshotListener
                }

                if (snapshots != null) {
                  val invitations =
                      snapshots.documents.mapNotNull { doc -> convertToInvitation(doc) }
                  onUpdate(invitations)
                }
              }
    } else {
      onError(Exception("User not logged in"))
    }
  }

  /** Removes the real-time listener for invitations. */
  override fun removeInvitationListener() {
    listenerRegistration?.remove()
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
          .get()
          .addOnSuccessListener {
            // delete the invitation
            db.collection("invitations").document(invitation.invitationId).delete()
            // add the user to the household
            db.collection("households")
                .document(invitation.householdId)
                .update("members", FieldValue.arrayUnion(currentUser.uid))
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
