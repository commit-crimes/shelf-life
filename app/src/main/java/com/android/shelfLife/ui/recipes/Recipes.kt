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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.android.shelfLife.R
import com.android.shelfLife.model.recipe.recipe
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.google.firebase.Timestamp
import androidx.compose.ui.Alignment.Companion.CenterVertically as CenterVertically1


@Composable
fun RecipesScreen(
    navigationActions: NavigationActions
){

    val listOfRecipes = listOf(
        recipe(name= "Paella",
            instructions = "cook",
            servings = 4,
            time = Timestamp(5400,0)),
        recipe(name = "Fideua",
            instructions = "cry",
            servings = 3,
            time = Timestamp(3600, 0)),
        recipe(name= "Tortilla de patata",
            instructions = "cook",
            servings = 4,
            time = Timestamp(5400,0)),
        recipe(name = "Costillas a la brasa",
            instructions = "cry",
            servings = 3,
            time = Timestamp(3600, 0)),
        recipe(name= "Curry rojo",
            instructions = "cook",
            servings = 4,
            time = Timestamp(5400,0)),
        recipe(name = "Butifarra con boniato",
            instructions = "cry",
            servings = 3,
            time = Timestamp(3600, 0))
    ) // this is to test for the moment, we will later use the firebase and GPT-API

    Scaffold(
        modifier = Modifier, // todo we will place a testTag
        topBar = { TopNavigationBar()},
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = {destination -> navigationActions.navigateTo(destination)},
                tabList =  LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.RECIPES)
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize() // Fill the available size
            ) {
                RecipesSearchBar() // Search bar at the top

                // LazyColumn for displaying the list of recipes
                LazyColumn(
                    modifier = Modifier.fillMaxSize() // Fill remaining space
                ) {
                    items(listOfRecipes) { recipe ->
                        RecipeItem(recipe)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesSearchBar() {
    // State to track the current query and whether the search bar is active
    var query by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }

    SearchBar(
        query = query, // The current query string
        onQueryChange = { newQuery ->
            query = newQuery // Update the query when user types
        },
        placeholder = {
            Text("Search recipes") // Placeholder when query is empty
        },
        onSearch = {
            // You can handle search logic here if needed
        },
        active = isActive, // Track if the search bar is active
        onActiveChange = { active ->
            isActive = active // Update the active state
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        leadingIcon = {
            // You can customize the leading icon here if needed
        },
        trailingIcon = {
            IconButton(onClick = { /* Trigger search action if needed */ }) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "icon for recipes search bar"
                )
            }
        }
    ) {
        // Additional content can be added here if required
    }
} // todo finish this

@Preview
@Composable
fun RecipesScreenOverview() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    RecipesScreen(navigationActions)
}

@Composable
fun RecipeItem(recipe: recipe) {

    Card(modifier = Modifier
        .fillMaxWidth() // Fill the available width
        .padding(8.dp), // Add padding around the card
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Padding inside the card
        ){
            Column(
                modifier = Modifier
                    .width(275.dp)
                    .size(80.dp)
                    .padding(vertical = 16.dp)
                    .padding(horizontal = 18.dp)
            ) {
                Text(text = recipe.name,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(500),
                    overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()){
                    Text("Servings : ${recipe.servings}",
                        overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Time : ${getTotalMinutes(recipe.time)} min",
                        overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Image(painter = painterResource(R.drawable.google_logo),
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .size(80.dp) // Fixed size for the image
                    .clip(RoundedCornerShape(4.dp)),// Optional: clip the image to rounded corners
                contentScale = ContentScale.Fit // Fit the image to fit the size
                )
        }
    }

}

fun getTotalMinutes(timestamp: Timestamp): Int {
    return (timestamp.seconds / 60).toInt() // Convert seconds to minutes
}

