package com.android.shelfLife.ui.newoverview

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.newnavigation.BottomNavigationMenu
import com.android.shelfLife.ui.newnavigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.newnavigation.TopNavigationBar
import com.android.shelfLife.ui.utils.CustomSearchBar
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the overview screen
 *
 * @param navigationActions The actions to handle navigation
 */
@Composable
fun OverviewScreen(navigationActions: NavigationActions) {
  val overviewScreenViewModel = hiltViewModel<OverviewScreenViewModel>()

  val selectedHousehold by overviewScreenViewModel.selectedHousehold.collectAsState()
  val foodItems by overviewScreenViewModel.foodItems.collectAsState()
  val households by overviewScreenViewModel.households.collectAsState()
  val selectedFilters by overviewScreenViewModel.selectedFilters.collectAsState()
  val multipleSelectedFoodItems by
      overviewScreenViewModel.multipleSelectedFoodItems.collectAsState()
  val filteredFoodItems by overviewScreenViewModel.filteredFoodItems.collectAsState()
  val searchQuery by overviewScreenViewModel.query.collectAsState()

  val drawerState by overviewScreenViewModel.drawerState.collectAsState()
  val scope = rememberCoroutineScope()

  HouseHoldSelectionDrawer(
      scope = scope, drawerState = drawerState, navigationActions = navigationActions) {
        if (selectedHousehold == null && households.isEmpty()) {
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
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp) // space between FABs
                    ) {
                      // Leaderboard
                      FloatingActionButton(
                          onClick = { navigationActions.navigateTo(Screen.LEADERBOARD) },
                          containerColor = MaterialTheme.colorScheme.secondaryContainer,
                          modifier = Modifier.testTag("leaderboardFab")) {
                            Icon(
                                imageVector =
                                    Icons.Default
                                        .Leaderboard, // Make sure you have an icon for leaderboard
                                contentDescription = "Leaderboard")
                          }

                      // Add
                      FloatingActionButton(
                          onClick = { navigationActions.navigateTo(Screen.ADD_FOOD) },
                          containerColor = MaterialTheme.colorScheme.secondaryContainer,
                          modifier = Modifier.testTag("addFoodFab")) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                          }
                    }
              },
          ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
              CustomSearchBar(
                  query = searchQuery,
                  onQueryChange = { newQuery -> overviewScreenViewModel.changeQuery(newQuery) },
                  placeholder = "Search food item",
                  onDeleteTextClicked = { overviewScreenViewModel.changeQuery("") },
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
