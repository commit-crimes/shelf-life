package com.android.shelfLife.ui.overview

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.android.shelfLife.ui.utils.CustomSearchBar
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the overview screen.
 *
 * This screen provides a list of food items within the selected household and options for searching,
 * filtering, and managing the items. Users can view, select, delete, or add food items. The screen
 * includes a top navigation bar, a floating action button to add new food items, and a bottom navigation
 * bar to navigate between other screens.
 *
 * @param navigationActions The actions for screen transitions within the app.
 */
@Composable
fun OverviewScreen(navigationActions: NavigationActions) {
    val overviewScreenViewModel = hiltViewModel<OverviewScreenViewModel>()

    // State variables for food items, households, and search
    val selectedHousehold by overviewScreenViewModel.selectedHousehold.collectAsState()
    val foodItems by overviewScreenViewModel.foodItems.collectAsState()
    val households by overviewScreenViewModel.households.collectAsState()
    val selectedFilters by overviewScreenViewModel.selectedFilters.collectAsState()
    val multipleSelectedFoodItems by overviewScreenViewModel.multipleSelectedFoodItems.collectAsState()
    val filteredFoodItems by overviewScreenViewModel.filteredFoodItems.collectAsState()
    val searchQuery by overviewScreenViewModel.query.collectAsState()

    // State for drawer and coroutine scope
    val drawerState by overviewScreenViewModel.drawerState.collectAsState()
    val scope = rememberCoroutineScope()

    // HouseHold selection drawer, ensuring household selection exists or redirecting to first-time user screen
    HouseHoldSelectionDrawer(
        scope = scope, drawerState = drawerState, navigationActions = navigationActions) {
        if (selectedHousehold == null && households.isEmpty()) {
            Log.d("OverviewScreen", households.toString())
            navigationActions.navigateTo(Screen.FIRST_TIME_USER)
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
                            modifier = Modifier.testTag("leaderboardFab")
                        ) {
                            Icon(
                                imageVector =
                                Icons.Default.Leaderboard, // Ensure you have an icon for leaderboard
                                contentDescription = "Leaderboard"
                            )
                        }

                        // Add food item
                        FloatingActionButton(
                            onClick = { navigationActions.navigateTo(Screen.ADD_FOOD) },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("addFoodFab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                },
            ) { paddingValues ->
                // Column for the main content of the screen
                Column(
                    modifier = Modifier.padding(paddingValues),
                ) {
                    // Custom search bar for searching food items
                    CustomSearchBar(
                        query = searchQuery,
                        onQueryChange = { newQuery -> overviewScreenViewModel.changeQuery(newQuery) },
                        placeholder = "Search food item",
                        onDeleteTextClicked = { overviewScreenViewModel.changeQuery("") },
                        searchBarTestTag = "foodSearchBar"
                    )

                    // Display list of food items with filtering
                    ListFoodItems(
                        foodItems = filteredFoodItems,
                        overviewScreenViewModel = overviewScreenViewModel,
                        onFoodItemClick = { selectedFoodItem ->
                            overviewScreenViewModel.selectFoodItem(selectedFoodItem)
                            navigationActions.navigateTo(Screen.INDIVIDUAL_FOOD_ITEM)
                        },
                        onFoodItemLongHold = { selectedFoodItem ->
                            overviewScreenViewModel.selectMultipleFoodItems(selectedFoodItem)
                        }
                    )
                }
            }
        }
    }
}