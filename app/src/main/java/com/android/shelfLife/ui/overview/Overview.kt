package com.android.shelfLife.ui.overview

import android.annotation.SuppressLint
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
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.HouseHoldElement
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun OverviewScreen(navigationActions : NavigationActions,
                   listFoodItemsViewModel: ListFoodItemsViewModel,
                   householdViewModel: HouseholdViewModel
) {
    val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val foodItems = selectedHousehold?.foodItems ?: emptyList()
    val userHouseholds = householdViewModel.households.collectAsState().value

    var showDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    AddHouseHoldPopUp(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        householdViewModel = householdViewModel,
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Household selection",
                    modifier = Modifier
                        .padding(vertical = 18.dp, horizontal = 16.dp)
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
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Household Icon",
                            modifier = Modifier.testTag("addHouseholdIcon")
                        )
                    }
                }
            }
        },
    ) {
        val filteredFoodItems = foodItems.filter {
            it.foodFacts.name.contains(searchQuery, ignoreCase = true)
        }

        if (selectedHousehold == null) {
            FirstTimeWelcomeScreen(householdViewModel)
        } else {
            Scaffold(
                modifier = Modifier.testTag("overviewScreen"),
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
                            onHamburgerClick = { scope.launch { drawerState.open() } }
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
                        modifier = Modifier.testTag("AddFoodFab"),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                },
                content = { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        FoodSearchBar(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                            } // Update the query state when the user types
                        )
                        ListFoodItems(filteredFoodItems, listFoodItemsViewModel, navigationActions)
                    }
                }
            )
        }
    }
}

    @Composable
    fun ListFoodItems(
        foodItems: List<FoodItem>,
        listFoodItemsViewModel: ListFoodItemsViewModel,
        navigationActions: NavigationActions
    ) {
        if (foodItems.isEmpty()) {
            // Display a prompt when there are no todos
            Box(
                modifier = Modifier.fillMaxSize().testTag("NoFoodItems"),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No food available")
            }
        } else {
            // Display the full list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(foodItems) { item ->
                    // Call a composable that renders each individual to-do item
                    FoodItemCard(
                        foodItem = item,
                        listFoodItemsViewModel = listFoodItemsViewModel,
                        navigationActions = navigationActions
                    )
                }
            }
        }
    }

    @Composable
    fun FoodItemCard(
        foodItem: FoodItem,
        listFoodItemsViewModel: ListFoodItemsViewModel,
        navigationActions: NavigationActions
    ) {
        val expiryDate = foodItem.expiryDate
        val formattedExpiryDate =
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(expiryDate)
        Column(
            modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .background(Color.White) // Add background color if needed
                .padding(16.dp)
        ) {
            // First Row for Date and Status
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = foodItem.foodFacts.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = foodItem.foodFacts.quantity.toString() + "in stock"
                )
                // Display the due date on the left
                Text(text = formattedExpiryDate, fontSize = 12.sp, color = Color.Black)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Display the remaining days until expiry in the middle
                Text(
                    text = "Expires on $formattedExpiryDate",
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FoodSearchBar(
        query: String,
        onQueryChange: (String) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Outer padding for spacing
            contentAlignment = Alignment.Center  // Center the SearchBar within the Box
        ) {
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = {
                    Text("Search food item")
                },
                onSearch = { /* Optional: Handle search action if needed */ },
                active = false,
                onActiveChange = {},
                leadingIcon = {},
                trailingIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    }
                },
                modifier = Modifier
                    .widthIn(max = 600.dp) // Restrict max width to prevent over-stretching on large screens
                    .fillMaxWidth(0.9f)   // Make it responsive and occupy 90% of available width
            ) {}
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FirstTimeWelcomeScreen(householdViewModel: HouseholdViewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.outlinedTextFieldColors(
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
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Create Household",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }


