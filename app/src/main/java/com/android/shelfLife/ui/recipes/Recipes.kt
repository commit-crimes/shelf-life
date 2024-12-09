package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.android.shelfLife.ui.overview.FirstTimeWelcomeScreen
import com.android.shelfLife.ui.utils.CustomSearchBar
import com.android.shelfLife.ui.utils.ExpandableFAB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
    householdViewModel: HouseholdViewModel,
) {
  // Collect the recipes StateFlow as a composable state
  val recipeList by listRecipesViewModel.recipes.collectAsState()

  // State for the search query
  var query by remember { mutableStateOf("") }
  var selectedFilters = remember { mutableStateListOf<String>() }

  val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
  val userHouseholds = householdViewModel.households.collectAsState().value

  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val filters =
      listOf("Soon to expire", "Only household items", "High protein", "Low calories", "Personal")

  var fabExpanded = remember { mutableStateOf(false) }

  HouseHoldSelectionDrawer(
      scope = scope,
      drawerState = drawerState,
      householdViewModel = householdViewModel,
      navigationActions = navigationActions) {

        // filtering recipeList using the filter and the query from the searchBar
        val filteredRecipes = filterRecipes(recipeList, selectedFilters, query)

        if (selectedHousehold == null) {
          FirstTimeWelcomeScreen(navigationActions, householdViewModel)
        } else {
          Scaffold(
              modifier = Modifier.testTag("recipesScreen"),
              topBar = {
                selectedHousehold?.let {
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
                    selectedItem = Route.RECIPES)
              },
              // Floating Action Button to add a new food item
              floatingActionButton = {
                ExpandableFAB(fabExpanded = fabExpanded, navigationActions = navigationActions)
              },
              content = { paddingValues ->
                Column(
                    modifier =
                        Modifier.padding(paddingValues).fillMaxSize().pointerInput(Unit) {
                          detectTapGestures(
                              onTap = { if (fabExpanded.value) fabExpanded.value = false })
                        }) {
                      CustomSearchBar(
                          query = query,
                          onQueryChange = { query = it },
                          placeholder = "Search recipe",
                          searchBarTestTag = "searchBar",
                          onDeleteTextClicked = { query = "" })

                      if (filteredRecipes.isEmpty()) {
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
                          items(filteredRecipes) { recipe ->
                            RecipeItem(recipe, navigationActions, listRecipesViewModel)
                          }
                        }
                      }
                    }
              })
        }
      }
}
/**
 * Converts a string representation of a recipe type into a corresponding
 * `RecipesRepository.SearchRecipeType` enumeration value.
 *
 * @param string A string describing the type of recipe to search for. Possible values include:
 *     - "Soon to expire": Recipes with ingredients that are nearing expiration.
 *     - "Only household items": Recipes using only ingredients available in the household.
 *     - "High protein": Recipes with a high protein content.
 *     - "Low calories": Recipes with low caloric content.
 *
 * @return The corresponding `RecipesRepository.SearchRecipeType` enum value.
 * @throws IllegalArgumentException If the input string does not match any known recipe type.
 */
fun stringToSearchRecipeType(string: String): RecipeType {
  return when (string) {
    "Soon to expire" -> RecipeType.USE_SOON_TO_EXPIRE
    "Only household items" -> RecipeType.USE_ONLY_HOUSEHOLD_ITEMS
    "High protein" -> RecipeType.HIGH_PROTEIN
    "Low calories" -> RecipeType.LOW_CALORIE
    "Personal" -> RecipeType.PERSONAL
    else -> throw IllegalArgumentException("Unknown filter: $string")
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
    listRecipesViewModel: ListRecipesViewModel
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
              .clickable(onClick = { clickOnRecipe = true }) // Handle clicks on the card
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
                  painter = painterResource(R.drawable.google_logo), // Placeholder image resource
                  contentDescription = "Recipe Image", // Content description for accessibility
                  modifier =
                      Modifier.size(80.dp) // Set the size for the image
                          .clip(
                              RoundedCornerShape(
                                  4.dp)), // Optionally clip the image with rounded corners
                  contentScale = ContentScale.Fit // Fit the image to the available space
                  )
            }
      }

  // Handle navigation to the individual recipe screen when the card is clicked
  if (clickOnRecipe) {
    clickOnRecipe = false // Reset the click state to prevent multiple navigations
    listRecipesViewModel.selectRecipe(recipe) // Select the clicked recipe in the ViewModel
    navigationActions.navigateTo(
        Screen.INDIVIDUAL_RECIPE) // Navigate to the individual recipe screen
  }
}

/**
 * Filters a list of recipes based on selected filters and a search query.
 *
 * @param listRecipes The complete list of recipes to be filtered.
 * @param selectedFilters A list of filter criteria. Each filter is matched against the recipe's
 *   types. If the list is empty, no filter is applied based on this parameter.
 * @param query A search string used to filter recipes by their names. If the string is empty, no
 *   filtering is applied based on this parameter.
 * @return A list of recipes that match both the selected filters and the search query.
 *     - Recipes are included if they match at least one filter from `selectedFilters` (if
 *       provided).
 *     - Recipes are included if their names contain the `query` string (case-insensitive, if
 *       provided).
 *     - If both `selectedFilters` and `query` are empty, the original list is returned without any
 *       filtering.
 */
fun filterRecipes(
    listRecipes: List<Recipe>,
    selectedFilters: List<String>,
    query: String
): List<Recipe> {
  // Combined filtering based on selected filters and search query
  val filteredRecipes =
      listRecipes.filter { recipe ->
        // Check if recipe matches selected filters
        (selectedFilters.isEmpty() ||
            selectedFilters.any { filter ->
              recipe.recipeType == stringToSearchRecipeType(filter)
            }) &&
            // Check if recipe matches the search query
            (query.isEmpty() || recipe.name.contains(query, ignoreCase = true))
      }

  return filteredRecipes
}
