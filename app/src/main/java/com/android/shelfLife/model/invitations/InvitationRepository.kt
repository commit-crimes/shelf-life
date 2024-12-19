package com.android.shelfLife.model.invitations

import com.android.shelfLife.model.household.HouseHold
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Interface for managing invitations within the application.
 *
 * This interface defines the contract for a repository that handles the operations related to
 * invitations, including sending, accepting, and declining invitations, as well as retrieving them.
 */
interface InvitationRepository {

  /**
   * Declines an invitation.
   *
   * @param invitation The invitation to decline.
   */
  fun declineInvitation(invitation: Invitation)

  /**
   * Accepts an invitation.
   *
   * @param invitation The invitation to accept.
   */
  fun acceptInvitation(invitation: Invitation)

  /**
   * Sends an invitation to a user to join a household.
   *
   * @param household The household to invite the user to.
   * @param invitedUserID The ID of the user to invite.
   */
  fun sendInvitation(household: HouseHold, invitedUserID: String)

  /**
   * Gets a batch of invitations by their UIDs.
   *
   * @param invitationUIDs The list of UIDs of the invitations to get.
   * @return A list of invitations.
   */
  suspend fun getInvitationsBatch(invitationUIDs: List<String>): List<Invitation>

  /**
   * Gets a specific invitation by its UID.
   *
   * @param uid The UID of the invitation to get.
   * @return The invitation, or null if not found.
   */
  suspend fun getInvitation(uid: String): Invitation?

  /**
   * Converts a Firestore document to an Invitation object.
   *
   * @param doc The Firestore document to convert.
   * @return An Invitation object, or null if conversion fails.
   */
  fun convertToInvitation(doc: DocumentSnapshot): Invitation?
}
