package com.android.shelfLife.ui.newoverview

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.newnavigation.BottomNavigationMenu
import com.android.shelfLife.ui.newnavigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.newnavigation.TopNavigationBar
import com.android.shelfLife.ui.utils.CustomSearchBar
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the overview screen
 *
 * @param navigationActions The actions to handle navigation
 * @param houseHoldRepository The repository to handle household data
 */
@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
) {
  val overviewScreenViewModel = hiltViewModel<OverviewScreenViewModel>()

  val selectedHousehold by overviewScreenViewModel.selectedHousehold.collectAsState()
  val foodItems by overviewScreenViewModel.foodItems.collectAsState()
  val households by overviewScreenViewModel.households.collectAsState()
  val householdViewModelIsLoaded by overviewScreenViewModel.finishedLoading.collectAsState()
  val selectedFilters by overviewScreenViewModel.selectedFilters.collectAsState()
  val multipleSelectedFoodItems by
      overviewScreenViewModel.multipleSelectedFoodItems.collectAsState()

  var searchQuery by rememberSaveable { mutableStateOf("") }

  val drawerState by overviewScreenViewModel.drawerState.collectAsState()
  val scope = rememberCoroutineScope()

  HouseHoldSelectionDrawer(
      scope = scope, drawerState = drawerState, navigationActions = navigationActions) {
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
        } else if (selectedHousehold == null && households.isEmpty()) {
          Log.d("OverviewScreen", households.toString())
          FirstTimeWelcomeScreen(navigationActions, overviewScreenViewModel)
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
                      showDeleteOption = multipleSelectedFoodItems.isNotEmpty(),
                      onDeleteClick = {
                        overviewScreenViewModel.deleteMultipleFoodItems(multipleSelectedFoodItems)
                        overviewScreenViewModel.clearMultipleSelectedFoodItems()
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
              CustomSearchBar(
                  query = searchQuery,
                  onQueryChange = { searchQuery = it },
                  placeholder = "Search food item",
                  onDeleteTextClicked = { searchQuery = "" },
                  searchBarTestTag = "foodSearchBar")
              ListFoodItems(
                  foodItems = filteredFoodItems,
                  overviewScreenViewModel = overviewScreenViewModel,
                  onFoodItemClick = { selectedFoodItem ->
                    overviewScreenViewModel.selectFoodItem(selectedFoodItem)
                    navigationActions.navigateTo(Screen.INDIVIDUAL_FOOD_ITEM)
                  },
                  onFoodItemLongHold = { selectedFoodItem ->
                    overviewScreenViewModel.selectMultipleFoodItems(selectedFoodItem)
                  })
            }
          }
        }
      }
}
