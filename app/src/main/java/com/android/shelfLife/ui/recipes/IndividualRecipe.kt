package com.android.shelfLife.ui.recipes.IndividualRecipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
import com.android.shelfLife.ui.navigation.*
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.recipes.IndividualRecipeViewModel
import kotlin.math.floor
import kotlinx.coroutines.launch

/**
 * Composable function to display the screen for an individual recipe.
 *
 * The screen provides detailed information about a selected recipe, including its image, servings,
 * cooking time, ingredients, and instructions. If no recipe is selected, the user is redirected
 * to an easter egg screen.
 *
 * @param navigationActions Actions for handling navigation events, such as going back.
 * @param individualRecipeViewModel The ViewModel associated with the individual recipe screen.
 */
@Composable
fun IndividualRecipeScreen(
    navigationActions: NavigationActions,
    individualRecipeViewModel: IndividualRecipeViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    if (individualRecipeViewModel.selectedRecipeIsNonEmpty) {
        // Scaffold provides structure for the screen, including top and bottom bars
        Scaffold(
            modifier = Modifier.testTag("individualRecipesScreen"),
            topBar = {
                // Top bar with recipe name and delete action
                CustomTopAppBar(
                    onClick = { navigationActions.goBack() },
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
                            modifier = Modifier.testTag("deleteFoodItem")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                        }
                    }
                )
            },
            bottomBar = {
                // Bottom navigation menu
                BottomNavigationMenu(
                    onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                    tabList = LIST_TOP_LEVEL_DESTINATION,
                    selectedItem = Route.RECIPES
                )
            },
            floatingActionButton = {
                // Floating action button to start recipe execution
                FloatingActionButton(
                    onClick = { navigationActions.navigateTo(Route.RECIPE_EXECUTION) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("startButton")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Recipe"
                    )
                }
            },
            content = { paddingValues ->
                // Main content displaying recipe details
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .testTag("recipe")
                ) {
                    RecipeContent(individualRecipeViewModel)
                }
            }
        )
    } else {
        // If no recipe is selected, navigate to the easter egg screen
        navigationActions.navigateTo(Screen.EASTER_EGG)
    }
}

/**
 * Composable function to display the content of the selected recipe.
 *
 * The function renders the recipe image, servings, cooking time, ingredients, and instructions
 * in a vertically scrollable layout.
 *
 * @param viewModel The ViewModel associated with the recipe.
 */
@Composable
fun RecipeContent(viewModel: IndividualRecipeViewModel) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enable vertical scrolling
            .testTag("recipeContent")
    ) {
        // Recipe image at the top
        Image(
            painter = painterResource(R.drawable.individual_recipe_pot),
            contentDescription = "Recipe Image",
            modifier = Modifier
                .width(537.dp)
                .height(164.dp)
                .testTag("recipeImage"),
            contentScale = ContentScale.FillWidth
        )

        // Row displaying servings and cooking time
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Servings: ${viewModel.getRecipeServing()}",
                modifier = Modifier.testTag("recipeServings")
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Time: ${viewModel.getRecipeTime()} min",
                modifier = Modifier.testTag("recipeTime")
            )
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
 * Composable function to display a single ingredient in the recipe.
 *
 * The ingredient is displayed in the format: " - [quantity][unit] of [ingredient name]".
 *
 * @param ingredient The ingredient to display, including its name, quantity, and unit.
 */
@Composable
fun DisplayIngredientNew(ingredient: Ingredient) {
    // Map FoodUnit to a displayable string
    val unit = when (ingredient.quantity.unit) {
        FoodUnit.GRAM -> "gr"
        FoodUnit.ML -> "ml"
        FoodUnit.COUNT -> ""
    }

    // Format quantity to remove decimals if unnecessary
    val amount = ingredient.quantity.amount
    val quantity = if (floor(amount) == amount) amount.toInt().toString() else amount.toString()

    Text(
        text = " - ${quantity}${unit} of ${ingredient.name}",
        modifier = Modifier.testTag("recipeIngredient")
    )
}

/**
 * Composable function to display a single instruction in the recipe.
 *
 * Each instruction is displayed as a separate text block, with vertical spacing.
 *
 * @param instruction The text of the instruction to display.
 */
@Composable
fun DisplayInstructionNew(instruction: String) {
    Text(
        text = instruction,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .testTag("recipeInstruction")
    )
}