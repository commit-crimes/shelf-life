package com.android.shelfLife.model.invitations

import com.android.shelfLife.model.household.HouseHold

interface InvitationRepository {

  /**
   * Sets up a real-time listener for invitations for the current user.
   *
   * @param onUpdate The callback to invoke with the updated list of invitations.
   * @param onError The callback to invoke in case of an error.
   */
  fun addInvitationListener(onUpdate: (List<Invitation>) -> Unit, onError: (Exception) -> Unit)

  /** Removes the real-time listener for invitations. */
  fun removeInvitationListener()
  /**
   * Declines an invitation.
   *
   * @param invitation The invitation to decline.
   * @param onSuccess The callback to be invoked on success.
   * @param onFailure The callback to be invoked on failure.
   */
  fun declineInvitation(
      invitation: Invitation,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Accepts an invitation.
   *
   * @param invitation The invitation to accept.
   * @param onSuccess The callback to be invoked on success.
   * @param onFailure The callback to be invoked on failure.
   */
  fun acceptInvitation(
      invitation: Invitation,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Fetches all invitations for the current user.
   *
   * @param onSuccess The callback to be invoked on success.
   * @param onFailure The callback to be invoked on failure.
   * @return The list of invitations.
   */
  fun getInvitations(onSuccess: (List<Invitation>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Sends an invitation to a user to join a household.
   *
   * @param household The household to invite the user to.
   * @param invitedUserEmail The email of the user to invite.
   * @param onSuccess The callback to be invoked on success.
   * @param onFailure The callback to be invoked on failure.
   */
  fun sendInvitation(
      household: HouseHold,
      invitedUserEmail: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
