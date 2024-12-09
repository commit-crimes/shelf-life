package com.android.shelfLife.viewmodel

import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.newInvitations.Invitation
import com.android.shelfLife.model.newInvitations.InvitationRepository
import com.android.shelfLife.model.user.UserRepository

class InvitationViewModel(
    private val invitationRepository: InvitationRepository,
    private val userRepo: UserRepository
) : ViewModel() {

  val invitations = invitationRepository.invitations

  suspend fun acceptInvitation(selectedInvitation: Invitation) {
    userRepo.deleteInvitationUID(selectedInvitation.invitationId)
    invitationRepository.acceptInvitation(invitation = selectedInvitation)
  }

  suspend fun declineInvitation(selectedInvitation: Invitation) {
    userRepo.deleteInvitationUID(selectedInvitation.invitationId)
    invitationRepository.declineInvitation(invitation = selectedInvitation)
  }
}
