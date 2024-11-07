package com.android.shelfLife.model.household

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
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
  val households: StateFlow<List<HouseHold>> = _households.asStateFlow()

  private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
  val selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold.asStateFlow()

  private val _householdToEdit = MutableStateFlow<HouseHold?>(null)
  val householdToEdit: StateFlow<HouseHold?> = _householdToEdit.asStateFlow()

  var finishedLoading = MutableStateFlow(false)

  /** Initializes the HouseholdViewModel by loading the list of households from the repository. */
  init {
    FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
      if (firebaseAuth.currentUser != null) {
        loadHouseholds()
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
          selectHousehold(householdList.firstOrNull()) // Default to the first household
          finishedLoading.value = true
        },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error loading households: $exception")
          finishedLoading.value = true
        })
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
            onSuccess = {
              loadHouseholds()
              Log.d("HouseholdViewModel", "Household added successfully")
            },
            onFailure = { exception ->
              Log.e("HouseholdViewModel", "Error adding household: $exception")
            })
      }
    } else {
      Log.e("HouseholdViewModel", "User not logged in")
    }
    loadHouseholds()
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
    loadHouseholds()
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
    loadHouseholds()
  }

  /**
   * Factory for creating a [HouseholdViewModel] with a constructor that takes a
   * [HouseHoldRepository] and a [ListFoodItemsViewModel].
   */
  fun addFoodItem(foodItem: FoodItem) {
    val selectedHousehold = selectedHousehold.value
    if (selectedHousehold != null) {
      updateHousehold(
          selectedHousehold.copy(foodItems = selectedHousehold.foodItems.plus(foodItem)))
    }
  }

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
}
