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

  val filters =
      listOf("Fruit", "Vegetable", "Meat", "Fish", "Dairy", "Grain", "Beverage", "Snack", "Other")

  HouseHoldSelectionDrawer(
      scope = scope,
      drawerState = drawerState,
      householdViewModel = householdViewModel,
      navigationActions = navigationActions) {
        val filteredFoodItemsByFilters =
            if (selectedFilters.isEmpty()) {
              foodItems
            } else {
              foodItems.filter { foodItem ->
                selectedFilters.any { filter ->
                  foodItem.foodFacts.category == stringToCategory(filter)
                }
              }
            }

        val filteredFoodItemsByQuery = {
          if (searchQuery.isEmpty()) {
            foodItems
          } else {
            foodItems.filter { item ->
              item.foodFacts.name.contains(searchQuery, ignoreCase = true)
            }
          }
        }

        val filteredFoodItems =
            filteredFoodItemsByFilters.filter { it in filteredFoodItemsByQuery() }

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
                      filters = overviewScreenViewModel.filters,
                      selectedFilters = selectedFilters,
                      onFilterChange = { filter, _ ->
                        overviewScreenViewModel.toggleFilter(filter)
                      },
                      showDeleteOption = multipleSelectedFoodItems.value.isNotEmpty(),
                      onDeleteClick = {
                        householdViewModel.deleteMultipleFoodItems(multipleSelectedFoodItems.value)
                        listFoodItemsViewModel.clearMultipleSelectedFoodItems()
                      })
                }
              },
              bottomBar = {
                BottomNavigationMenu(
                    onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                    tabList = LIST_TOP_LEVEL_DESTINATION,
                    selectedItem = Route.OVERVIEW)
              },
              // Floating Action Button to add a new food item
              floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigationActions.navigateTo(Screen.ADD_FOOD) },
                    content = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    modifier = Modifier.testTag("addFoodFab"),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer)
              },
          ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
              FoodSearchBar(
                  query = searchQuery,
                  onQueryChange = { searchQuery = it } // Update the query state when the user types
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
                  })
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
