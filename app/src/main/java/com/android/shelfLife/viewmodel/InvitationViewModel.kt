package com.android.shelfLife.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newInvitations.Invitation
import com.android.shelfLife.model.newInvitations.InvitationRepository
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel
@Inject
constructor(
  private val invitationRepository: InvitationRepository,
  private val userRepo: UserRepository,
  private val houseHoldRepo: HouseHoldRepository,
) : ViewModel() {

  private val _invitations = MutableStateFlow<List<Invitation>>(emptyList())
  val invitations: StateFlow<List<Invitation>> = _invitations

  init {
    viewModelScope.launch {
      val invitationList = mutableListOf<Invitation>()
      userRepo.invitations.collect { invitationUIDs ->
        invitationList.clear()
        invitationUIDs.forEach { uid ->
          try {
            val invitation = invitationRepository.getInvitation(uid)
            if (invitation != null) {
              invitationList.add(invitation)
            } else {
              Log.e("InvitationViewModel", "Error getting that invitation uid")
            }
          } catch (e: Exception) {
            Log.e("InvitationViewModel", "Error getting invitation", e)
          }
        }
        _invitations.value = invitationList
      }
    }
  }

  suspend fun acceptInvitation(selectedInvitation: Invitation) {
    userRepo.deleteInvitationUID(selectedInvitation.invitationId)
    invitationRepository.acceptInvitation(selectedInvitation)
    userRepo.addCurrentUserToHouseHold(selectedInvitation.householdId
      ,selectedInvitation.invitedUserId)
    Log.d("InvitationViewModel", "before adding new household to user : ${userRepo.user.value}")
    refreshInvitations()
  }

  suspend fun declineInvitation(selectedInvitation: Invitation) {
    userRepo.deleteInvitationUID(selectedInvitation.invitationId)
    invitationRepository.declineInvitation(selectedInvitation)
    refreshInvitations()
  }

  private suspend fun refreshInvitations() {
    val invitationUIDs = userRepo.invitations.value
    if (invitationUIDs.isNotEmpty()) {
      try {
        val querySnapshot = invitationRepository.getInvitationsBatch(invitationUIDs)
        val updatedInvitations = querySnapshot.documents.mapNotNull { doc ->
          invitationRepository.convertToInvitation(doc)
        }
        _invitations.value = updatedInvitations
      } catch (e: Exception) {
        Log.e("InvitationViewModel", "Error while refreshing invitations", e)
      }
    } else {
      _invitations.value = emptyList()
    }
  }
}