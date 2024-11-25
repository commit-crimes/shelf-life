package com.android.shelfLife.model.household

import com.android.shelfLife.model.invitations.Invitation

interface HouseHoldRepository {

  /** Generates a new unique ID for a household. */
  fun getNewUid(): String

  /**
   * Fetches all households from the repository associated with the current user.
   *
   * @param onSuccess - Called when the list of households is successfully retrieved.
   * @param onFailure - Called when there is an error retrieving the households.
   */
  fun getHouseholds(onSuccess: (List<HouseHold>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Adds a new household to the repository.
   *
   * @param household - The household to be added.
   * @param onSuccess - Called when the household is successfully added.
   * @param onFailure - Called when there is an error adding the household.
   */
  fun addHousehold(household: HouseHold, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates an existing household in the repository.
   *
   * @param household - The household with updated data.
   * @param onSuccess - Called when the household is successfully updated.
   * @param onFailure - Called when there is an error updating the household.
   */
  fun updateHousehold(household: HouseHold, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a household by its unique ID.
   *
   * @param id - The unique ID of the household to delete.
   * @param onSuccess - Called when the household is successfully deleted.
   * @param onFailure - Called when there is an error deleting the household.
   */
  fun deleteHouseholdById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Gets the user IDs for a list of users in the repository we want.
   *
   * @param users - The list of users of which we want the IDs.
   * @param callback - Called when the user IDs are successfully retrieved.
   */
  fun getUserIds(users: Set<String?>, callback: (Map<String, String>) -> Unit)

  /**
   * Gets the user emails for a list of users in the repository we want.
   *
   * @param userIds - The list of user IDs of which we want the emails.
   * @param callback - Called when the user emails are successfully retrieved.
   */
  fun getUserEmails(userIds: List<String>, callback: (Map<String, String>) -> Unit)

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
