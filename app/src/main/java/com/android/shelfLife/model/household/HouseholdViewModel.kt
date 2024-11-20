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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HouseholdViewModel(
    private val repository: HouseHoldRepository,
    private val listFoodItemsViewModel: ListFoodItemsViewModel,
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

  val KEY = stringPreferencesKey("household_uid")

  /** Initializes the HouseholdViewModel by loading the list of households from the repository. */
  init {
    Log.d("HouseholdViewModel", "Initializing HouseholdViewModel")
    FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
      if (firebaseAuth.currentUser != null) {
        loadHouseholds()
      }
    }
  }

  /** Save the selected household UID to DataStore. */
  private suspend fun saveSelectedHouseholdUid(uid: String?) {
    Log.d("HouseholdViewModel", "Saving selected household UID: $uid")
    dataStore.edit { preferences -> preferences[KEY] = uid ?: "" }
  }

  /** Load the selected household UID from DataStore. */
  private fun loadSelectedHouseholdUid(callback: (String?) -> Unit) {
    viewModelScope.launch {
      val uid = dataStore.data.map { preferences -> preferences[KEY] }.first()
      callback(uid)
    }
  }

  fun setHouseholds(households: List<HouseHold>) {
    _households.value = households
  }

  /** Loads the list of households from the repository and updates the [_households] flow. */
  private fun loadHouseholds() {
    Log.d("HouseholdViewModel", "Loading households  ${Throwable().stackTrace[1]}")
    repository.getHouseholds(
        onSuccess = { householdList ->
          _households.value = householdList
          Log.d("HouseholdViewModel", "Households loaded successfully")
          Log.d("HouseholdViewModel", "Selected household: ${_selectedHousehold.value}")
          loadSelectedHouseholdUid { uid ->
            if (uid != null) {
              selectHousehold(householdList.find { it.uid == uid } ?: householdList.firstOrNull())
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
      _selectedHousehold.value = updatedHousehold
      Log.d("HouseholdViewModel", "Selected household updated: $updatedHousehold")
      listFoodItemsViewModel.setFoodItems(_selectedHousehold.value!!.foodItems)
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
      viewModelScope.launch {
        Log.d("HouseholdViewModel", "Saving selected household UID: ${household?.uid}")
        saveSelectedHouseholdUid(household?.uid)
      }
    }
    _selectedHousehold.value = household
    Log.d(
        "HouseholdViewModel",
        "Selected household: $household stacktrace: ${Throwable().stackTrace[1]}")
    household?.let { listFoodItemsViewModel.setFoodItems(it.foodItems) }
  }

  fun selectHouseholdToEdit(household: HouseHold?) {
    _householdToEdit.value = household
    household?.let {
      repository.getUserEmails(it.members) { uidToEmail -> _memberEmails.value = uidToEmail }
    }
  }

  fun getUserIdsByEmails(emails: List<String>, callback: (Map<String, String>) -> Unit) {
    repository.getUserIds(emails) { emailToUid -> callback(emailToUid) }
  }

  fun checkIfHouseholdNameExists(houseHoldName: String): Boolean {
    return _households.value.any { it.name == houseHoldName }
  }

  /**
   * Adds a new household to the repository and updates the household list.
   *
   * @param householdName - The name of the household to be added.
   */
  fun addNewHousehold(householdName: String, friendEmails: List<String> = emptyList()) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
      val householdUid = repository.getNewUid()
      val household = HouseHold(householdUid, householdName, emptyList(), emptyList())

      // Get user IDs corresponding to friend emails
      repository.getUserIds(friendEmails) { emailToUserId ->
        val emailsNotFound = friendEmails.filter { it !in emailToUserId.keys }
        if (emailsNotFound.isNotEmpty()) {
          Log.w("HouseholdViewModel", "Emails not found: $emailsNotFound")
        }
        val friendUserIds = emailToUserId.values.toList()
        val allMembers = friendUserIds.plus(currentUser.uid)
        val householdWithMembers = household.copy(members = allMembers)

        repository.addHousehold(
            householdWithMembers,
            onSuccess = { Log.d("HouseholdViewModel", "Household added successfully") },
            onFailure = { exception ->
              Log.e("HouseholdViewModel", "Error adding household: $exception")
            })
        // Update the list of households locally, this saves resources and ensures that
        // the UI is updated
        updateViewModelStateWithHousehold(householdWithMembers)
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
  fun updateHousehold(household: HouseHold) {
    repository.updateHousehold(
        household,
        onSuccess = { Log.d("HouseholdViewModel", "Household updated successfully") },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error updating household: $exception")
        })
    updateViewModelStateWithHousehold(household)
  }

  /**
   * Deletes a household by its unique ID and refreshes the household list.
   *
   * @param householdId - The unique ID of the household to delete.
   */
  fun deleteHouseholdById(householdId: String) {
    repository.deleteHouseholdById(
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
}
