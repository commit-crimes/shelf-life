package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.TopNavigationBar

@Composable
fun OverviewScreen(navigationActions : NavigationActions) {
    Scaffold(
        modifier = Modifier.testTag("overviewScreen"),
        topBar = { TopNavigationBar() },
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
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchBar(
    query : String = "Search food item",
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
        leadingIcon = {

        },
        trailingIcon = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "" // Add a valid content description
                )
            }
        }
    ) { }
}

@Preview
@Composable
fun OverviewScreenPreview() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    OverviewScreen(navigationActions)
}
