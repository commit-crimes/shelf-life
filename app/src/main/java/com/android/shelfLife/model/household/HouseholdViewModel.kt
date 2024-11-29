package com.android.shelfLife.model.household

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.invitations.InvitationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HouseholdViewModel(
    private val houseHoldRepository: HouseHoldRepository,
    private val listFoodItemsViewModel: ListFoodItemsViewModel,
    private val invitationRepository: InvitationRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
  private val _households = MutableStateFlow<List<HouseHold>>(emptyList())
  var households: StateFlow<List<HouseHold>> = _households.asStateFlow()

  private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
  var selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold.asStateFlow()

  private val _householdToEdit = MutableStateFlow<HouseHold?>(null)
  val householdToEdit: StateFlow<HouseHold?> = _householdToEdit.asStateFlow()

  private val _memberEmails = MutableStateFlow<Map<String, String>>(emptyMap())
  val memberEmails: StateFlow<Map<String, String>> = _memberEmails.asStateFlow()

  var finishedLoading = MutableStateFlow(false)

  private val KEY = stringPreferencesKey("household_uid")

  /** Initializes the HouseholdViewModel by loading the list of households from the repository. */
  init {
    Log.d("HouseholdViewModel", "Initializing HouseholdViewModel")
    FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
      if (firebaseAuth.currentUser != null) {
        loadHouseholds()
      }
    }
    viewModelScope.launch {
      householdToEdit.filterNotNull().collect { household ->
        houseHoldRepository.getUserEmails(household.members) { uidToEmail ->
          _memberEmails.value = uidToEmail
        }
      }
    }
  }

  /**
   * Sets the list of households to a new list of households. This is used for testing purposes
   * only.
   *
   * @param households - The new list of household.
   */
  fun setHouseholds(households: List<HouseHold>) {
    _households.value = households
  }

  /** Save the selected household UID to DataStore. */
  private fun saveSelectedHouseholdUid(uid: String?) {
    Log.d("HouseholdViewModel", "Saving selected household UID: $uid")
    viewModelScope.launch { dataStore.edit { preferences -> preferences[KEY] = uid ?: "" } }
  }

  /** Load the selected household UID from DataStore. */
  private fun loadSelectedHouseholdUid(callback: (String?) -> Unit) {
    viewModelScope.launch {
      val uid = dataStore.data.map { preferences -> preferences[KEY] }.first()
      callback(uid)
    }
  }

  /** Loads the list of households from the repository and updates the [_households] flow. */
  private fun loadHouseholds() {
    houseHoldRepository.getHouseholds(
        onSuccess = { householdList ->
          _households.value = householdList
          Log.d("HouseholdViewModel", "Households loaded successfully")
          Log.d("HouseholdViewModel", "Selected household: ${_selectedHousehold.value}")
          loadSelectedHouseholdUid { uid ->
            if (uid != null) {
              Log.d("HouseholdViewModel", "Selected household UID: $uid")
              selectHousehold(householdList.find { it.uid == uid } ?: householdList.firstOrNull())
            } else {
              selectHousehold(householdList.firstOrNull())
            }
            updateSelectedHousehold()
            finishedLoading.value = true
          }
        },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error loading households: $exception")
          finishedLoading.value = true
        })
  }

  /**
   * Updates the view model state with a new household.
   *
   * @param household - The household to update the view model state with.
   */
  private fun updateViewModelStateWithHousehold(household: HouseHold) {
    Log.d("HouseholdViewModel", "Updating view model state with household: $household")
    val currentHouseholds = _households.value
    val existingHousehold = currentHouseholds.find { it.uid == household.uid }

    // Update the households list
    _households.value =
        if (existingHousehold == null) {
          currentHouseholds.plus(household)
        } else {
          currentHouseholds.minus(existingHousehold).plus(household)
        }

    // Update the selected household if necessary
    if (_selectedHousehold.value == null) {
      Log.d("HouseholdViewModel", "Selected household is null")
      selectHousehold(_households.value.firstOrNull()) // Default to the first household
    } else {
      updateSelectedHousehold()
    }
  }

  /**
   * Updates the selected household with the latest data from the list of households using the uid.
   */
  private fun updateSelectedHousehold() {
    Log.d("HouseholdViewModel", "Updating selected household")
    _selectedHousehold.value?.let { selectedHousehold ->
      val updatedHousehold = _households.value.find { it.uid == selectedHousehold.uid }
      selectHousehold(updatedHousehold)
    }
  }

  /**
   * Selects a household and updates the selected household and the list of food items.
   *
   * @param household - The household to select.
   */
  fun selectHousehold(household: HouseHold?) {
    // Save the selected household UID to DataStore
    if (_selectedHousehold.value == null || _selectedHousehold.value!!.uid != household?.uid) {
      saveSelectedHouseholdUid(household?.uid)
    }
    _selectedHousehold.value = household
    household?.let { listFoodItemsViewModel.setFoodItems(it.foodItems) }
  }

  /**
   * Selects a household to edit and loads the list of member emails.
   *
   * @param household - The household to edit.
   */
  fun selectHouseholdToEdit(household: HouseHold?) {
    _householdToEdit.value = household
  }

  /**
   * Gets the user IDs corresponding to a list of emails.
   *
   * @param emails - The list of emails to get user IDs for.
   * @param callback - The callback to be invoked with the map of email to user ID.
   */
  fun getUserIdsByEmails(emails: Set<String>, callback: (Map<String, String>) -> Unit) {
    houseHoldRepository.getUserIds(emails) { emailToUid -> callback(emailToUid) }
  }

  /**
   * Checks if a household name already exists in the list of households.
   *
   * @param houseHoldName - The name of the household to check.
   * @return True if the household name already exists, false otherwise.
   */
  fun checkIfHouseholdNameExists(houseHoldName: String): Boolean {
    return _households.value.any { it.name == houseHoldName }
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
        houseHoldRepository.getUserIds(friendEmails) { emailToUserId ->
          val emailsNotFound = friendEmails.filter { it !in emailToUserId.keys }
          if (emailsNotFound.isNotEmpty()) {
            Log.w("HouseholdViewModel", "Emails not found: $emailsNotFound")
          }

          houseHoldRepository.addHousehold(
              household,
              onSuccess = {
                Log.d("HouseholdViewModel", "Household added successfully")
                // Send invitations to friends
                for (email in friendEmails.filter { it != currentUser.email }) {
                  invitationRepository.sendInvitation(
                      household = household,
                      invitedUserEmail = email!!,
                      onSuccess = { Log.d("HouseholdViewModel", "Invitation sent successfully") },
                      onFailure = { exception ->
                        Log.e("HouseholdViewModel", "Error sending invitation: $exception")
                      })
                }
                households.value.plus(household)
              },
              onFailure = { exception ->
                Log.e("HouseholdViewModel", "Error adding household: $exception")
              })
          // Update the list of households locally, this saves resources and ensures that
          // the UI is updated
          updateViewModelStateWithHousehold(household)
        }
      } else {
        // No friend emails, add household with current user only
        household = household.copy(members = listOf(currentUser.uid))
        houseHoldRepository.addHousehold(
            household,
            onSuccess = {
              Log.d("HouseholdViewModel", "Household added successfully")
              households.value.plus(household)
              loadHouseholds()
            },
            onFailure = { exception ->
              Log.e("HouseholdViewModel", "Error adding household: $exception")
            })
        // Update the list of households locally, this saves resources and ensures that
        // the UI is updated
        updateViewModelStateWithHousehold(household)
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
  fun updateHousehold(household: HouseHold, shouldUpdateRepo : Boolean = true) {
    val oldHousehold = households.value.find { it.uid == household.uid }
    if (oldHousehold != null) {
      if (oldHousehold.members != household.members) {
        val newMemberUids = household.members.toSet() - oldHousehold.members.toSet()
        if (newMemberUids.isNotEmpty()) {
          // Fetch emails for the new member UIDs
          houseHoldRepository.getUserEmails(newMemberUids.toList()) { uidToEmail ->
            for (uid in newMemberUids) {
              val email = uidToEmail[uid]
              if (email != null) {
                invitationRepository.sendInvitation(
                    household = household,
                    invitedUserEmail = email,
                    onSuccess = {
                      Log.d("HouseholdViewModel", "Invitation sent successfully to $email")
                    },
                    onFailure = { exception ->
                      Log.e("HouseholdViewModel", "Error sending invitation to $email: $exception")
                    })
              } else {
                Log.e("HouseholdViewModel", "No email found for UID: $uid")
              }
            }
          }
        }
      }
        if (shouldUpdateRepo) {
            houseHoldRepository.updateHousehold(
                household,
                onSuccess = { Log.d("HouseholdViewModel", "Household updated successfully") },
                onFailure = { exception ->
                Log.e("HouseholdViewModel", "Error updating household: $exception")
                })
        }
    } else {
      Log.e("HouseholdViewModel", "Old household not found for UID: ${household.uid}")
    }
  }

  /**
   * Deletes a household by its unique ID and refreshes the household list.
   *
   * @param householdId - The unique ID of the household to delete.
   */
  fun deleteHouseholdById(householdId: String) {
    houseHoldRepository.deleteHouseholdById(
        householdId,
        onSuccess = { Log.d("HouseholdViewModel", "Household deleted successfully") },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error deleting household: $exception")
        })
    // Delete household from the list of households
    _households.value
        .find { it.uid == householdId }
        ?.let { _households.value = _households.value.minus(it) }
    if (_selectedHousehold.value == null || householdId == _selectedHousehold.value!!.uid) {
      // If the deleted household was selected, deselect it
      selectHousehold(_households.value.firstOrNull())
    }
  }

  // TODO this is a bad way to update the food items, we need a plan to separate the food items from
  // the household
  fun addFoodItem(foodItem: FoodItem) {
    val selectedHousehold = _selectedHousehold.value
    if (selectedHousehold != null) {
      updateHousehold(
          selectedHousehold.copy(foodItems = selectedHousehold.foodItems.plus(foodItem)))
    }
  }

  fun editFoodItem(newFoodItem: FoodItem, oldFoodItem: FoodItem) {
    val selectedHousehold = _selectedHousehold.value
    if (selectedHousehold != null) {
      updateHousehold(
          selectedHousehold.copy(
              foodItems = selectedHousehold.foodItems.minus(oldFoodItem).plus(newFoodItem)))
    }
  }

  fun deleteFoodItem(foodItem: FoodItem) {
    val selectedHousehold = selectedHousehold.value
    if (selectedHousehold != null) {
      updateHousehold(
          selectedHousehold.copy(foodItems = selectedHousehold.foodItems.minus(foodItem)))
    }
  }

  fun deleteMultipleFoodItems(foodItems: List<FoodItem>) {
    val selectedHousehold = selectedHousehold.value
    if (selectedHousehold != null) {
      val updatedFoodItems = selectedHousehold.foodItems.minus(foodItems)
      updateHousehold(selectedHousehold.copy(foodItems = updatedFoodItems))
    }
  }

  fun deleteMember(memberUid: String) {
    val selectedHousehold = selectedHousehold.value
    if (selectedHousehold != null) {
      val updatedMembers = selectedHousehold.members.minus(memberUid)
      households.value.find { it.uid == selectedHousehold.uid }!!.copy(members = updatedMembers)
      val updatedHousehold = selectedHousehold.copy(members = updatedMembers)
      houseHoldRepository.updateHousehold(
          updatedHousehold,
          { Log.d("HouseholdViewModel", "Member deleted successfully") },
          { exception -> Log.e("HouseholdViewModel", "Error deleting member: $exception") })
      updateViewModelStateWithHousehold(updatedHousehold)
    }
  }

  /**
   * Factory for creating a [HouseholdViewModel] with a constructor that takes a
   * [HouseHoldRepository] and a [ListFoodItemsViewModel].
   */
  /*
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val firebaseFirestore = FirebaseFirestore.getInstance()
            val foodItemRepository = FoodItemRepositoryFirestore(firebaseFirestore)
            val listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
            val invitationRepository = InvitationRepositoryFirestore(firebaseFirestore)
            val repository = HouseholdRepositoryFirestore(firebaseFirestore)
            return HouseholdViewModel(repository, listFoodItemsViewModel, invitationRepository) as T
          }
        }

   */
}
