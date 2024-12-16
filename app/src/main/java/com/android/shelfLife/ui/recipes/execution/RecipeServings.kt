package com.android.shelfLife.ui.recipes.execution

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

/**
 * Composable function to display the screen where users can select the number of servings for a recipe.
 *
 * This screen allows users to adjust the number of servings for the recipe. The "Next" button allows the
 * user to proceed to the next step in the recipe process. The top bar includes a "Back" button for navigation.
 * The screen also features a "Servings Selector" that enables the user to increase or decrease the servings.
 *
 * @param navigationActions The actions for navigating between screens.
 * @param executeRecipeViewModel The [ExecuteRecipeViewModel] for managing the state related to recipe execution.
 * @param onNext Lambda function to navigate to the next step after adjusting servings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServingsScreen(
    navigationActions: NavigationActions,
    executeRecipeViewModel: ExecuteRecipeViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    // Observe the current servings value from the ViewModel
    val servings by executeRecipeViewModel.servings.collectAsState()

    Log.d("ServingsScreen", "Current servings: $servings")

    Scaffold(
        modifier = Modifier.testTag("servingsScreen"),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.testTag("topBar"),
                navigationIcon = {
                    // Back button
                    IconButton(
                        onClick = {
                            Log.d("ServingsScreen", "Back button clicked")
                            navigationActions.goBack()
                        },
                        modifier = Modifier.testTag("goBackArrow")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back Icon"
                        )
                    }
                },
                title = {
                    // Title of the screen
                    Text(
                        text = "Choose number of servings",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        },
        bottomBar = {
            // Bottom navigation menu
            BottomNavigationMenu(
                onTabSelect = { destination ->
                    Log.d("ServingsScreen", "Navigating to $destination")
                    navigationActions.navigateTo(destination)
                },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.RECIPES
            )
        },
        floatingActionButton = {
            // Floating Action Button to move to the next step
            FloatingActionButton(
                onClick = {
                    Log.d("ServingsScreen", "FloatingActionButton clicked: next state")
                    onNext() // Move to the next step
                },
                modifier = Modifier
                    .testTag("nextFab")
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .width(120.dp),
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        content = { paddingValues ->
            // The content of the screen, which is the ServingsSelector
            ServingsSelector(
                servings = servings,
                onIncrease = {
                    Log.d("ServingsScreen", "Increasing servings from $servings to ${servings + 1}")
                    executeRecipeViewModel.updateServings(servings + 1) // Increase servings by 1
                },
                onDecrease = {
                    // Decrease servings, but not below 1
                    if (servings > 1) {
                        Log.d("ServingsScreen", "Decreasing servings from $servings to ${servings - 1}")
                        executeRecipeViewModel.updateServings(servings - 1) // Decrease servings by 1
                    } else {
                        Log.d("ServingsScreen", "Servings cannot be decreased below 1")
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}

/**
 * Composable function to display the servings selector.
 *
 * This selector allows the user to adjust the number of servings by increasing or decreasing the value.
 * It also displays the current selected number of servings and provides buttons to increase or decrease the servings.
 *
 * @param servings The current number of servings selected.
 * @param onIncrease Lambda function to handle increasing the servings.
 * @param onDecrease Lambda function to handle decreasing the servings.
 * @param modifier The modifier to apply to the layout.
 */
@Composable
fun ServingsSelector(
    servings: Float,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Servings",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Decrease servings button
            IconButton(onClick = {
                Log.d("ServingsSelector", "Decrease button clicked")
                onDecrease() // Decrease servings
            }) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease Servings"
                )
            }

            // Display the current number of servings
            Text(
                text = servings.toString(),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Increase servings button
            IconButton(onClick = {
                Log.d("ServingsSelector", "Increase button clicked")
                onIncrease() // Increase servings
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase Servings"
                )
            }
        }
    }
}