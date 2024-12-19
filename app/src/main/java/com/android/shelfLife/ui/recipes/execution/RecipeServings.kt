package com.android.shelfLife.ui.recipes.execution

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

/**
 * Composable function to display the screen for selecting the number of servings.
 *
 * @param navigationActions The actions to handle navigation.
 * @param executeRecipeViewModel The ViewModel for managing the state of the recipe execution.
 * @param onNext Callback function to handle the next action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServingsScreen(
    navigationActions: NavigationActions,
    executeRecipeViewModel: ExecuteRecipeViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
  val servings by executeRecipeViewModel.servings.collectAsState()

  Log.d("ServingsScreen", "Current servings: $servings")

  Scaffold(
      modifier = Modifier.testTag("servingsScreen"),
      topBar = {
        TopAppBar(
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            modifier = Modifier.testTag("topBar"),
            navigationIcon = {
              IconButton(
                  onClick = {
                    Log.d("ServingsScreen", "Back button clicked")
                    navigationActions.goBack()
                  },
                  modifier = Modifier.testTag("goBackArrow")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back Icon")
                  }
            },
            title = {
              Text(
                  text = "Choose number of servings",
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontSize = 24.sp, fontWeight = FontWeight.Bold))
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { destination ->
              Log.d("ServingsScreen", "Navigating to $destination")
              navigationActions.navigateTo(destination)
            },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.RECIPES)
      },
      floatingActionButton = {
        androidx.compose.material3.FloatingActionButton(
            onClick = {
              Log.d("ServingsScreen", "FloatingActionButton clicked: next state")
              onNext()
            },
            modifier =
                Modifier.testTag("nextFab").padding(horizontal = 16.dp).height(48.dp).width(120.dp),
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary) {
              Text(
                  text = "Next",
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
      },
      content = { paddingValues ->
        ServingsSelector(
            servings = servings,
            onIncrease = {
              Log.d("ServingsScreen", "Increasing servings from $servings to ${servings + 1}")
              executeRecipeViewModel.updateServings(servings + 1)
            },
            onDecrease = {
              if (servings > 1) {
                Log.d("ServingsScreen", "Decreasing servings from $servings to ${servings - 1}")
                executeRecipeViewModel.updateServings(servings - 1)
              } else {
                Log.d("ServingsScreen", "Servings cannot be decreased below 1")
              }
            },
            modifier = Modifier.padding(paddingValues))
      })
}

/**
 * Composable function to display the selector for the number of servings.
 *
 * @param servings The current number of servings.
 * @param onIncrease Callback function to handle increasing the number of servings.
 * @param onDecrease Callback function to handle decreasing the number of servings.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun ServingsSelector(
    servings: Float,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier
) {
  Column(
      modifier = modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Select Servings", modifier = Modifier.padding(bottom = 16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)) {
              IconButton(
                  modifier = Modifier.testTag("decreaseButton"),
                  onClick = {
                    Log.d("ServingsSelector", "Decrease button clicked")
                    onDecrease()
                  }) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease Servings")
                  }

              Text(
                  text = servings.toString(),
                  modifier = Modifier.padding(horizontal = 16.dp).testTag("servingsText"))

              IconButton(
                  modifier = Modifier.testTag("increaseButton"),
                  onClick = {
                    Log.d("ServingsSelector", "Increase button clicked")
                    onIncrease()
                  }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Servings")
                  }
            }
      }
}
