package com.android.shelfLife.model.household

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.invitations.Invitation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HouseholdViewModel(
    private val repository: HouseHoldRepository,
    private val listFoodItemsViewModel: ListFoodItemsViewModel
) : ViewModel() {
  private val _households = MutableStateFlow<List<HouseHold>>(emptyList())
  var households: StateFlow<List<HouseHold>> = _households.asStateFlow()

  private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
  var selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold.asStateFlow()

  private val _householdToEdit = MutableStateFlow<HouseHold?>(null)
  val householdToEdit: StateFlow<HouseHold?> = _householdToEdit.asStateFlow()

  private val _memberEmails = MutableStateFlow<Map<String, String>>(emptyMap())
  val memberEmails: StateFlow<Map<String, String>> = _memberEmails.asStateFlow()

  private val _invitations = MutableStateFlow<List<Invitation>>(emptyList())
  val invitations: StateFlow<List<Invitation>> = _invitations.asStateFlow()

  var finishedLoading = MutableStateFlow(false)

  /** Initializes the HouseholdViewModel by loading the list of households from the repository. */
  init {
    FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
      if (firebaseAuth.currentUser != null) {
        loadHouseholds()
        loadInvitations()
      }
    }
  }

  fun setHouseholds(households: List<HouseHold>) {
    _households.value = households
  }

  /** Loads the list of households from the repository and updates the [_households] flow. */
  private fun loadHouseholds() {
    repository.getHouseholds(
        onSuccess = { householdList ->
          _households.value = householdList
          Log.d("HouseholdViewModel", "Households loaded successfully")
          Log.d("HouseholdViewModel", "Selected household: ${_selectedHousehold.value}")
          if (_selectedHousehold.value == null) {
            selectHousehold(householdList.firstOrNull()) // Default to the first household
          } else {
            updateSelectedHousehold()
          }
          finishedLoading.value = true
        },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error loading households: $exception")
          finishedLoading.value = true
        })
  }

  private fun updateViewModelStateWithHousehold(household: HouseHold) {
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
      selectHousehold(_households.value.firstOrNull()) // Default to the first household
    } else {
      updateSelectedHousehold()
    }
  }

  /**
   * Updates the selected household with the latest data from the list of households using the uid,
   * we may need to add another uid than the name.
   */
  private fun updateSelectedHousehold() {
    selectedHousehold.value?.let { selectedHousehold ->
      val updatedHousehold = _households.value.find { it.uid == selectedHousehold.uid }
      if (updatedHousehold != null) {
        _selectedHousehold.value = updatedHousehold
        listFoodItemsViewModel.setFoodItems(_selectedHousehold.value!!.foodItems)
      }
    }
  }

  /**
   * Selects a household and updates the selected household and the list of food items.
   *
   * @param household - The household to select.
   */
  fun selectHousehold(household: HouseHold?) {
    _selectedHousehold.value = household
    household?.let { listFoodItemsViewModel.setFoodItems(it.foodItems) }
  }

  fun selectHouseholdToEdit(household: HouseHold?) {
    _householdToEdit.value = household
    household?.let {
      repository.getUserEmails(it.members) { uidToEmail -> _memberEmails.value = uidToEmail }
    }
  }

  fun getUserIdsByEmails(emails: Set<String>, callback: (Map<String, String>) -> Unit) {
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
  fun addNewHousehold(householdName: String, friendEmails: Set<String?> = emptySet()) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
      val householdUid = repository.getNewUid()
      var household = HouseHold(householdUid, householdName, emptyList(), emptyList())

      if (friendEmails.isNotEmpty()) { // Corrected condition
        repository.getUserIds(friendEmails) { emailToUserId ->
          val emailsNotFound = friendEmails.filter { it !in emailToUserId.keys }
          if (emailsNotFound.isNotEmpty()) {
            Log.w("HouseholdViewModel", "Emails not found: $emailsNotFound")
          }
          val friendUserIds = emailToUserId.values.toList()
          household = household.copy(members = friendUserIds.plus(currentUser.uid))

          repository.addHousehold(
              household,
              onSuccess = {
                Log.d("HouseholdViewModel", "Household added successfully")
                // Send invitations to friends
                for (email in friendEmails.filter { it != currentUser.email }) {
                  sendInvitation(household, email!!)
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
        repository.addHousehold(
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
    val selectedHousehold = selectedHousehold.value
    if (selectedHousehold != null) {
      updateHousehold(
          selectedHousehold.copy(foodItems = selectedHousehold.foodItems.plus(foodItem)))
    }
  }

  fun editFoodItem(newFoodItem: FoodItem, oldFoodItem: FoodItem) {
    val selectedHousehold = selectedHousehold.value
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

  /**
   * Factory for creating a [HouseholdViewModel] with a constructor that takes a
   * [HouseHoldRepository] and a [ListFoodItemsViewModel].
   */
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val firebaseFirestore = FirebaseFirestore.getInstance()
            val foodItemRepository = FoodItemRepositoryFirestore(firebaseFirestore)
            val listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
            val repository = HouseholdRepositoryFirestore(firebaseFirestore)
            return HouseholdViewModel(repository, listFoodItemsViewModel) as T
          }
        }
  }

  /** Loads the list of invitations from the repository and updates the [_invitations] flow. */
  private fun loadInvitations() {
    repository.getInvitations(
        onSuccess = { invitationList -> _invitations.value = invitationList },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error loading invitations: $exception")
        })
  }

  private fun sendInvitation(household: HouseHold, invitedUserEmail: String) {
    repository.sendInvitation(
        household = household,
        invitedUserEmail = invitedUserEmail,
        onSuccess = { Log.d("HouseholdViewModel", "Invitation sent successfully") },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error sending invitation: $exception")
        })
  }

  fun acceptInvitation(invitation: Invitation) {
    repository.acceptInvitation(
        invitation,
        onSuccess = {
          Log.d("HouseholdViewModel", "Invitation accepted")
          invitations.value.minus(invitation)
          // refresh invitations
          loadInvitations()
          // refresh households
          loadHouseholds()
        },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error accepting invitation: $exception")
        })
  }

  fun declineInvitation(invitation: Invitation) {
    repository.declineInvitation(
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
