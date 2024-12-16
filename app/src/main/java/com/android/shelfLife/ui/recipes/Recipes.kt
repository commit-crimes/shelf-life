package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.android.shelfLife.ui.utils.CustomSearchBar
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.android.shelfLife.viewmodel.recipes.RecipesViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipesScreen(navigationActions: NavigationActions) {
  val recipesViewModel = hiltViewModel<RecipesViewModel>()
  val overviewScreenViewModel = hiltViewModel<OverviewScreenViewModel>()

  val user = recipesViewModel.user.collectAsState()
  val selectedHousehold = recipesViewModel.household.collectAsState()

  val query by recipesViewModel.query.collectAsState()
  val filteredRecipeList by recipesViewModel.filteredRecipes.collectAsState()
  val selectedFilters by recipesViewModel.selectedFilters.collectAsState()

  val drawerState by overviewScreenViewModel.drawerState.collectAsState()
  val scope = rememberCoroutineScope()

  HouseHoldSelectionDrawer(
      scope = scope, drawerState = drawerState, navigationActions = navigationActions) {
        if (selectedHousehold == null) {
          navigationActions.navigateTo(Screen.FIRST_TIME_USER)
        } else {
          if (user != null) {
            Scaffold(
                modifier = Modifier.testTag("recipesScreen"),
                topBar = {
                  selectedHousehold.value?.let {
                    TopNavigationBar(
                        houseHold = it,
                        onHamburgerClick = { scope.launch { drawerState.open() } },
                        filters = recipesViewModel.filters,
                        selectedFilters = selectedFilters,
                        onFilterChange = { filter, _ -> recipesViewModel.clickOnFilter(filter) })
                  }
                },
                bottomBar = {
                  BottomNavigationMenu(
                      onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                      tabList = LIST_TOP_LEVEL_DESTINATION,
                      selectedItem = Route.RECIPES)
                },
                // Floating Action Button to add a new food item
                floatingActionButton = {
                  Column(
                      horizontalAlignment = Alignment.End,
                      verticalArrangement = Arrangement.spacedBy(16.dp),
                      modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)) {
                        // Secondary FAB for "Manual" option
                        if (recipesViewModel.fabExpanded.value) {
                          ExtendedFloatingActionButton(
                              text = { Text("Generate") },
                              icon = {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Add")
                              },
                              onClick = {
                                // Navigate to Manual Recipe screen
                                navigationActions.navigateTo(Screen.GENERATE_RECIPE)
                                recipesViewModel.shrinkFab()
                              },
                              containerColor = MaterialTheme.colorScheme.secondaryContainer,
                              modifier = Modifier.testTag("generateRecipeFab").width(150.dp))
                        }

                        // Primary FAB
                        ExtendedFloatingActionButton(
                            text = {
                              Text(if (recipesViewModel.fabExpanded.value) "Manual" else "")
                            },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                            onClick = {
                              if (recipesViewModel.fabExpanded.value) {
                                // Navigate to Generate Recipe screen
                                navigationActions.navigateTo(Screen.ADD_RECIPE)
                                recipesViewModel.shrinkFab()
                              } else {
                                // Expand the FABs
                                recipesViewModel.expandFab()
                              }
                            },
                            expanded = recipesViewModel.fabExpanded.value, // Bind to the state
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier =
                                Modifier.testTag("addRecipeFab")
                                    .width(
                                        if (recipesViewModel.fabExpanded.value) 150.dp else 56.dp))
                      }
                },
                content = { paddingValues ->
                  Column(
                      modifier =
                          Modifier.padding(paddingValues).fillMaxSize().pointerInput(Unit) {
                            detectTapGestures(onTap = { recipesViewModel.shrinkFab() })
                          }) {
                        CustomSearchBar(
                            query = query,
                            onQueryChange = { newQuery -> recipesViewModel.changeQuery(newQuery) },
                            placeholder = "Search recipe",
                            searchBarTestTag = "searchBar",
                            onDeleteTextClicked = { recipesViewModel.changeQuery("") })

                        if (filteredRecipeList.isEmpty()) {
                          Box(
                              modifier = Modifier.fillMaxSize(),
                              content = {
                                Text(
                                    text = "No recipes available",
                                    modifier = Modifier.testTag("noRecipesAvailableText"))
                              },
                              contentAlignment = Alignment.Center)
                        } else {
                          // LazyColumn for displaying the list of filtered recipes
                          LazyColumn(modifier = Modifier.fillMaxSize().testTag("recipesList")) {
                            items(filteredRecipeList) { recipe ->
                              RecipeItem(recipe, navigationActions, recipesViewModel)
                            }
                          }
                        }
                      }
                })
          }
        }
      }
}

@Composable
/**
 * Displays a recipe item as a card that the user can click to navigate to the recipe's details.
 *
 * @param recipe The recipe object that contains the recipe data (name, servings, time, etc.).
 * @param navigationActions A navigation controller to handle navigation events (e.g., navigating to
 *   the individual recipe screen).
 * @param listRecipesViewModel The ViewModel that manages the state and logic for the recipe list.
 */
fun RecipeItem(
    recipe: Recipe,
    navigationActions: NavigationActions,
    recipesViewModel: RecipesViewModel
) {
  var clickOnRecipe by remember { mutableStateOf(false) } // State to track if the recipe is clicked
  val cardColor =
      if (clickOnRecipe) MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.background
  val elevation = if (clickOnRecipe) 16.dp else 8.dp

  // The card that visually represents the recipe item
  ElevatedCard(
      colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
      elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
      modifier =
          Modifier.fillMaxWidth() // Make the card fill the available width
              .padding(horizontal = 16.dp, vertical = 8.dp) // Add padding around the card
              .clickable(
                  onClick = {
                    recipesViewModel.selectRecipe(recipe)
                    clickOnRecipe = true
                    navigationActions.navigateTo(
                        Screen.INDIVIDUAL_RECIPE) // Navigate to the individual recipe screen
                  }) // Handle clicks on the card
              .testTag("recipesCards")) {
        // Layout for the content inside the card
        Row(
            modifier =
                Modifier.fillMaxWidth() // Fill the width inside the card
                    .padding(2.dp)) {
              // Column for recipe details: name, servings, and time
              Column(
                  modifier =
                      Modifier.width(240.dp) // Set the width of the column
                          .size(80.dp) // Set the size of the column
                          .padding(vertical = 12.dp) // Add vertical padding inside the column
                          .padding(horizontal = 18.dp) // Add horizontal padding inside the column
                  ) {
                    // Display the recipe name with a specific font size, weight, and overflow
                    // handling
                    Text(
                        text = recipe.name,
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight(500),
                        maxLines = 1, // Ensure only one line is shown
                        overflow = TextOverflow.Ellipsis, // Show ellipsis when the text is too long
                        modifier = Modifier.fillMaxWidth() // Fill available width
                        )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)) // Add a small vertical space between elements

                    // Row for servings and time information
                    Row(
                        modifier =
                            Modifier.fillMaxWidth() // Fill the available width for the row
                                .fillMaxHeight() // Fill the available height for the row
                        ) {
                          // Display the servings information
                          Text(
                              "Servings : ${recipe.servings}",
                              overflow =
                                  TextOverflow.Ellipsis // Show ellipsis if the text overflows
                              )

                          Spacer(
                              modifier =
                                  Modifier.width(
                                      2.dp)) // Add a horizontal space between servings and time

                          // Display the total cooking time
                          Text(
                              "Time : ${recipe.time.inWholeMinutes} min",
                              overflow =
                                  TextOverflow.Ellipsis // Show ellipsis if the text overflows
                              )
                        }
                  }

              Spacer(modifier = Modifier.width(16.dp)) // Add space between the text and the image

              // Display an image for the recipe (using a placeholder image)
              Image(
                  painter =
                      painterResource(
                          R.drawable.recipe_placeholder_small), // Placeholder image resource
                  contentDescription = "Recipe Image", // Content description for accessibility
                  modifier =
                      Modifier.size(80.dp) // Set the size for the image
                          .clip(
                              RoundedCornerShape(
                                  8.dp)), // Optionally clip the image with rounded corners
                  contentScale = ContentScale.Fit // Fit the image to the available space
                  )
            }
      }
}
