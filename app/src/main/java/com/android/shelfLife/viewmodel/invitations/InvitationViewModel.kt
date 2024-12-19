package com.android.shelfLife.viewmodel.invitations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class InvitationViewModel
@Inject
constructor(
    private val invitationRepository: InvitationRepository,
    private val userRepo: UserRepository,
    private val houseHoldRepo: HouseHoldRepository
) : ViewModel() {

  private val _invitations = MutableStateFlow<List<Invitation>>(emptyList())
  val invitations: StateFlow<List<Invitation>> = _invitations

  init {
    viewModelScope.launch {
      userRepo.invitations.collect { invitationUIDs ->
        _invitations.value = invitationRepository.getInvitationsBatch(invitationUIDs)
      }
    }
  }

  suspend fun acceptInvitation(selectedInvitation: Invitation) {
    userRepo.deleteInvitationUID(selectedInvitation.invitationId)
    invitationRepository.acceptInvitation(selectedInvitation)
    userRepo.addCurrentUserToHouseHold(
        selectedInvitation.householdId, selectedInvitation.invitedUserId)
    houseHoldRepo.getHousehold(selectedInvitation.householdId)
    Log.d("InvitationViewModel", "before adding new household to user : ${userRepo.user.value}")
    refreshInvitations()
  }

  suspend fun declineInvitation(selectedInvitation: Invitation) {
    userRepo.deleteInvitationUID(selectedInvitation.invitationId)
    invitationRepository.declineInvitation(selectedInvitation)
    refreshInvitations()
  }

  internal suspend fun refreshInvitations() {
    val invitationUIDs = userRepo.invitations.value
    if (invitationUIDs.isNotEmpty()) {
      try {
        _invitations.value = invitationRepository.getInvitationsBatch(invitationUIDs)
      } catch (e: Exception) {
        Log.e("InvitationViewModel", "Error while refreshing invitations", e)
        _invitations.value = emptyList()
      }
    } else {
      _invitations.value = emptyList()
    }
  }
}
