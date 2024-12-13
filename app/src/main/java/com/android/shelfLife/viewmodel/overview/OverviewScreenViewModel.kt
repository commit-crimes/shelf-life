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
import com.android.shelfLife.model.newFoodItem.FoodStatus
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _query = MutableStateFlow<String>("")
    val query = _query.asStateFlow()

    // Automatically filtered list of food items based on selected filters and query
    val filteredFoodItems =
        combine(foodItems, selectedFilters, query) { foods, currentFilters, currentQuery ->
            foods.filter { item ->
                // Check filters:
                // Matches if no filters are selected OR if the item's category is one of the selected
                // filters
                val matchesFilters =
                    currentFilters.isEmpty() ||
                            currentFilters.any { filter -> item.foodFacts.category == FILTERS[filter] }

                // Check query:
                // Matches if the query is empty OR if the item's name contains the query
                val matchesQuery =
                    currentQuery.isEmpty() ||
                            item.foodFacts.name.contains(currentQuery, ignoreCase = true)

                matchesFilters && matchesQuery
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList())

  /**
   * Initializes the OverviewScreenViewModel by loading the list of households from the repository.
   */
  init {
    checkItemStatus()
    Log.d("OverviewScreenViewModel", "Init")
  }

  suspend fun addCustomHouseholdForTesting() {
    val houseHold =
        HouseHold(
            "testHouseHoldUid",
            "testHouseHoldName",
            listOf("V2ps8JltT1fpHnrS32Im0BWTlcI3", "TrKKgOQ0oaVPZDiY8g5Xj793nEz2"),
            emptyList(),
            mapOf("V2ps8JltT1fpHnrS32Im0BWTlcI3" to 10, "TrKKgOQ0oaVPZDiY8g5Xj793nEz2" to 20),
            mapOf("V2ps8JltT1fpHnrS32Im0BWTlcI3" to 30, "TrKKgOQ0oaVPZDiY8g5Xj793nEz2" to 40))
    houseHoldRepository.addHousehold(houseHold)
    userRepository.addHouseholdUID(houseHold.uid)
  }

  fun checkItemStatus() {
    val selectedHousehold = selectedHousehold.value
    if (selectedHousehold != null) {
      viewModelScope.launch {
        listFoodItemsRepository.foodItems.collect { foodItems ->
          foodItems.forEach { foodItem ->
            if (foodItem.expiryDate!! < Timestamp.now() && foodItem.status != FoodStatus.EXPIRED) {

              listFoodItemsRepository.updateFoodItem(
                  selectedHousehold.uid, foodItem.copy(status = FoodStatus.EXPIRED))

              val newStinkyPoints = selectedHousehold.stinkyPoints.toMutableMap()
              if (!newStinkyPoints.contains(foodItem.owner)) {
                newStinkyPoints[foodItem.owner] = foodItem.foodFacts.quantity.amount.toLong()
              } else {
                newStinkyPoints[foodItem.owner] =
                    foodItem.foodFacts.quantity.amount.toLong() + newStinkyPoints[foodItem.owner]!!
              }

              houseHoldRepository.updateStinkyPoints(selectedHousehold.uid, newStinkyPoints)
            } else if (foodItem.openDate != null &&
                foodItem.openDate < Timestamp.now() &&
                foodItem.status != FoodStatus.OPENED) {
              listFoodItemsRepository.updateFoodItem(
                  selectedHousehold.uid, foodItem.copy(status = FoodStatus.OPENED))
            }
          }
        }
      }
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

  fun editFoodItem(newFoodItem: FoodItem) {
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
    // No need to manually call filterFoodItems(), as filteredFoodItems is now reactive.
  }
}
