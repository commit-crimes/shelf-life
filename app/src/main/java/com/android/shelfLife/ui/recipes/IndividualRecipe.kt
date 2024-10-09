package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.TopNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualRecipeScreen(navigationActions: NavigationActions, listRecipesViewModel: ListRecipesViewModel) {

    val selectedRecipe =
        listRecipesViewModel.selectedRecipe.collectAsState().value
            ?: return Text(text = "No ToDo selected. Should not happen", color = Color.Red)

    Scaffold(
        topBar = { TopNavigationBar()},
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
                // Second TopAppBar below the first one
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { navigationActions.goBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back Icon"
                            )
                        }
                    },
                    title = {Text(text = selectedRecipe.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    }
                )

                // Content of the recipe screen
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Add vertical scroll functionality
                ) {

                    Image(painter = painterResource(R.drawable.google_logo),
                        contentDescription = "Recipe Image",
                        modifier = Modifier
                            .width(537.dp)
                            .height(100.dp),
                        contentScale = ContentScale.FillWidth // Fit the image to fit the size
                    )


                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Servings: ${selectedRecipe.servings}")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Time: ${getTotalMinutes(selectedRecipe.time)} min")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Making the instructions text scrollable
                    Text(
                        text = selectedRecipe.instructions,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    )
}
