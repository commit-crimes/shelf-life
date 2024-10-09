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
import com.android.shelfLife.ui.utils.getTotalMinutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * Displays the detailed view of a selected recipe, including its name, image, servings, time, and instructions.
         * This screen allows navigation back to the previous screen and provides a top and bottom navigation bar.
         *
         * @param navigationActions Handles navigation between screens (e.g., go back, navigate to other destinations).
         * @param listRecipesViewModel ViewModel containing the list of recipes and the selected recipe data.
         */
fun IndividualRecipeScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel
) {
    // Retrieve the currently selected recipe from the ViewModel.
    // If no recipe is selected, display an error message.
    val selectedRecipe =
        listRecipesViewModel.selectedRecipe.collectAsState().value
            ?: return Text(text = "No recipe selected. Should not happen", color = Color.Red)

    // Scaffold that provides the structure for the screen, including top and bottom bars.
    Scaffold(
        topBar = { TopNavigationBar() },  // Custom top navigation bar (if any)
        bottomBar = {
            // Bottom navigation bar for switching between main app destinations.
            BottomNavigationMenu(
                onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                tabList = LIST_TOP_LEVEL_DESTINATION,  // List of top-level destinations
                selectedItem = Route.RECIPES  // The currently selected item in the bottom navigation
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)  // Apply padding provided by Scaffold
                    .fillMaxSize()  // Fill the available space
            ) {
                // Additional top app bar for navigation back
                TopAppBar(
                    navigationIcon = {
                        // Back button to return to the previous screen
                        IconButton(onClick = { navigationActions.goBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back Icon"
                            )
                        }
                    },
                    // Title of the screen: Recipe name
                    title = {
                        Text(
                            text = selectedRecipe.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                )

                // Recipe content: image, servings, time, and instructions
                Column(
                    modifier = Modifier
                        .padding(8.dp)  // Padding around the content
                        .fillMaxSize()  // Fill the remaining screen space
                        .verticalScroll(rememberScrollState())  // Enable vertical scrolling
                ) {
                    // Display the recipe image (placeholder for now)
                    Image(
                        painter = painterResource(R.drawable.google_logo),
                        contentDescription = "Recipe Image",
                        modifier = Modifier
                            .width(537.dp)  // Set the image width
                            .height(100.dp),  // Set the image height
                        contentScale = ContentScale.FillWidth  // Make the image fill the width
                    )

                    // Row displaying servings and time information
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Servings: ${selectedRecipe.servings}")  // Display servings
                        Spacer(modifier = Modifier.width(16.dp))  // Add space between text
                        Text(text = "Time: ${getTotalMinutes(selectedRecipe.time)} min")  // Display total time
                    }

                    Spacer(modifier = Modifier.height(16.dp))  // Add space before instructions

                    // Display recipe instructions, scrollable if long
                    Text(
                        text = selectedRecipe.instructions,
                        modifier = Modifier.padding(vertical = 8.dp)  // Add padding around instructions
                    )
                }
            }
        }
    )
}
