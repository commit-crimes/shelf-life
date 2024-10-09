package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.HouseHoldElement
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.android.shelfLife.ui.overview.AddHouseHoldPopUp
import com.android.shelfLife.ui.overview.EditHouseHoldPopUp
import com.android.shelfLife.ui.overview.FirstTimeWelcomeScreen
import com.android.shelfLife.ui.utils.getTotalMinutes
import kotlinx.coroutines.launch


@Composable
fun RecipesScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
    householdViewModel: HouseholdViewModel
) {
    // Collect the recipes StateFlow as a composable state
    val recipeList by listRecipesViewModel.recipes.collectAsState()


    // State for the search query
    var query by remember { mutableStateOf("") }


    val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
    val userHouseholds = householdViewModel.households.collectAsState().value

    var showDialog by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    AddHouseHoldPopUp(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        householdViewModel = householdViewModel,
    )

    EditHouseHoldPopUp(
        showDialog = showEdit,
        onDismiss = { showEdit = false },
        householdViewModel = householdViewModel)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Household selection",
                    modifier =
                    Modifier.padding(vertical = 18.dp, horizontal = 16.dp)
                        .padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelMedium)
                userHouseholds.forEach { household ->
                    selectedHousehold?.let {
                        HouseHoldElement(
                            household = household,
                            selectedHousehold = it,
                            onHouseholdSelected = { household ->
                                if (household != selectedHousehold) {
                                    householdViewModel.selectHousehold(household)
                                }
                                scope.launch { drawerState.close() }
                            })
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Household Icon",
                            modifier = Modifier.testTag("addHouseholdIcon"))
                    }

                    IconButton(onClick = { showEdit = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Household Icon",
                            modifier = Modifier.testTag("editHouseholdIcon"))
                    }
                }
            }
        },
    ){
        // Filter the recipes based on the search query
        val filteredRecipes = if (query.isEmpty()) {
            recipeList // Use the collected recipe list
        } else {
            recipeList.filter { recipe ->
                recipe.name.contains(query, ignoreCase = true) // Filter by recipe name
            }
        }

        if (selectedHousehold == null) {
            FirstTimeWelcomeScreen(householdViewModel)
        } else{
            Scaffold(
                modifier = Modifier,
                topBar = {
                    selectedHousehold?.let {
                        TopNavigationBar(
                            userHouseholds = householdViewModel.households.collectAsState().value,
                            onHouseholdChange = { household ->
                                if (household != selectedHousehold) {
                                    householdViewModel.selectHousehold(household)
                                }
                            },
                            houseHold = it,
                            householdViewModel = householdViewModel,
                            onHamburgerClick = { scope.launch { drawerState.open() } })
                    }
                },
                bottomBar = {
                    BottomNavigationMenu(
                        onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                        tabList = LIST_TOP_LEVEL_DESTINATION,
                        selectedItem = Route.RECIPES
                    )
                },
                content = { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        RecipesSearchBar(query) { newQuery ->
                            query = newQuery // Update the query when user types
                        } // Pass query and update function to the search bar

                        // LazyColumn for displaying the list of filtered recipes
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredRecipes) { recipe ->
                                RecipeItem(recipe, navigationActions, listRecipesViewModel)
                            }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * A composable function that displays a search bar for filtering recipes.
         *
         * @param query The current search query as a string.
         * @param onQueryChange A callback function that gets triggered whenever the query changes,
         *                      updating the query state in the parent composable.
         *
         * This search bar provides a text input field where users can type to search for recipes.
         * The UI layout consists of a box container that holds the search bar. It uses a `SearchBar`
         * from the Material3 library, with options for managing its active state and search behavior.
         *
         * - The `query` parameter represents the current text in the search bar.
         * - The `onQueryChange` function is invoked each time the user updates the search query,
         *   allowing the parent composable to filter recipes based on user input.
         * - The search bar displays a placeholder "Search recipes" when the query is empty.
         * - The trailing icon (search icon) allows users to toggle the search bar's active state.
         */
fun RecipesSearchBar(query: String, onQueryChange: (String) -> Unit) {
    var isActive by remember { mutableStateOf(false) }  // State to manage whether the search bar is active

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // Set a fixed height for the search bar container
    ) {
        SearchBar(
            query = query, // The current query string displayed in the search bar
            onQueryChange = { newQuery -> onQueryChange(newQuery) }, // Updates the query when the user types
            placeholder = {
                Text("Search recipes") // Placeholder text shown when the query is empty
            },
            onSearch = {
                // Logic to handle the search action can be added here if necessary
            },
            active = isActive,  // Determines whether the search bar is in an active state
            onActiveChange = { active -> isActive = active },  // Callback to update the active state
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),  // Padding around the search bar
            trailingIcon = {
                IconButton(onClick = { isActive = false }) { // Button to deactivate the search bar
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "icon for recipes search bar" // Accessibility description for the search icon
                    )
                }
            }
        ) {}
    }
}
 // todo ask Paul where does the filter management comes into to play, the screen or the top bar

@Composable
        /**
         * Displays a recipe item as a card that the user can click to navigate to the recipe's details.
         *
         * @param recipe The recipe object that contains the recipe data (name, servings, time, etc.).
         * @param navigationActions A navigation controller to handle navigation events (e.g., navigating to the individual recipe screen).
         * @param listRecipesViewModel The ViewModel that manages the state and logic for the recipe list.
         */
fun RecipeItem(recipe: Recipe, navigationActions: NavigationActions,
               listRecipesViewModel: ListRecipesViewModel) {
    var clickOnRecipe by remember { mutableStateOf(false) }  // State to track if the recipe is clicked

    // The card that visually represents the recipe item
    Card(
        modifier = Modifier
            .fillMaxWidth()  // Make the card fill the available width
            .padding(8.dp)  // Add padding around the card
            .clickable(onClick = { clickOnRecipe = true })  // Handle clicks on the card
    ) {
        // Layout for the content inside the card
        Row(
            modifier = Modifier
                .fillMaxWidth()  // Fill the width inside the card
                .padding(18.dp)  // Add padding inside the card for layout spacing
        ) {
            // Column for recipe details: name, servings, and time
            Column(
                modifier = Modifier
                    .width(275.dp)  // Set the width of the column
                    .size(80.dp)  // Set the size of the column
                    .padding(vertical = 14.dp)  // Add vertical padding inside the column
                    .padding(horizontal = 18.dp)  // Add horizontal padding inside the column
            ) {
                // Display the recipe name with a specific font size, weight, and overflow handling
                Text(
                    text = recipe.name,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(500),
                    overflow = TextOverflow.Ellipsis  // If the text is too long, show ellipsis (...)
                )

                Spacer(modifier = Modifier.height(4.dp))  // Add a small vertical space between elements

                // Row for servings and time information
                Row(
                    modifier = Modifier
                        .fillMaxWidth()  // Fill the available width for the row
                        .fillMaxHeight()  // Fill the available height for the row
                ) {
                    // Display the servings information
                    Text(
                        "Servings : ${recipe.servings}",
                        overflow = TextOverflow.Ellipsis  // Show ellipsis if the text overflows
                    )

                    Spacer(modifier = Modifier.width(8.dp))  // Add a horizontal space between servings and time

                    // Display the total cooking time
                    Text(
                        "Time : ${getTotalMinutes(recipe.time)} min",
                        overflow = TextOverflow.Ellipsis  // Show ellipsis if the text overflows
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))  // Add space between the text and the image

            // Display an image for the recipe (using a placeholder image)
            Image(
                painter = painterResource(R.drawable.google_logo),  // Placeholder image resource
                contentDescription = "Recipe Image",  // Content description for accessibility
                modifier = Modifier
                    .size(80.dp)  // Set the size for the image
                    .clip(RoundedCornerShape(4.dp)),  // Optionally clip the image with rounded corners
                contentScale = ContentScale.Fit  // Fit the image to the available space
            )
        }
    }

    // Handle navigation to the individual recipe screen when the card is clicked
    if (clickOnRecipe) {
        clickOnRecipe = false  // Reset the click state to prevent multiple navigations
        listRecipesViewModel.selectRecipe(recipe)  // Select the clicked recipe in the ViewModel
        navigationActions.navigateTo(Screen.INDIVIDUAL_RECIPE)  // Navigate to the individual recipe screen
    }
}
