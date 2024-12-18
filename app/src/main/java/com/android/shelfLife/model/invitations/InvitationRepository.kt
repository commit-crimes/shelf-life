package com.android.shelfLife.model.invitations

import com.android.shelfLife.model.household.HouseHold
import com.google.firebase.firestore.DocumentSnapshot

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
   * @param user The user to invite.
   */
  fun sendInvitation(household: HouseHold, invitedUserID: String)

  /**
   * Gets a specific invitation.
   *
   * @param uid The uid of the invitation to get.
   */
  suspend fun getInvitationsBatch(invitationUIDs: List<String>): List<Invitation>

  suspend fun getInvitation(uid: String): Invitation?

  fun convertToInvitation(doc: DocumentSnapshot): Invitation?
}
