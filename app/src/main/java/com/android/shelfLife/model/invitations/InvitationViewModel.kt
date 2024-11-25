package com.android.shelfLife.model.invitations

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.household.HouseHold
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InvitationViewModel(private val invitationRepository: InvitationRepository) : ViewModel() {

  private val _invitations = MutableStateFlow<List<Invitation>>(emptyList())
  val invitations: StateFlow<List<Invitation>> = _invitations.asStateFlow()

  init {
    FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
      if (firebaseAuth.currentUser != null) {
        loadInvitations()
      }
    }
  }

  /** Loads the list of invitations from the repository and updates the [_invitations] flow. */
  private fun loadInvitations() {
    invitationRepository.getInvitations(
        onSuccess = { invitationList -> _invitations.value = invitationList },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error loading invitations: $exception")
        })
  }

    /**
     * Sends an invitation to a user to join a household.
     *
     * @param household The household to invite the user to.
     * @param invitedUserEmail The email of the user to invite.
     */
    private fun sendInvitation(household: HouseHold, invitedUserEmail: String) {
    invitationRepository.sendInvitation(
        household = household,
        invitedUserEmail = invitedUserEmail,
        onSuccess = { Log.d("HouseholdViewModel", "Invitation sent successfully") },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error sending invitation: $exception")
        })
  }

    /**
     * Accepts an invitation.
     *
     * @param invitation The invitation to accept.
     */
    fun acceptInvitation(invitation: Invitation) {
    invitationRepository.acceptInvitation(
        invitation,
        onSuccess = {
          Log.d("HouseholdViewModel", "Invitation accepted")
          invitations.value.minus(invitation)
          // refresh invitations
          loadInvitations()
        },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error accepting invitation: $exception")
        })
  }

    /**
     * Declines an invitation.
     *
     * @param invitation The invitation to decline.
     */
    fun declineInvitation(invitation: Invitation) {
    invitationRepository.declineInvitation(
        invitation,
        onSuccess = {
          Log.d("HouseholdViewModel", "Invitation declined")
          // Refresh invitations
          invitations.value.minus(invitation)
          loadInvitations()
        },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error declining invitation: $exception")
        })
  }
}
