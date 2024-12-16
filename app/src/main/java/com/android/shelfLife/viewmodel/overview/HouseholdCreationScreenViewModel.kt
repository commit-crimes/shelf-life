package com.android.shelfLife.viewmodel.overview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing household creation and editing.
 *
 * This ViewModel provides logic for:
 * - Creating a new household
 * - Editing an existing household
 * - Managing household members and their invitations
 * - Updating the household repository
 *
 * @param houseHoldRepository Repository for managing household data.
 * @param foodItemRepository Repository for managing food items associated with households.
 * @param invitationRepository Repository for managing household invitations.
 * @param userRepository Repository for managing user-related data.
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

  /** The list of email addresses of members in the household being created or edited. */
  private val _emailList = MutableStateFlow<Set<String>>(emptySet())
  val emailList: StateFlow<Set<String>> = _emailList.asStateFlow()

  /** The household currently being edited, if any. */
  val householdToEdit: StateFlow<HouseHold?> =
    houseHoldRepository.householdToEdit.stateIn(
      viewModelScope, started = SharingStarted.Eagerly, null)

  /** The currently selected household. */
  val selectedHousehold: StateFlow<HouseHold?> =
    houseHoldRepository.selectedHousehold.stateIn(
      viewModelScope, started = SharingStarted.Eagerly, null)

  /** The list of all households associated with the user. */
  val households: StateFlow<List<HouseHold>> =
    houseHoldRepository.households.stateIn(
      viewModelScope, started = SharingStarted.Eagerly, emptyList())

  /** The current user's information. */
  private val currentUser =
    userRepository.user.stateIn(viewModelScope, started = SharingStarted.Eagerly, null)

  /** Tracks whether the household data has finished loading. */
  private val finishedLoading = MutableStateFlow(false)

  init {
    viewModelScope.launch {
      // Fetch members of the household being edited and populate the email list
      val members = houseHoldRepository.getHouseholdMembers(householdToEdit.value?.uid ?: "")
      _emailList.value = userRepository.getUserEmails(members).values.toSet()
      finishedLoading.value = true
    }
  }

  /**
   * Adds an email address to the household member list.
   *
   * @param email The email address to add.
   */
  private fun addEmail(email: String) {
    _emailList.value += email
  }

  /**
   * Removes an email address from the household member list.
   *
   * @param email The email address to remove.
   */
  fun removeEmail(email: String) {
    _emailList.value -= email
  }

  /**
   * Validates the household name to ensure it is unique and not blank.
   *
   * @param householdName The name to validate.
   * @return True if the name is invalid, false otherwise.
   */
  private fun isNewHouseholdNameIsInvalid(householdName: String): Boolean {
    return (householdName.isBlank() ||
            (houseHoldRepository.checkIfHouseholdNameExists(householdName) &&
                    (householdToEdit.value == null || householdName != householdToEdit.value!!.name)))
  }

  /**
   * Maps a set of email addresses to their corresponding user IDs.
   *
   * @param emails The set of email addresses.
   * @return A map of email addresses to user IDs.
   */
  private suspend fun getEmailToUserId(emails: Set<String>): Map<String, String> {
    return userRepository.getUserIds(emails)
  }

  /**
   * Attempts to add an email address to the list of household members.
   *
   * @param emailInput The email address to add.
   * @return True if the email was successfully added, false otherwise.
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
   * @param householdName The name of the household to save.
   * @return False if the household name is invalid, true if the operation succeeds.
   */
  suspend fun confirmHouseholdActions(householdName: String): Boolean {
    if (isNewHouseholdNameIsInvalid(householdName)) {
      return false
    }

    val editHousehold = householdToEdit.value
    if (editHousehold != null) {
      // Editing an existing household
      val updatedHousehold = editHousehold.copy(name = householdName)
      val emailToUserIds = getEmailToUserId(_emailList.value)
      if (emailToUserIds.isNotEmpty()) {
        val oldUidList = updatedHousehold.members
        val uidList = _emailList.value.mapNotNull { emailToUserIds[it] }

        // Determine if members were added or removed and update accordingly
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
      // Creating a new household
      addNewHousehold(householdName, _emailList.value)
      Log.d("HouseholdCreationScreen", "Added new household")
    }

    return true
  }

  /**
   * Creates a new household and sends invitations to the provided email addresses.
   *
   * @param householdName The name of the new household.
   * @param friendEmails The set of email addresses to invite.
   */
  private fun addNewHousehold(householdName: String, friendEmails: Set<String?> = emptySet()) {
    viewModelScope.launch {
      val user = currentUser.value
        ?: run {
          Log.e("HouseholdViewModel", "User not logged in")
          return@launch
        }

      val householdUid = houseHoldRepository.getNewUid()
      val household = HouseHold(householdUid, householdName, listOf(user.uid), emptyList(), emptyMap(), emptyMap())

      houseHoldRepository.addHousehold(household)
      userRepository.addHouseholdUID(household.uid)
      houseHoldRepository.selectHousehold(household)
      userRepository.selectHousehold(household.uid)

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
   * Updates an existing household and sends invitations to new members if needed.
   *
   * @param household The updated household object.
   * @param shouldUpdateRepo Whether to update the repository after modifying the household.
   */
  private fun updateHousehold(household: HouseHold, shouldUpdateRepo: Boolean = true) {
    viewModelScope.launch {
      val oldHousehold = houseHoldRepository.households.value.find { it.uid == household.uid }
      if (oldHousehold != null) {
        val newMemberUIDs = household.members.toSet() - oldHousehold.members.toSet()
        if (newMemberUIDs.isNotEmpty()) {
          newMemberUIDs.forEach { uid ->
            invitationRepository.sendInvitation(household, uid)
          }
        }
      }
      if (shouldUpdateRepo) {
        houseHoldRepository.updateHousehold(household)
        if (selectedHousehold.value?.uid == household.uid) {
          houseHoldRepository.selectHousehold(household)
          userRepository.selectHousehold(household.uid)
          foodItemRepository.getFoodItems(household.uid)
        }
      }
    }
  }
}