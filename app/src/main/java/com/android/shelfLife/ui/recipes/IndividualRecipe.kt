package com.android.shelfLife.ui.recipes.IndividualRecipe

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import kotlin.math.floor
import kotlinx.coroutines.launch

/**
 * Composable function to display the screen for an individual recipe.
 *
 * The screen provides detailed information about a selected recipe, including its image, servings,
 * cooking time, ingredients, and instructions. If no recipe is selected, an error message is
 * displayed.
 *
 * @param navigationActions The navigation actions for handling navigation events, such as going
 *   back.
 * @param individualRecipeViewModel The ViewModel for managing the state of the Individual Recipe screen.
 */
@Composable
fun IndividualRecipeScreen(
    navigationActions: NavigationActions,
    individualRecipeViewModel: IndividualRecipeViewModel =
        hiltViewModel<IndividualRecipeViewModel>()
) {

    val coroutineScope = rememberCoroutineScope()

    if (individualRecipeViewModel.selectedRecipeIsNonEmpty) {
        // Scaffold that provides the structure for the screen, including top and bottom bars.
        Scaffold(
            modifier = Modifier.testTag("individualRecipesScreen"),
            topBar = {
                CustomTopAppBar(
                    onClick = {
                        navigationActions.goBack()
                        individualRecipeViewModel.deselectRecipe()
                    },
                    title = individualRecipeViewModel.getRecipeName(),
                    titleTestTag = "individualRecipeTitle",
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    individualRecipeViewModel.deleteSelectedRecipe()
                                    navigationActions.goBack()
                                }
                            },
                            modifier = Modifier.testTag("deleteFoodItem")) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                        }
                    })
            },
            bottomBar = {
                BottomNavigationMenu(
                    onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                    tabList = LIST_TOP_LEVEL_DESTINATION,
                    selectedItem = Route.RECIPES)
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigationActions.navigateTo(Route.RECIPE_EXECUTION) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("startButton")) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow, // Replace with a suitable icon
                        contentDescription = "Start Recipe")
                }
            },
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize().testTag("recipe")) {
                    // Use the extracted RecipeContent composable
                    RecipeContent(individualRecipeViewModel)
                }
            })
    } else {
        // If no recipe is selected, navigate to the easter egg screen
        navigationActions.navigateTo(Screen.EASTER_EGG)
    }
}

/**
 * Composable function to display the content of a recipe.
 *
 * This function displays the recipe image, servings, cooking time, ingredients, and instructions
 * in a scrollable layout.
 *
 * @param viewModel The ViewModel for managing the state of the Individual Recipe screen.
 */
@Composable
fun RecipeContent(viewModel: IndividualRecipeViewModel) {
    Column(
        modifier =
        Modifier.padding(8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enable vertical scrolling
            .testTag("recipeContent")) {
        // Display the recipe image
        Image(
            painter = painterResource(R.drawable.individual_recipe_pot),
            contentDescription = "Recipe Image",
            modifier = Modifier.width(537.dp).height(164.dp).testTag("recipeImage"),
            contentScale = ContentScale.FillWidth)

        // Row displaying servings and time information
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Servings: ${viewModel.getRecipeServing()}",
                modifier = Modifier.testTag("recipeServings"))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Time: ${viewModel.getRecipeTime()} min",
                modifier = Modifier.testTag("recipeTime"))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ingredients section
        Column(modifier = Modifier.testTag("recipeIngredients")) {
            viewModel.getRecipeIngredients().forEach { ingredient ->
                DisplayIngredientNew(ingredient)
            }
        }

        // Instructions section
        Column(modifier = Modifier.testTag("recipeInstructions")) {
            viewModel.getRecipeInstruction().forEach { instruction ->
                DisplayInstructionNew(instruction)
            }
        }
    }
}

/**
 * Composable function to display a single ingredient in a recipe.
 *
 * This function displays the ingredient's quantity, unit, and name.
 *
 * @param ingredient The ingredient to display.
 */
@Composable
fun DisplayIngredientNew(ingredient: Ingredient) {
    val unit =
        when (ingredient.quantity.unit) {
            FoodUnit.GRAM -> "gr"
            FoodUnit.ML -> "ml"
            FoodUnit.COUNT -> ""
        }

    val amount = ingredient.quantity.amount
    val quantity = if (floor(amount) == amount) amount.toInt().toString() else amount.toString()

    Text(
        text = " - ${quantity}${unit} of ${ingredient.name}",
        modifier = Modifier.testTag("recipeIngredient"))
}

/**
 * Composable function to display a single instruction in a recipe.
 *
 * This function displays the instruction text.
 *
 * @param instruction The instruction text to display.
 */
@Composable
fun DisplayInstructionNew(instruction: String) {
    // Display recipe instructions, scrollable if long
    Text(
        text = instruction, modifier = Modifier.padding(vertical = 8.dp).testTag("recipeInstruction"))
}