package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.overview.FirstTimeWelcomeScreen
import com.android.shelfLife.ui.utils.getTotalMinutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Displays the detailed view of a selected recipe, including its name, image, servings, time, and
 * instructions. This screen allows navigation back to the previous screen and provides a top and
 * bottom navigation bar.
 *
 * @param navigationActions Handles navigation between screens (e.g., go back, navigate to other
 *   destinations).
 * @param listRecipesViewModel ViewModel containing the list of recipes and the selected recipe
 *   data.
 */
fun IndividualRecipeScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
    householdViewModel: HouseholdViewModel
) {
  // Retrieve the currently selected recipe from the ViewModel.
  // If no recipe is selected, display an error message.
  val selectedRecipe =
      listRecipesViewModel.selectedRecipe.collectAsState().value
          ?: return Box(
              contentAlignment = Alignment.Center,
              content = {
                Text(
                    text = "No recipe selected. Should not happen",
                    modifier = Modifier.testTag("noRecipeSelectedMessage"),
                    color = Color.Red)
              })

  val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
  val userHouseholds = householdViewModel.households.collectAsState().value

  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  HouseHoldSelectionDrawer(
      scope = scope, drawerState = drawerState, householdViewModel = householdViewModel) {
        if (selectedHousehold == null) {
          FirstTimeWelcomeScreen(householdViewModel)
        } else {
          // Scaffold that provides the structure for the screen, including top and bottom bars.
          Scaffold(
              modifier = Modifier.testTag("individualRecipesScreen"),
              topBar = {
                TopAppBar(
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            navigationIconContentColor =
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            actionIconContentColor =
                                MaterialTheme.colorScheme.onSecondaryContainer),
                    modifier = Modifier.testTag("topBar"),
                    navigationIcon = {
                      // Back button to return to the previous screen
                      IconButton(
                          onClick = { navigationActions.goBack() },
                          modifier = Modifier.testTag("goBackArrow")) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back Icon")
                          }
                    },
                    // Title of the screen: Recipe name
                    title = {
                      Text(
                          text = selectedRecipe.name,
                          style =
                              MaterialTheme.typography.bodyLarge.copy(
                                  fontSize = 24.sp, fontWeight = FontWeight.Bold))
                    })
              },
              bottomBar = {
                // Bottom navigation bar for switching between main app destinations.
                BottomNavigationMenu(
                    onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                    tabList = LIST_TOP_LEVEL_DESTINATION,
                    selectedItem = Route.RECIPES)
              },
              content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize().testTag("recipe")) {

                  // Recipe content: image, servings, time, and instructions
                  Column(
                      modifier =
                          Modifier.padding(8.dp)
                              .fillMaxSize()
                              .verticalScroll(rememberScrollState()) // Enable vertical scrolling
                      ) {
                        // Display the recipe image (placeholder for now)
                        Image(
                            painter = painterResource(R.drawable.google_logo),
                            contentDescription = "Recipe Image",
                            modifier = Modifier.width(537.dp).height(159.dp).testTag("recipeImage"),
                            contentScale = ContentScale.FillWidth)

                        // Row displaying servings and time information
                        Row(modifier = Modifier.fillMaxWidth()) {
                          Text(
                              text = "Servings: ${selectedRecipe.servings}",
                              modifier = Modifier.testTag("recipeServings"))
                          Spacer(modifier = Modifier.width(16.dp))
                          Text(
                              text = "Time: ${getTotalMinutes(selectedRecipe.time)} min",
                              modifier = Modifier.testTag("recipeTime"))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display recipe instructions, scrollable if long
                        Text(
                            text = selectedRecipe.instructions,
                            modifier =
                                Modifier.padding(vertical = 8.dp).testTag("recipeInstructions"))
                      }
                }
              })
        }
      }
}
