package com.android.shelfLife.viewmodel.overview

import android.util.Log
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OverviewScreenViewModel(
    private val houseHoldRepository: HouseHoldRepository,
    private val listFoodItemsRepository: FoodItemRepository,
    private val userRepository: UserRepository
) : ViewModel() {
  private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
  val selectedFilters = _selectedFilters.asStateFlow()

  private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
  val drawerState = _drawerState.asStateFlow()

  private val _multipleSelectedFoodItems = MutableStateFlow<List<FoodItem>>(emptyList())
  val multipleSelectedFoodItems: StateFlow<List<FoodItem>> =
      _multipleSelectedFoodItems.asStateFlow()

  val finishedLoading = MutableStateFlow(false)

  val selectedHousehold = userRepository.selectedHousehold
  val households = houseHoldRepository.households
  val foodItems = listFoodItemsRepository.foodItems

  val filters = listOf("Dairy", "Meat", "Fish", "Fruit", "Vegetables", "Bread", "Canned")

  /**
   * Initializes the OverviewScreenViewModel by loading the list of households from the repository.
   */
  init {
    Log.d("OverviewScreenViewModel", "Initialized")
    FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
      if (firebaseAuth.currentUser != null) {
        Log.d("OverviewScreenViewModel", "User logged in, loading....")
      }
    }
  }

  /** Loads the list of households from the repository and updates the [_households] flow. */
  private fun loadHouseholds() {
    viewModelScope.launch {
      userRepository.user.value?.let { user ->
        houseHoldRepository.initializeHouseholds(user.householdUIDs, user.selectedHouseholdUID!!)
        userRepository.selectHousehold(
            households.value.find { it.uid == user.selectedHouseholdUID }
                ?: households.value.firstOrNull())
      }
      Log.d("OverviewScreenViewModel", "Households loaded")
      finishedLoading.value = true
    }
  }

  /**
   * Selects a household to edit
   *
   * @param household - The household to edit.
   */
  fun selectHouseholdToEdit(household: HouseHold?) {
    houseHoldRepository.selectHouseholdToEdit(household)
  }

  fun deleteMultipleFoodItems(foodItems: List<FoodItem>) {
    val selectedHousehold = selectedHousehold.value
    if (selectedHousehold != null) {

      viewModelScope.launch {
        foodItems.forEach { listFoodItemsRepository.updateFoodItem(selectedHousehold.uid, it) }
      }
    }
  }

  /**
   * Toggles the filter on or off
   *
   * @param filter The filter to toggle
   */
  fun toggleFilter(filter: String) {
    if (_selectedFilters.value.contains(filter)) {
      _selectedFilters.update { it - filter }
    } else {
      _selectedFilters.update { it + filter }
    }
  }

  /** Selects multiple FoodItem documents for bulk actions */
  fun selectMultipleFoodItems(foodItem: FoodItem) {
    if (_multipleSelectedFoodItems.value.contains(foodItem)) {
      _multipleSelectedFoodItems.value = _multipleSelectedFoodItems.value.minus(foodItem)
    } else {
      _multipleSelectedFoodItems.value = _multipleSelectedFoodItems.value.plus(foodItem)
    }
  }

  fun clearMultipleSelectedFoodItems() {
    _multipleSelectedFoodItems.value = emptyList()
  }

  fun editFoodItem(newFoodItem: FoodItem, oldFoodItem: FoodItem) {
    val selectedHousehold = selectedHousehold.value
    if (selectedHousehold != null) {
      viewModelScope.launch {
        listFoodItemsRepository.updateFoodItem(selectedHousehold.uid, newFoodItem)
      }
    }
  }

  /** Selects a FoodItem document for individual view */
  fun selectFoodItem(foodItem: FoodItem?) {
    listFoodItemsRepository.selectFoodItem(foodItem)
  }
}
