package com.android.shelfLife.ui.overview

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import com.android.shelfLife.ui.utils.CustomSearchBar
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

  HouseHoldSelectionDrawer(
      scope = scope,
      drawerState = drawerState,
      householdViewModel = householdViewModel,
      navigationActions = navigationActions) {
        val filteredFoodItems =
            foodItems.filter { item ->
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
                    onClick = { navigationActions.navigateTo(Screen.FIRST_FOOD_ITEM) },
                    content = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    modifier = Modifier.testTag("addFoodFab"),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer)
              },
          ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
              CustomSearchBar(
                  query = searchQuery,
                  onQueryChange = { searchQuery = it },
                  placeholder = "Search food item",
                  searchBarTestTag = "foodSearchBar")
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
