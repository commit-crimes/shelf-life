package com.android.shelfLife.model.newInvitations

import com.android.shelfLife.model.newhousehold.HouseHold
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.StateFlow

interface InvitationRepository {

  /**
   * Declines an invitation.
   *
   * @param invitation The invitation to decline.
   */
  suspend fun declineInvitation(invitation: Invitation)

  /**
   * Accepts an invitation.
   *
   * @param invitation The invitation to accept.
   */
  suspend fun acceptInvitation(invitation: Invitation)

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
  suspend fun getInvitationsBatch(invitationUIDs: List<String>): QuerySnapshot

  suspend fun getInvitation(uid: String): Invitation?

  fun convertToInvitation(doc: DocumentSnapshot): Invitation?
}
