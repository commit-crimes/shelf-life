package com.android.shelfLife.model.creationScreen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newInvitations.InvitationRepository
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreationScreenViewModel(
    initialEmails: Set<String> = emptySet(),
    private val houseHoldRepository: HouseHoldRepository,
    private val invitationRepository: InvitationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
  private val _emailList = MutableStateFlow<Set<String>>(initialEmails)
  val emailList: StateFlow<Set<String>> = _emailList.asStateFlow()

  val householdToEdit = houseHoldRepository.householdToEdit

  var finishedLoading = MutableStateFlow(false)

  // Fetch the list of members for the household to edit and set the email list
  init {
    viewModelScope.launch {
      val members = houseHoldRepository.getHouseholdMembers(householdToEdit.value?.uid ?: "")
      userRepository.getUserEmails(members) { uidToEmail ->
        _emailList.value = uidToEmail.values.toSet()
      }
      finishedLoading.value = true
    }
  }

  /**
   * Sets the list of emails to the provided list. FOR TESTING PURPOSES ONLY.
   *
   * @param emails - The list of emails to set.
   */
  fun setEmails(emails: Set<String>) {
    _emailList.value = emails
  }

  fun addEmail(email: String) {
    _emailList.value += email
  }

  fun removeEmail(email: String) {
    _emailList.value -= email
  }

  /**
   * Gets the user IDs corresponding to a list of emails.
   *
   * @param emails - The list of emails to get user IDs for.
   * @param callback - The callback to be invoked with the map of email to user ID.
   */
  fun getUserIdsByEmails(emails: Set<String>, callback: (Map<String, String>) -> Unit) {
    userRepository.getUserIds(emails) { emailToUid -> callback(emailToUid) }
  }

  fun newHouseholdNameIsInvalid(householdName: String): Boolean {
    return (householdName.isBlank() ||
        houseHoldRepository.checkIfHouseholdNameExists(householdName) &&
            (householdToEdit.value == null || householdName != householdToEdit.value!!.name))
  }

  /**
   * Adds a new household to the repository and updates the household list.
   *
   * @param householdName - The name of the household to be added.
   */
  fun addNewHousehold(householdName: String, friendEmails: Set<String?> = emptySet()) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
      val householdUid = houseHoldRepository.getNewUid()
      var household = HouseHold(householdUid, householdName, listOf(currentUser.uid), emptyList())

      if (friendEmails.isNotEmpty()) { // Corrected condition
        userRepository.getUserIds(friendEmails) { emailToUserId ->
          val emailsNotFound = friendEmails.filter { it !in emailToUserId.keys }
          if (emailsNotFound.isNotEmpty()) {
            Log.w("HouseholdViewModel", "Emails not found: $emailsNotFound")
          }
          viewModelScope.launch {
            houseHoldRepository.addHousehold(household)
            for (user in friendEmails.filter { it != currentUser.email }) {
              invitationRepository.sendInvitation(household = household, invitedUserID = user!!)
            }
          }
        }
      } else {
        // No friend emails, add household with current user only
        household = household.copy(members = listOf(currentUser.uid))
        viewModelScope.launch { houseHoldRepository.addHousehold(household) }
      }
    } else {
      Log.e("HouseholdViewModel", "User not logged in")
    }
  }

  /**
   * Updates an existing household in the repository and refreshes the household list.
   *
   * @param household - The updated household.
   */
  fun updateHousehold(household: HouseHold, shouldUpdateRepo: Boolean = true) {
    val oldHousehold = houseHoldRepository.households.value.find { it.uid == household.uid }
    if (oldHousehold != null) {
      if (oldHousehold.members != household.members) {
        val newMemberUids = household.members.toSet() - oldHousehold.members.toSet()
        if (newMemberUids.isNotEmpty()) {
          for (uid in newMemberUids) {
            invitationRepository.sendInvitation(
                household = household,
                invitedUserID = uid,
            )
          }
        }
      }
    }
    if (shouldUpdateRepo) {
      viewModelScope.launch { houseHoldRepository.updateHousehold(household) }
    }
  }
}
