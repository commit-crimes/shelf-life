package com.android.shelfLife.ui.recipes.execution

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.TopLevelDestinations
import com.android.shelfLife.viewmodel.recipes.RecipeExecutionState
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

/**
 * Composable function to manage the execution flow of a recipe.
 *
 * This screen is responsible for managing different states during the execution of a recipe.
 * The screen transitions between various steps based on the current state, including selecting servings,
 * selecting ingredients, and following the recipe instructions. It handles the navigation between the steps
 * and provides the user interface for each step.
 *
 * @param navigationActions The actions to navigate between screens in the app.
 * @param viewModel The [ExecuteRecipeViewModel] responsible for managing the state and logic of recipe execution.
 */
@Composable
fun RecipeExecutionScreen(navigationActions: NavigationActions, viewModel: ExecuteRecipeViewModel = hiltViewModel()) {
    val currentState by viewModel.state.collectAsState() // Observing the current state of the recipe execution

    // Based on the current state, navigate to the appropriate screen
    when (currentState) {
        // State for selecting the number of servings for the recipe
        is RecipeExecutionState.SelectServings -> ServingsScreen(
            navigationActions, // Pass navigation actions to the ServingsScreen
            viewModel, // Pass the view model to manage state
            onNext = { viewModel.nextState() } // On next, move to the next state
        )
        // State for selecting the food items for the recipe
        is RecipeExecutionState.SelectFood -> SelectFoodItemsForIngredientScreen(
            navigationActions,
            viewModel,
            onNext = { viewModel.nextState() }, // Move to the next state
            onPrevious = { viewModel.previousState() } // Go back to the previous state
        )
        // State for displaying the recipe instructions
        is RecipeExecutionState.Instructions -> InstructionScreen(
            navigationActions,
            viewModel,
            onFinish = {
                // Once the instructions are finished, navigate back to the overview screen
                navigationActions.goBack()
                navigationActions.goBack()
                navigationActions.navigateTo(TopLevelDestinations.OVERVIEW) // Navigate to the overview screen
            }
        )
    }
}