package com.android.shelfLife.ui.recipes

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.android.shelfLife.model.recipe.recipe
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualRecipeScreen(navigationActions: NavigationActions, selectedRecipe: recipe) {

    Scaffold(
        modifier = Modifier.padding(horizontal = 8.dp),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back Icon"
                        )
                    }
                },
                title = {
                    Text("Recipe Details")
                }
            )
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
                // Second TopAppBar below the first one
                TopAppBar(
                    title = {
                        Text(selectedRecipe.name)
                    }
                )

                // Content of the recipe screen
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Add vertical scroll functionality
                ) {
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

@Preview
@Composable
fun IndividualRecipeScreenOverview() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    IndividualRecipeScreen(navigationActions, recipe(name= "Paella",
        instructions = "Prepare the Saffron Broth:\n" +
                "\n" +
                "In a small bowl, soak the saffron threads in 2 tablespoons of warm water and set aside.\n" +
                "Heat the broth in a separate pot and keep it warm for later.\n" +
                "Cook the Chicken and Chorizo:\n" +
                "\n" +
                "In a large paella pan (or a large skillet), heat some olive oil over medium heat.\n" +
                "Add the chicken thighs and cook until browned on all sides. Remove and set aside.\n" +
                "Add the chorizo slices to the pan and cook until browned, then remove and set aside with the chicken.\n" +
                "Sauté the Vegetables:\n" +
                "\n" +
                "In the same pan, add a little more olive oil if needed. Sauté the chopped onion, garlic, and red bell pepper until softened (about 5 minutes).\n" +
                "Add the diced tomatoes and smoked paprika, cooking for another 2-3 minutes.\n" +
                "Cook the Rice:\n" +
                "\n" +
                "Stir the rice into the pan with the vegetables, coating it well with the oil and letting it toast for a minute.\n" +
                "Pour in the saffron water and the warm broth. Stir gently to combine.\n" +
                "Add the cooked chicken and chorizo back into the pan, distributing evenly.\n" +
                "Add the Seafood:\n" +
                "\n" +
                "Arrange the shrimp, mussels, and clams on top of the rice mixture. Push them slightly into the rice without stirring.\n" +
                "Let the paella cook over medium-low heat for 15-20 minutes, uncovered, until the rice has absorbed most of the liquid and the seafood is cooked through (mussels and clams should open up). Avoid stirring the rice during this time.\n" +
                "Final Touches:\n" +
                "\n" +
                "If you're adding peas, sprinkle them over the top during the last 5 minutes of cooking.\n" +
                "Once the rice is done, turn up the heat for a minute or two to develop a crispy layer of rice at the bottom (called socarrat), but be careful not to burn it.\n" +
                "Serve:\n" +
                "\n" +
                "Let the paella rest for 5 minutes, then garnish with lemon wedges.\n" +
                "Serve directly from the pan for an authentic experience.",
        servings = 4,
        time = Timestamp(5400, 0)
    ))
}