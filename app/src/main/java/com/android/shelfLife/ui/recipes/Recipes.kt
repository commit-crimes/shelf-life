package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.android.shelfLife.ui.navigation.*
import com.android.shelfLife.ui.utils.CustomSearchBar
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.android.shelfLife.viewmodel.recipes.RecipesViewModel
import kotlinx.coroutines.launch

/**
 * The main screen for displaying and managing recipes in the app.
 *
 * The screen includes:
 * - A search bar for filtering recipes.
 * - A list of filtered recipes displayed as cards.
 * - Floating Action Buttons (FAB) for adding new recipes or generating recipes.
 * - Top and bottom navigation bars.
 *
 * @param navigationActions Navigation actions for managing screen transitions.
 */
@Composable
fun RecipesScreen(navigationActions: NavigationActions) {
    val recipesViewModel = hiltViewModel<RecipesViewModel>()
    val overviewScreenViewModel = hiltViewModel<OverviewScreenViewModel>()

    // Collecting necessary state values from the ViewModel
    val user = recipesViewModel.user.collectAsState()
    val selectedHousehold = recipesViewModel.household.collectAsState()
    val query by recipesViewModel.query.collectAsState()
    val filteredRecipeList by recipesViewModel.filteredRecipes.collectAsState()
    val selectedFilters by recipesViewModel.selectedFilters.collectAsState()
    val drawerState by overviewScreenViewModel.drawerState.collectAsState()
    val scope = rememberCoroutineScope()

    HouseHoldSelectionDrawer(
        scope = scope,
        drawerState = drawerState,
        navigationActions = navigationActions
    ) {
        if (selectedHousehold.value == null) {
            // Navigate to the first-time user screen if no household is selected
            navigationActions.navigateTo(Screen.FIRST_TIME_USER)
        } else {
            if (user.value != null) {
                Scaffold(
                    modifier = Modifier.testTag("recipesScreen"),
                    topBar = {
                        selectedHousehold.value?.let {
                            TopNavigationBar(
                                houseHold = it,
                                onHamburgerClick = { scope.launch { drawerState.open() } },
                                filters = recipesViewModel.filters,
                                selectedFilters = selectedFilters,
                                onFilterChange = { filter, _ -> recipesViewModel.clickOnFilter(filter) }
                            )
                        }
                    },
                    bottomBar = {
                        BottomNavigationMenu(
                            onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                            tabList = LIST_TOP_LEVEL_DESTINATION,
                            selectedItem = Route.RECIPES
                        )
                    },
                    floatingActionButton = {
                        // Floating Action Buttons for adding or generating recipes
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                        ) {
                            if (recipesViewModel.fabExpanded.value) {
                                // Secondary FAB for generating a recipe
                                ExtendedFloatingActionButton(
                                    text = { Text("Generate") },
                                    icon = {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "Generate")
                                    },
                                    onClick = {
                                        navigationActions.navigateTo(Screen.GENERATE_RECIPE)
                                        recipesViewModel.shrinkFab()
                                    },
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.testTag("generateRecipeFab").width(150.dp)
                                )
                            }
                            // Primary FAB for adding a recipe
                            ExtendedFloatingActionButton(
                                text = {
                                    Text(if (recipesViewModel.fabExpanded.value) "Manual" else "")
                                },
                                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                                onClick = {
                                    if (recipesViewModel.fabExpanded.value) {
                                        navigationActions.navigateTo(Screen.ADD_RECIPE)
                                        recipesViewModel.shrinkFab()
                                    } else {
                                        recipesViewModel.expandFab()
                                    }
                                },
                                expanded = recipesViewModel.fabExpanded.value,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.testTag("addRecipeFab")
                                    .width(if (recipesViewModel.fabExpanded.value) 150.dp else 56.dp)
                            )
                        }
                    },
                    content = { paddingValues ->
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { recipesViewModel.shrinkFab() })
                                }
                        ) {
                            // Search bar for filtering recipes
                            CustomSearchBar(
                                query = query,
                                onQueryChange = { newQuery -> recipesViewModel.changeQuery(newQuery) },
                                placeholder = "Search recipe",
                                searchBarTestTag = "searchBar",
                                onDeleteTextClicked = { recipesViewModel.changeQuery("") }
                            )
                            if (filteredRecipeList.isEmpty()) {
                                // Display a message if no recipes are available
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recipes available",
                                        modifier = Modifier.testTag("noRecipesAvailableText")
                                    )
                                }
                            } else {
                                // LazyColumn displaying the filtered recipes as cards
                                LazyColumn(modifier = Modifier.fillMaxSize().testTag("recipesList")) {
                                    items(filteredRecipeList) { recipe ->
                                        RecipeItem(recipe, navigationActions, recipesViewModel)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Displays a recipe item as a clickable card.
 *
 * @param recipe The recipe object containing its name, servings, and cooking time.
 * @param navigationActions Navigation controller for handling navigation events.
 * @param recipesViewModel The ViewModel managing the state of the recipes list.
 */
@Composable
fun RecipeItem(
    recipe: Recipe,
    navigationActions: NavigationActions,
    recipesViewModel: RecipesViewModel
) {
    var clickOnRecipe by remember { mutableStateOf(false) }
    val cardColor = if (clickOnRecipe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background
    val elevation = if (clickOnRecipe) 16.dp else 8.dp

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                recipesViewModel.selectRecipe(recipe)
                clickOnRecipe = true
                navigationActions.navigateTo(Screen.INDIVIDUAL_RECIPE)
            }
            .testTag("recipesCards")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .size(80.dp)
                    .padding(vertical = 12.dp, horizontal = 18.dp)
            ) {
                Text(
                    text = recipe.name,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(500),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                ) {
                    Text(
                        "Servings : ${recipe.servings}",
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Time : ${recipe.time.inWholeMinutes} min",
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(R.drawable.recipe_placeholder_small),
                contentDescription = "Recipe Image",
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}