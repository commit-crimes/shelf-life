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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
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
  val selectedHousehold = householdViewModel.selectedHousehold.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  val foodItems = listFoodItemsViewModel.foodItems.collectAsState()
  val userHouseholds = householdViewModel.households.collectAsState()
  val householdViewModelIsLoaded = householdViewModel.finishedLoading.collectAsState().value
  val selectedFilters = remember { mutableStateListOf<String>() }

  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val filters = listOf("Dairy", "Meat", "Fish", "Fruit", "Vegetables", "Bread", "Canned")

  HouseHoldSelectionDrawer(
      scope = scope,
      drawerState = drawerState,
      householdViewModel = householdViewModel,
      navigationActions = navigationActions) {
        val filteredFoodItems =
            foodItems.value.filter { item ->
              item.foodFacts.name.contains(searchQuery, ignoreCase = true) &&
                  (selectedFilters.isEmpty() ||
                      selectedFilters.contains(item.foodFacts.category.name))
            }

        if (!householdViewModelIsLoaded) {
          Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
          ) {
            CircularProgressIndicator()
          }
        } else if (selectedHousehold.value == null && userHouseholds.value.isEmpty()) {
            Log.d("OverviewScreen", userHouseholds.value.toString())
          FirstTimeWelcomeScreen(navigationActions, householdViewModel)
        } else {
          Scaffold(
              modifier = Modifier.testTag("overviewScreen"),
              topBar = {
                selectedHousehold.value?.let {
                  TopNavigationBar(
                      houseHold = it,
                      onHamburgerClick = { scope.launch { drawerState.open() } },
                      filters = filters,
                      selectedFilters = selectedFilters,
                      onFilterChange = { filter, isSelected ->
                        if (isSelected) {
                          selectedFilters.add(filter)
                        } else {
                          selectedFilters.remove(filter)
                        }
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
                  onFoodItemClick = { selectedFoodItem ->
                    listFoodItemsViewModel.selectFoodItem(selectedFoodItem)
                    navigationActions.navigateTo(Screen.INDIVIDUAL_FOOD_ITEM)
                  })
            }
          }
        }
      }
}
