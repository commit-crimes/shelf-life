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
          Log.d("HouseholdViewModel", "Households loaded successfully")
          Log.d("HouseholdViewModel", "Selected household: ${_selectedHousehold.value}")
          if (_selectedHousehold.value == null) {
            selectHousehold(householdList.firstOrNull()) // Default to the first household
          }
          updateSelectedHousehold()
          finishedLoading.value = true
        },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error loading households: $exception")
          finishedLoading.value = true
        })
  }
  /**
   * Updates the selected household with the latest data from the list of households using the uid,
   * we may need to add another uid than the name.
   */
  private fun updateSelectedHousehold() {
    selectedHousehold.value?.let { selectedHousehold ->
      val updatedHousehold = _households.value.find { it.uid == selectedHousehold.uid }
      _selectedHousehold.value = updatedHousehold
      listFoodItemsViewModel.setFoodItems(_selectedHousehold.value!!.foodItems)
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
  }

  fun checkIfHouseholdNameExists(houseHoldName: String): Boolean {
    return _households.value.any { it.name == houseHoldName }
  }

  /**
   * Adds a new household to the repository and updates the household list.
   *
   * @param householdName - The name of the household to be added.
   */
  fun addNewHousehold(householdName: String) {
    val household = HouseHold(repository.getNewUid(), householdName, emptyList(), emptyList())
    repository.addHousehold(
        household,
        onSuccess = { Log.d("HouseholdViewModel", "Household added successfully") },
        onFailure = { exception ->
          Log.e("HouseholdViewModel", "Error adding household: $exception")
        })
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
              selectedHousehold.copy(foodItems = selectedHousehold.foodItems.minus(oldFoodItem).plus(newFoodItem)))
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
}
