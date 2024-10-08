package com.android.shelfLife.ui.overview

import HouseholdViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.android.shelfLife.ui.component.AddHouseholdDialogContent
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.newSingleThreadContext
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun OverviewScreen(navigationActions : NavigationActions,
                   listFoodItemsViewModel: ListFoodItemsViewModel,
                   householdViewModel: HouseholdViewModel) {
    val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
    val foodItems = selectedHousehold?.foodItems ?: emptyList()
    Scaffold(
        modifier = Modifier.testTag("overviewScreen"),
        topBar = { TopNavigationBar(
            userHouseholds = householdViewModel.households.collectAsState().value,
            onHouseholdChange = { household -> householdViewModel.selectHousehold(household) },
            houseHold =  selectedHousehold ?: HouseHold(
                uid = "default",
                name = "No Household Selected",
                members = emptyList(),
                foodItems = emptyList()
            ), // Safely handle the null case,
            householdViewModel = householdViewModel
        ) },
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
            Box(modifier = Modifier.padding(paddingValues)) {
                FoodSearchBar()
            }
            Box(modifier = Modifier.padding(paddingValues)) {
                ListFoodItems(foodItems, listFoodItemsViewModel, navigationActions)
            }
        }
    )
}

@Composable
fun ListFoodItems(foodItems : List <FoodItem>, listFoodItemsViewModel: ListFoodItemsViewModel, navigationActions : NavigationActions) {
    if (foodItems.isEmpty()) {
        // Display a prompt when there are no todos
        Box(
            modifier = Modifier.fillMaxSize().testTag("NoFoodItems"),
            contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(text = "No food available")
        }
    } else {
        // Display the full list of to-dos
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(foodItems) { item ->
                // Call a composable that renders each individual to-do item
                FoodItemCard(foodItem = item, listFoodItemsViewModel = listFoodItemsViewModel, navigationActions = navigationActions)
            }
        }
    }
}

@Composable
fun FoodItemCard(foodItem : FoodItem, listFoodItemsViewModel: ListFoodItemsViewModel, navigationActions : NavigationActions){
    val expiryDate = foodItem.expiryDate
    val formattedExpiryDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(expiryDate)
    Column(
        modifier =
        Modifier.fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(Color.White) // Add background color if needed
            .padding(16.dp)) {
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
            Text(text = "Expires on $formattedExpiryDate", fontSize = 12.sp, color = Color.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchBar(
    query : String = "Search food item",
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Outer padding for spacing
        contentAlignment = Alignment.Center  // Center the SearchBar within the Box
    ) {
        SearchBar(
            query = "",
            onQueryChange = {},
            placeholder = {
                Text(query)
            },
            onSearch = {},
            active = false,
            onActiveChange = {},
            leadingIcon = {},
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "" // Add a valid content description
                    )
                }
            },
            modifier = Modifier
                .widthIn(max = 600.dp) // Restrict max width to prevent over-stretching on large screens
                .fillMaxWidth(0.9f)   // Make it responsive and occupy 90% of available width
        ) { }
    }
}


@Preview
@Composable
fun OverviewScreenPreview() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val foodItemRepositry = FoodItemRepositoryFirestore(firebaseFirestore)
    val listFoodItemViewModel = ListFoodItemsViewModel(foodItemRepositry)
    val householdViewModel = HouseholdViewModel(HouseholdRepositoryFirestore(firebaseFirestore), listFoodItemViewModel)
    OverviewScreen(navigationActions, listFoodItemViewModel, householdViewModel)
}
