package com.android.shelfLife.ui.overview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.HouseHoldElement
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.TopNavigationBar
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Composable function to display the overview screen
 *
 * @param navigationActions The actions to handle navigation
 * @param listFoodItemsViewModel The ViewModel for the list of food items
 * @param householdViewModel The ViewModel for the households the user has access to
 */
@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
    listFoodItemsViewModel: ListFoodItemsViewModel,
    householdViewModel: HouseholdViewModel
) {
    val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val foodItems = selectedHousehold?.foodItems ?: emptyList()
    val userHouseholds = householdViewModel.households.collectAsState().value

    var showDialog by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val filters = listOf("Dairy", "Meat", "Fish", "Fruit", "Vegetables", "Bread", "Canned")

    AddHouseHoldPopUp(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        householdViewModel = householdViewModel,
    )

    EditHouseHoldPopUp(
        showDialog = showEdit,
        onDismiss = { showEdit = false },
        householdViewModel = householdViewModel
    )

    ModalNavigationDrawer(
        modifier = Modifier.testTag("householdSelectionDrawer"),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Text(
                    "Household selection",
                    modifier =
                    Modifier.padding(vertical = 18.dp, horizontal = 16.dp)
                        .padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelMedium
                )
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
                            }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        modifier = Modifier.testTag("addHouseholdIcon"),
                        onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Household Icon",
                        )
                    }

                    IconButton(
                        modifier = Modifier.testTag("editHouseholdIcon"),
                        onClick = { showEdit = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Household Icon",
                        )
                    }
                }
            }
        },
    ) {
        val filteredFoodItems =
            foodItems.filter { it.foodFacts.name.contains(searchQuery, ignoreCase = true) }

        if (selectedHousehold == null) {
            FirstTimeWelcomeScreen(householdViewModel)
        } else {
            Scaffold(
                modifier = Modifier.testTag("overviewScreen"),
                topBar = {
                    selectedHousehold?.let {
                        TopNavigationBar(
                            houseHold = it,
                            onHamburgerClick = { scope.launch { drawerState.open() } },
                            filters = filters
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
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navigationActions.navigateTo(Screen.ADD_FOOD) },
                        content = { Icon(Icons.Default.Add, contentDescription = "Add") },
                        modifier = Modifier.testTag("addFoodFab"),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                },
                content = { paddingValues ->
                    Column(
                        modifier = Modifier.padding(paddingValues),
                    ) {
                        FoodSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it } // Update the query state when the user types
                        )
                        ListFoodItems(filteredFoodItems)
                    }
                })
        }
    }
}

/**
 * Composable function to display the list of food items
 *
 * @param foodItems The list of food items to display
 */
@Composable
fun ListFoodItems(foodItems: List<FoodItem>) {
    if (foodItems.isEmpty()) {
        // Display a prompt when there are no food items
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("NoFoodItems"),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No food available")
        }
    } else {
        // Display the full list
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .testTag("foodItemList")
        ) {
            items(foodItems) { item ->
                // Call a composable that renders each individual food item
                FoodItemCard(foodItem = item)
            }
        }
    }
}

/**
 * Composable function to display a single food item card
 *
 * @param foodItem The food item to display
 */
@Composable
fun FoodItemCard(foodItem: FoodItem) {
    val expiryDate = foodItem.expiryDate
    Log.d("FoodItemCard", "Expiry Date: $expiryDate")
    val formattedExpiryDate =
        expiryDate?.toDate()?.let { SimpleDateFormat("MM dd, yyyy", Locale.getDefault()).format(it) }
            ?: "No Expiry Date"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(Color.White) // Add background color if needed
            .padding(16.dp)
            .testTag("foodItemCard"),
    ) {
        // First Row for Date and Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = foodItem.foodFacts.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Text(text = foodItem.foodFacts.quantity.toString() + " in stock")
            // Display the due date on the left
            Text(text = formattedExpiryDate, fontSize = 12.sp, color = Color.Black)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Display the remaining days until expiry in the middle
            Text(text = "Expires on $formattedExpiryDate", fontSize = 12.sp, color = Color.Black)
        }
    }
}

/**
 * Composable function to display the search bar for filtering food items
 *
 * @param query The current query string
 * @param onQueryChange The callback to update the query string
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchBar(query: String, onQueryChange: (String) -> Unit) {
    var active by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("foodSearchBar"),
        contentAlignment = Alignment.Center
    ) {
        SearchBar(
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
            shadowElevation = 3.dp,
            query = query,
            onQueryChange = onQueryChange,
            placeholder = { Text("Search food item") },
            onSearch = { /* Optional: Handle search action if needed */ },
            active = active,
            onActiveChange = { active = it },
            leadingIcon = {},
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                }
            },
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(0.9f)
                .testTag("searchBar")
        ) {}
    }
}

/**
 * Composable function to display the first time welcome screen for the user to create a new
 *
 * @param householdViewModel The ViewModel for the households the user has access to
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstTimeWelcomeScreen(householdViewModel: HouseholdViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("firstTimeWelcomeScreen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Text
        Text(
            text = "Welcome to ShelfLife!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle Text
        Text(
            text = "Get started by naming your Household",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // State for household name
        var householdName by remember { mutableStateOf("") }

        // Text Field for entering household name
        OutlinedTextField(
            value = householdName,
            onValueChange = { newValue -> householdName = newValue },
            label = { Text("Enter Household name") },
            modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 32.dp)
                .testTag("householdNameTextField"),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors =
            TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Create Household Button
        Button(
            onClick = { householdViewModel.addNewHousehold(householdName) },
            enabled = householdName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp)
                .testTag("householdNameSaveButton"),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Create Household", style = MaterialTheme.typography.labelLarge)
        }
    }
}