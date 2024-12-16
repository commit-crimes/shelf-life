package com.android.shelfLife.viewmodel.overview

import android.content.Context
import android.util.Log
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the Overview Screen.
 *
 * This ViewModel handles:
 * - Household management, including adding/testing households and selecting households to edit.
 * - Managing food items, including filtering, querying, and updating their status.
 * - Managing UI state, including filters, query changes, and multi-selection of food items.
 *
 * @property houseHoldRepository The repository for managing household-related data.
 * @property listFoodItemsRepository The repository for managing food item-related data.
 * @property userRepository The repository for managing user-related data.
 * @property context The application context.
 */
@HiltViewModel
class OverviewScreenViewModel
@Inject
constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val listFoodItemsRepository: FoodItemRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** The currently selected filters for food item filtering. */
    private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
    val selectedFilters = _selectedFilters.asStateFlow()

    /** The state of the drawer in the UI. */
    private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
    val drawerState = _drawerState.asStateFlow()

    /** The list of currently selected food items for bulk actions. */
    private val _multipleSelectedFoodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val multipleSelectedFoodItems: StateFlow<List<FoodItem>> = _multipleSelectedFoodItems.asStateFlow()

    /** Indicates if data has finished loading. */
    val finishedLoading = MutableStateFlow(false)

    /** The list of all households available to the user. */
    val households = houseHoldRepository.households

    /** The currently selected household. */
    val selectedHousehold = houseHoldRepository.selectedHousehold

    /** The list of all food items in the selected household. */
    val foodItems = listFoodItemsRepository.foodItems

    /** A map of available food category filters. */
    private var FILTERS = mapOf(
        "Dairy" to FoodCategory.DAIRY,
        "Meat" to FoodCategory.MEAT,
        "Fish" to FoodCategory.FISH,
        "Fruit" to FoodCategory.FRUIT,
        "Vegetables" to FoodCategory.VEGETABLE,
        "Grain" to FoodCategory.GRAIN,
        "Beverage" to FoodCategory.BEVERAGE,
        "Snack" to FoodCategory.SNACK,
        "Other" to FoodCategory.OTHER
    )

    /** The list of filter names. */
    var filters = FILTERS.keys.toList()

    /** The current search query for food item filtering. */
    private val _query = MutableStateFlow<String>("")
    val query = _query.asStateFlow()

    /**
     * The list of food items filtered based on the selected filters and search query.
     */
    val filteredFoodItems: StateFlow<List<FoodItem>> =
        combine(foodItems, selectedFilters, query) { foods, currentFilters, currentQuery ->
            foods.filter { item ->
                val matchesFilters = currentFilters.isEmpty() ||
                        currentFilters.any { filter -> item.foodFacts.category == FILTERS[filter] }
                val matchesQuery = currentQuery.isEmpty() ||
                        item.foodFacts.name.contains(currentQuery, ignoreCase = true)
                matchesFilters && matchesQuery
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Initializes the ViewModel by checking the status of food items.
     */
    init {
        checkItemStatus()
        Log.d("OverviewScreenViewModel", "Init")
    }

    /**
     * Adds a custom household for testing purposes.
     */
    suspend fun addCustomHouseholdForTesting() {
        val houseHold = HouseHold(
            "testHouseHoldUid",
            "testHouseHoldName",
            listOf("V2ps8JltT1fpHnrS32Im0BWTlcI3", "TrKKgOQ0oaVPZDiY8g5Xj793nEz2"),
            emptyList(),
            mapOf("V2ps8JltT1fpHnrS32Im0BWTlcI3" to 10, "TrKKgOQ0oaVPZDiY8g5Xj793nEz2" to 20),
            mapOf("V2ps8JltT1fpHnrS32Im0BWTlcI3" to 30, "TrKKgOQ0oaVPZDiY8g5Xj793nEz2" to 40)
        )
        houseHoldRepository.addHousehold(houseHold)
        userRepository.addHouseholdUID(houseHold.uid)
    }

    /**
     * Checks and updates the status of food items based on their expiry or open date.
     */
    fun checkItemStatus() {
        val selectedHousehold = selectedHousehold.value
        if (selectedHousehold != null) {
            viewModelScope.launch {
                listFoodItemsRepository.foodItems.collect { foodItems ->
                    foodItems.forEach { foodItem ->
                        if (foodItem.expiryDate!! < Timestamp.now() && foodItem.status != FoodStatus.EXPIRED) {
                            listFoodItemsRepository.updateFoodItem(
                                selectedHousehold.uid, foodItem.copy(status = FoodStatus.EXPIRED)
                            )
                            val newStinkyPoints = selectedHousehold.stinkyPoints.toMutableMap()
                            newStinkyPoints[foodItem.owner] =
                                newStinkyPoints.getOrDefault(foodItem.owner, 0) +
                                        foodItem.foodFacts.quantity.amount.toLong()
                            houseHoldRepository.updateStinkyPoints(selectedHousehold.uid, newStinkyPoints)
                        } else if (foodItem.openDate != null &&
                            foodItem.openDate < Timestamp.now() &&
                            foodItem.status != FoodStatus.OPENED) {
                            listFoodItemsRepository.updateFoodItem(
                                selectedHousehold.uid, foodItem.copy(status = FoodStatus.OPENED)
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Selects a household for editing.
     *
     * @param household The household to edit.
     */
    fun selectHouseholdToEdit(household: HouseHold?) {
        houseHoldRepository.selectHouseholdToEdit(household)
    }

    /**
     * Deletes multiple food items from the repository.
     *
     * @param foodItems The list of food items to delete.
     */
    fun deleteMultipleFoodItems(foodItems: List<FoodItem>) {
        val selectedHousehold = selectedHousehold.value
        if (selectedHousehold != null) {
            viewModelScope.launch {
                foodItems.forEach { listFoodItemsRepository.updateFoodItem(selectedHousehold.uid, it) }
            }
        }
    }

    /**
     * Toggles the selected state of a filter.
     *
     * @param filter The filter to toggle.
     */
    fun toggleFilter(filter: String) {
        if (_selectedFilters.value.contains(filter)) {
            _selectedFilters.update { it - filter }
        } else {
            _selectedFilters.update { it + filter }
        }
    }

    /**
     * Selects or deselects a food item for multi-selection actions.
     *
     * @param foodItem The food item to toggle selection for.
     */
    fun selectMultipleFoodItems(foodItem: FoodItem) {
        if (_multipleSelectedFoodItems.value.contains(foodItem)) {
            _multipleSelectedFoodItems.value = _multipleSelectedFoodItems.value.minus(foodItem)
        } else {
            _multipleSelectedFoodItems.value = _multipleSelectedFoodItems.value.plus(foodItem)
        }
    }

    /**
     * Clears the list of selected food items for multi-selection actions.
     */
    fun clearMultipleSelectedFoodItems() {
        _multipleSelectedFoodItems.value = emptyList()
    }

    /**
     * Updates an existing food item in the repository.
     *
     * @param newFoodItem The updated food item.
     */
    fun editFoodItem(newFoodItem: FoodItem) {
        val selectedHousehold = selectedHousehold.value
        if (selectedHousehold != null) {
            viewModelScope.launch {
                listFoodItemsRepository.updateFoodItem(selectedHousehold.uid, newFoodItem)
            }
        }
    }

    /**
     * Selects a food item for individual viewing or editing.
     *
     * @param foodItem The food item to select.
     */
    fun selectFoodItem(foodItem: FoodItem?) {
        listFoodItemsRepository.selectFoodItem(foodItem)
    }

    /**
     * Updates the current search query.
     *
     * @param newQuery The new search query.
     */
    fun changeQuery(newQuery: String) {
        _query.value = newQuery
    }
}