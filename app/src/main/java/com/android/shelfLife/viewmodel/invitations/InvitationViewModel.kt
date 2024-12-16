package com.android.shelfLife.viewmodel.invitations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing user invitations to households.
 *
 * Handles retrieving, accepting, and declining invitations. Integrates with the InvitationRepository
 * and UserRepository to manage the state and update user data accordingly.
 *
 * @param invitationRepository Repository for managing invitation-related operations.
 * @param userRepo Repository for managing user-related data and operations.
 */
@HiltViewModel
class InvitationViewModel
@Inject
constructor(
  private val invitationRepository: InvitationRepository,
  private val userRepo: UserRepository
) : ViewModel() {

  // StateFlow to hold the list of invitations for the user
  private val _invitations = MutableStateFlow<List<Invitation>>(emptyList())
  val invitations: StateFlow<List<Invitation>> = _invitations

  init {
    // Observe the user's invitations and fetch details for each invitation
    viewModelScope.launch {
      userRepo.invitations.collect { invitationUIDs ->
        _invitations.value = invitationRepository.getInvitationsBatch(invitationUIDs)
      }
    }
  }

  /**
   * Accepts an invitation and updates the user's associated household.
   *
   * @param selectedInvitation The invitation to be accepted.
   */
  suspend fun acceptInvitation(selectedInvitation: Invitation) {
    // Remove the invitation ID from the user's list
    userRepo.deleteInvitationUID(selectedInvitation.invitationId)

    // Update the invitation status in the repository
    invitationRepository.acceptInvitation(selectedInvitation)

    // Add the current user to the household associated with the invitation
    userRepo.addCurrentUserToHouseHold(
      selectedInvitation.householdId,
      selectedInvitation.invitedUserId
    )

    Log.d("InvitationViewModel", "Added new household to user: ${userRepo.user.value}")

    // Refresh the invitations after accepting one
    refreshInvitations()
  }

  /**
   * Declines an invitation and removes it from the user's list of invitations.
   *
   * @param selectedInvitation The invitation to be declined.
   */
  suspend fun declineInvitation(selectedInvitation: Invitation) {
    // Remove the invitation ID from the user's list
    userRepo.deleteInvitationUID(selectedInvitation.invitationId)

    // Update the invitation status in the repository
    invitationRepository.declineInvitation(selectedInvitation)

    // Refresh the invitations after declining one
    refreshInvitations()
  }

  /**
   * Refreshes the list of invitations for the current user.
   *
   * Fetches updated invitation data from the repository based on the user's invitation IDs.
   * If there are no invitations, sets the invitations state to an empty list.
   */
  private suspend fun refreshInvitations() {
    val invitationUIDs = userRepo.invitations.value
    if (invitationUIDs.isNotEmpty()) {
      try {
        // Fetch updated invitations batch and update the state
        _invitations.value = invitationRepository.getInvitationsBatch(invitationUIDs)
      } catch (e: Exception) {
        // Log any errors encountered while refreshing invitations
        Log.e("InvitationViewModel", "Error while refreshing invitations", e)
      }
    } else {
      // No invitations to display
      _invitations.value = emptyList()
    }
  }
}