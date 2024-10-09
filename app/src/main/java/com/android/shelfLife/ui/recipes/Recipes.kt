package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.filter


@Composable
fun RecipesScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel
) {
    // Collect the recipes StateFlow as a composable state
    val recipeList by listRecipesViewModel.recipes.collectAsState()


    // State for the search query
    var query by remember { mutableStateOf("") }

    // Filter the recipes based on the search query
    val filteredRecipes = if (query.isEmpty()) {
        recipeList // Use the collected recipe list
    } else {
        recipeList.filter { recipe ->
            recipe.name.contains(query, ignoreCase = true) // Filter by recipe name
        }
    }

    Scaffold(
        modifier = Modifier,
        topBar = { TopNavigationBar() },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesSearchBar(query: String, onQueryChange: (String) -> Unit) {
    var isActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // Set a fixed height for the search bar
    ){
        SearchBar(
            query = query, // The current query string
            onQueryChange = { newQuery -> onQueryChange(newQuery) }, // Use the passed function to update the query
            placeholder = {
                Text("Search recipes")
            },
            onSearch = {},
            active = isActive,
            onActiveChange = { active -> isActive = active },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            trailingIcon = {
                IconButton(onClick = { isActive = false }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "icon for recipes search bar"
                    )
                }
            }
        ) {}
    }
} // todo ask Paul where does the filter management comes into to play, the screen or the top bar

@Composable
fun RecipeItem(recipe: Recipe, navigationActions: NavigationActions, listRecipesViewModel: ListRecipesViewModel) {
    var clickOnRecipe by remember { mutableStateOf(false) }

    Card(modifier = Modifier
        .fillMaxWidth() // Fill the available width
        .padding(8.dp) // Add padding around the card
        .clickable(onClick = {clickOnRecipe = true})
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp), // Padding inside the card
        ){
            Column(
                modifier = Modifier
                    .width(275.dp)
                    .size(80.dp)
                    .padding(vertical = 14.dp)
                    .padding(horizontal = 18.dp)
            ) {
                Text(text = recipe.name,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(500),
                    overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()){
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



    if(clickOnRecipe){
        clickOnRecipe = false // If I don't return the value to false, it would navigate twice to the IndividualRecipeScreen
        listRecipesViewModel.selectRecipe(recipe)
        navigationActions.navigateTo(Screen.INDIVIDUAL_RECIPE)
    }

}

fun getTotalMinutes(timestamp: Timestamp): Int {
    return (timestamp.seconds / 60).toInt() // Convert seconds to minutes
}

