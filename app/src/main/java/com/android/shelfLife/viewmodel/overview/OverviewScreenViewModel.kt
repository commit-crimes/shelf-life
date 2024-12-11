package com.android.shelfLife.viewmodel.overview

import android.content.Context
import android.util.Log
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OverviewScreenViewModel
@Inject
constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val listFoodItemsRepository: FoodItemRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
  private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
  val selectedFilters = _selectedFilters.asStateFlow()

  private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
  val drawerState = _drawerState.asStateFlow()

  private val _multipleSelectedFoodItems = MutableStateFlow<List<FoodItem>>(emptyList())
  val multipleSelectedFoodItems: StateFlow<List<FoodItem>> =
      _multipleSelectedFoodItems.asStateFlow()

  val finishedLoading = MutableStateFlow(false)

  val households = houseHoldRepository.households
  val selectedHousehold = houseHoldRepository.selectedHousehold
  val foodItems = listFoodItemsRepository.foodItems

    private val _filteredFoodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val filteredFoodItems = _filteredFoodItems.asStateFlow()

  private var FILTERS =
      mapOf(
          "Dairy" to FoodCategory.DAIRY,
          "Meat" to FoodCategory.MEAT,
          "Fish" to FoodCategory.FISH,
          "Fruit" to FoodCategory.FRUIT,
          "Vegetables" to FoodCategory.VEGETABLE,
          "Grain" to FoodCategory.GRAIN,
          "Beverage" to FoodCategory.BEVERAGE,
          "Snack" to FoodCategory.SNACK,
          "Other" to FoodCategory.OTHER)

  var filters = FILTERS.keys.toList()

  private var _query = MutableStateFlow<String>("")
  val query = _query.asStateFlow()

  init {
    Log.d("OverviewScreenViewModel", "Init")
      //TODO ("Filter food items")
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

  fun changeQuery(newQuery: String) {
    _query.value = newQuery
    filterFoodItems()
  }

  fun filterFoodItems() {
    foodItems.value.filter { item ->
      item.foodFacts.name.contains(_query.value, ignoreCase = true) &&
          (_selectedFilters.value.isEmpty()) ||
          _selectedFilters.value.contains(item.foodFacts.category.name)
    }
  }
}
