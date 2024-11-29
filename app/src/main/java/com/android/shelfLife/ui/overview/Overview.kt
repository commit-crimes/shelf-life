package com.android.shelfLife.ui.overview

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.shelfLife.model.foodFacts.FoodCategory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.overview.OverviewScreenViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.TopNavigationBar
import kotlinx.coroutines.launch

/**
 * Composable function to display the overview screen
 *
 * @param navigationActions The actions to handle navigation
 * @param householdViewModel The ViewModel for the households the user has access to
 */
@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
    householdViewModel: HouseholdViewModel,
    listFoodItemsViewModel: ListFoodItemsViewModel
) {
    val overviewScreenViewModel = viewModel<OverviewScreenViewModel>()

    val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
    val foodItems by listFoodItemsViewModel.foodItems.collectAsState()
    val userHouseholds by householdViewModel.households.collectAsState()
    val householdViewModelIsLoaded by householdViewModel.finishedLoading.collectAsState()
    val selectedFilters by overviewScreenViewModel.selectedFilters.collectAsState()
    val multipleSelectedFoodItems = listFoodItemsViewModel.multipleSelectedFoodItems.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val drawerState by overviewScreenViewModel.drawerState.collectAsState()
    val scope = rememberCoroutineScope()

    // Map of filter labels to FoodCategory enums
    val categoryMap = FoodCategory.values().associateBy { it.toString() }

    HouseHoldSelectionDrawer(
        scope = scope,
        drawerState = drawerState,
        householdViewModel = householdViewModel,
        navigationActions = navigationActions
    ) {
        // Perform filtering
        val filteredFoodItems = filterFoodItems(
            foodItems = foodItems,
            selectedFilters = selectedFilters,
            query = searchQuery,
            categoryMap = categoryMap
        )

        if (!householdViewModelIsLoaded) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (selectedHousehold == null && userHouseholds.isEmpty()) {
            Log.d("OverviewScreen", userHouseholds.toString())
            FirstTimeWelcomeScreen(navigationActions, householdViewModel)
        } else {
            Scaffold(
                modifier = Modifier.testTag("overviewScreen"),
                topBar = {
                    selectedHousehold?.let {
                        TopNavigationBar(
                            houseHold = it,
                            onHamburgerClick = { scope.launch { drawerState.open() } },
                            filters = categoryMap.keys.toList(), // Use keys from map as filter options
                            selectedFilters = selectedFilters,
                            onFilterChange = { filter, _ ->
                                overviewScreenViewModel.toggleFilter(filter)
                            },
                            showDeleteOption = multipleSelectedFoodItems.value.isNotEmpty(),
                            onDeleteClick = {
                                householdViewModel.deleteMultipleFoodItems(
                                    multipleSelectedFoodItems.value
                                )
                                listFoodItemsViewModel.clearMultipleSelectedFoodItems()
                            }
                        )
                    }
                },
                bottomBar = {
                    BottomNavigationMenu(
                        onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                        tabList = LIST_TOP_LEVEL_DESTINATION,
                        selectedItem = Route.OVERVIEW
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navigationActions.navigateTo(Screen.ADD_FOOD) },
                        content = { Icon(Icons.Default.Add, contentDescription = "Add") },
                        modifier = Modifier.testTag("addFoodFab"),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                },
            ) { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues),
                ) {
                    FoodSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                    ListFoodItems(
                        foodItems = filteredFoodItems,
                        householdViewModel = householdViewModel,
                        listFoodItemsViewModel = listFoodItemsViewModel,
                        onFoodItemClick = { selectedFoodItem ->
                            listFoodItemsViewModel.selectFoodItem(selectedFoodItem)
                            navigationActions.navigateTo(Screen.INDIVIDUAL_FOOD_ITEM)
                        },
                        onFoodItemLongHold = { selectedFoodItem ->
                            listFoodItemsViewModel.selectMultipleFoodItems(selectedFoodItem)
                        }
                    )
                }
            }
        }
    }
}


/**
 * Converts a given string into a corresponding FoodCategory enum value.
 *
 * @param string the name of the food category as a string (e.g., "Dairy", "Meat", "Fish").
 * @return the matching FoodCategory enum value.
 * @throws IllegalArgumentException if the string does not match any known food category.
 *
 * Example:
 * ```
 * stringToCategory("Dairy") // Returns FoodCategory.DAIRY
 * stringToCategory("Meat")  // Returns FoodCategory.MEAT
 * stringToCategory("Invalid") // Throws IllegalArgumentException
 * ```
 */
fun stringToCategory(string: String): FoodCategory {
  return when (string) {
    "Dairy" -> FoodCategory.DAIRY
    "Meat" -> FoodCategory.MEAT
    "Fish" -> FoodCategory.FISH
    "Fruit" -> FoodCategory.FRUIT
    "Vegetable" -> FoodCategory.VEGETABLE
    "Grain" -> FoodCategory.GRAIN
    "Beverage" -> FoodCategory.BEVERAGE
    "Snack" -> FoodCategory.SNACK
    "Other" -> FoodCategory.OTHER
    else -> throw IllegalArgumentException("Unknown food category: $string")
  }
}

/**
 * Filters a list of food items based on selected filters and a search query.
 *
 * @param foodItems The complete list of food items to be filtered.
 * @param selectedFilters A list of filter criteria. Each filter is matched against the item's
 *   category. If the list is empty, no filter is applied based on this parameter.
 * @param query A search string used to filter food items by their names. If the string is empty, no
 *   filtering is applied based on this parameter.
 * @param categoryMap A map linking displayable labels to FoodCategory enums.
 * @return A list of food items that match both the selected filters and the search query.
 *     - Food items are included if they match at least one filter from `selectedFilters` (if
 *       provided).
 *     - Food items are included if their names contain the `query` string (case-insensitive, if
 *       provided).
 *     - If both `selectedFilters` and `query` are empty, the original list is returned without any
 *       filtering.
 */
fun filterFoodItems(
    foodItems: List<FoodItem>,
    selectedFilters: List<String>,
    query: String,
    categoryMap: Map<String, FoodCategory>
): List<FoodItem> {
    return foodItems.filter { foodItem ->
        // Match filters (if provided)
        (selectedFilters.isEmpty() ||
                selectedFilters.any { filter ->
                    foodItem.foodFacts.category == categoryMap[filter]
                }) &&
                // Match search query (if provided)
                (query.isEmpty() || foodItem.foodFacts.name.contains(query, ignoreCase = true))
    }
}

