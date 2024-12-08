package com.android.shelfLife.model.invitations

import com.android.shelfLife.model.household.HouseHold
import kotlinx.coroutines.flow.StateFlow

interface InvitationRepository {

  val invitations: StateFlow<List<Invitation>>

  /** Removes the real-time listener for invitations. */
  fun removeInvitationListener()

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
   * Fetches all invitations for the current user.
   *
   * @param listOfInvitationUids The list of invitation UIDs to fetch.
   * @return The list of invitations.
   */
  suspend fun getInvitations(listOfInvitationUids: List<String>): List<Invitation>

  /**
   * Sends an invitation to a user to join a household.
   *
   * @param household The household to invite the user to.
   * @param user The user to invite.
   */
  fun sendInvitation(household: HouseHold, invitedUserID: String)
}
