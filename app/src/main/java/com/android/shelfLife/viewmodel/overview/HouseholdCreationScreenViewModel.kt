package com.android.shelfLife.viewmodel.overview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing household creation and editing.
 *
 * @property houseHoldRepository Repository for accessing household data.
 * @property foodItemRepository Repository for accessing food item data.
 * @property invitationRepository Repository for accessing invitation data.
 * @property userRepository Repository for accessing user data.
 */
@HiltViewModel
class HouseholdCreationScreenViewModel
@Inject
constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val foodItemRepository: FoodItemRepository,
    private val invitationRepository: InvitationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

  private val _emailList = MutableStateFlow<Set<String>>(emptySet())
  val emailList: StateFlow<Set<String>> = _emailList.asStateFlow()

  val householdToEdit: StateFlow<HouseHold?> =
      houseHoldRepository.householdToEdit.stateIn(
          viewModelScope, started = SharingStarted.Eagerly, null)

  val selectedHousehold =
      houseHoldRepository.selectedHousehold.stateIn(
          viewModelScope, started = SharingStarted.Eagerly, null)

  val households =
      houseHoldRepository.households.stateIn(
          viewModelScope, started = SharingStarted.Eagerly, emptyList())
  private val currentUser =
      userRepository.user.stateIn(viewModelScope, started = SharingStarted.Eagerly, null)

  private var finishedLoading = MutableStateFlow(false)

  // Fetch the list of members for the household to edit and set the email list
  init {
    viewModelScope.launch {
      val members = houseHoldRepository.getHouseholdMembers(householdToEdit.value?.uid ?: "")
      _emailList.value = userRepository.getUserEmails(members).values.toSet()
      finishedLoading.value = true
    }
  }

  /**
   * Adds an email to the email list.
   *
   * @param email The email to add.
   */
  private fun addEmail(email: String) {
    _emailList.value += email
  }

  /**
   * Removes an email from the email list.
   *
   * @param email The email to remove.
   */
  fun removeEmail(email: String) {
    _emailList.value -= email
  }

  /**
   * Checks if the new household name is invalid.
   *
   * @param householdName The household name to check.
   * @return True if the household name is invalid, false otherwise.
   */
  private fun isNewHouseholdNameIsInvalid(householdName: String): Boolean {
    return (householdName.isBlank() ||
        (houseHoldRepository.checkIfHouseholdNameExists(householdName) &&
            (householdToEdit.value == null || householdName != householdToEdit.value!!.name)))
  }

  /**
   * Gets a map of emails to user IDs.
   *
   * @param emails The set of emails.
   * @return A map of emails to user IDs.
   */
  private suspend fun getEmailToUserId(emails: Set<String>): Map<String, String> {
    return userRepository.getUserIds(emails)
  }

  /**
   * Attempts to add an email card, returning true if successful and false otherwise. This
   * encapsulates the logic for checking duplicates and blank inputs.
   *
   * @param emailInput The email input to add.
   * @return True if the email card was added, false otherwise.
   */
  fun tryAddEmailCard(emailInput: String): Boolean {
    val trimmedEmail = emailInput.trim()
    if (trimmedEmail.isNotBlank() && trimmedEmail !in _emailList.value) {
      addEmail(trimmedEmail)
      return true
    }
    return false
  }

  /**
   * Handles creating or updating a household when the user clicks "Save".
   *
   * @param householdName The name of the household.
   * @return False if household name is invalid, true if the operation succeeded.
   */
  suspend fun confirmHouseholdActions(householdName: String): Boolean {
    if (isNewHouseholdNameIsInvalid(householdName)) {
      return false
    }

    val editHousehold = householdToEdit.value
    if (editHousehold != null) {
      // Editing existing household
      val updatedHousehold = editHousehold.copy(name = householdName)
      val emailToUserIds = getEmailToUserId(_emailList.value)
      if (emailToUserIds.isNotEmpty()) {
        val oldUidList = updatedHousehold.members
        val uidList = _emailList.value.mapNotNull { emailToUserIds[it] }

        // Compare sizes to determine if we need to send invites or remove members
        when {
          oldUidList.size < uidList.size -> {
            updateHousehold(updatedHousehold.copy(members = uidList), shouldUpdateRepo = false)
          }
          oldUidList.size > uidList.size -> {
            updateHousehold(updatedHousehold.copy(members = uidList), shouldUpdateRepo = true)
          }
        }
        updateHousehold(updatedHousehold)
      }
    } else {
      // Creating new household
      addNewHousehold(householdName, _emailList.value)
      Log.d("HouseholdCreationScreen", "Added new household")
    }

    return true
  }

  /**
   * Adds a new household.
   *
   * @param householdName The name of the new household.
   * @param friendEmails The set of friend emails to invite.
   */
  private fun addNewHousehold(householdName: String, friendEmails: Set<String?> = emptySet()) {
    viewModelScope.launch {
      val user =
          currentUser.value
              ?: run {
                Log.e("HouseholdViewModel", "User not logged in")
                return@launch
              }

      val householdUid = houseHoldRepository.getNewUid()
      val household =
          HouseHold(
              householdUid, householdName, listOf(user.uid), emptyList(), emptyMap(), emptyMap())

      houseHoldRepository.addHousehold(household)
      userRepository.addHouseholdUID(household.uid)
      Log.d("HouseholdViewModel", "Adding new household: ${household.name}")
      houseHoldRepository.selectHousehold(household)
      userRepository.selectHousehold(household.uid)
      Log.d("HouseholdViewModel", "Selected new household: ${household.name}")
      foodItemRepository.getFoodItems(household.uid)

      if (friendEmails.isNotEmpty()) {
        val emailToUid = userRepository.getUserIds(friendEmails)
        val emailsNotFound = friendEmails.filter { it !in emailToUid.keys }
        if (emailsNotFound.isNotEmpty()) {
          Log.w("HouseholdViewModel", "Emails not found: $emailsNotFound")
        }
        emailToUid
            .filterKeys { it != user.email }
            .values
            .forEach { invitationRepository.sendInvitation(household, it) }
      }
    }
  }

  /**
   * Updates an existing household.
   *
   * @param household The household to update.
   * @param shouldUpdateRepo Whether to update the repository.
   */
  private fun updateHousehold(household: HouseHold, shouldUpdateRepo: Boolean = true) {
    viewModelScope.launch {
      val oldHousehold = houseHoldRepository.households.value.find { it.uid == household.uid }
      if (oldHousehold != null) {
        val newMemberUIDs = household.members.toSet() - oldHousehold.members.toSet()
        if (newMemberUIDs.isNotEmpty()) {
          for (uid in newMemberUIDs) {
            invitationRepository.sendInvitation(
                household = household,
                invitedUserID = uid,
            )
          }
        }
      }
      if (shouldUpdateRepo) {
        houseHoldRepository.updateHousehold(household)
        if (selectedHousehold.value == null || household.uid == selectedHousehold.value!!.uid) {
          houseHoldRepository.selectHousehold(household)
          userRepository.selectHousehold(household.uid)
          foodItemRepository.getFoodItems(household.uid)
        }
      }
    }
  }
}
