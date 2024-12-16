package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.utils.FoodItemDetails
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.overview.IndividualFoodItemViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display individual food item details.
 *
 * This screen displays detailed information about a selected food item, including its image, name,
 * and other relevant details. It provides options to edit or delete the food item. If no food item
 * is selected, the user is navigated to the Easter Egg screen.
 *
 * @param navigationActions The navigation actions to be used for screen transitions.
 * @param individualFoodItemViewModel The [IndividualFoodItemViewModel] that handles the data
 * and actions for managing the selected food item.
 */
@Composable
fun IndividualFoodItemScreen(
    navigationActions: NavigationActions,
    individualFoodItemViewModel: IndividualFoodItemViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    if (individualFoodItemViewModel.selectedFood != null) {
        Scaffold(
            modifier = Modifier.testTag("IndividualFoodItemScreen"),
            topBar = {
                CustomTopAppBar(
                    onClick = { navigationActions.goBack() },
                    title =
                    if (individualFoodItemViewModel.selectedFood != null)
                        individualFoodItemViewModel.selectedFood!!.foodFacts.name
                    else "",
                    titleTestTag = "IndividualFoodItemName",
                    actions = {
                        // Delete icon action
                        IconButton(
                            onClick = {
                                individualFoodItemViewModel.selectedFood?.let {
                                    coroutineScope.launch {
                                        individualFoodItemViewModel.deleteFoodItem()
                                        navigationActions.goBack()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("deleteFoodItem")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                        }
                    })
            },
            // Floating Action Button for editing the food item
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigationActions.navigateTo(Screen.EDIT_FOOD) },
                    content = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                    modifier = Modifier.testTag("editFoodFab"),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            },
            bottomBar = {
                // Bottom navigation menu
                BottomNavigationMenu(
                    onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                    tabList = LIST_TOP_LEVEL_DESTINATION,
                    selectedItem = Route.OVERVIEW
                )
            }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                item {
                    // Show food item details if the item is selected
                    if (individualFoodItemViewModel.selectedFood != null) {
                        // Food image
                        AsyncImage(
                            model = individualFoodItemViewModel.selectedFood!!.foodFacts.imageUrl,
                            contentDescription = "Food Image",
                            modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("IndividualFoodItemImage"),
                            contentScale = ContentScale.Crop
                        )

                        // Display the food item details
                        FoodItemDetails(foodItem = individualFoodItemViewModel.selectedFood!!)
                    } else {
                        // Show loading indicator while data is being fetched
                        CircularProgressIndicator(modifier = Modifier.testTag("CircularProgressIndicator"))
                    }
                }
            }
        }
    } else {
        // Navigate to Easter Egg screen if no food item is selected
        navigationActions.navigateTo(Screen.EASTER_EGG)
    }
}