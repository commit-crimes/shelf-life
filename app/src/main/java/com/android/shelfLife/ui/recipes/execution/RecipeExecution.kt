package com.android.shelfLife.ui.recipes.execution

import InstructionScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.TopLevelDestinations
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel
import com.android.shelfLife.viewmodel.recipes.RecipeExecutionState

/**
 * Composable function to display the recipe execution screen.
 *
 * @param navigationActions The actions to handle navigation.
 * @param viewModel The ViewModel for managing the state of the recipe execution.
 */
@Composable
fun RecipeExecutionScreen(
    navigationActions: NavigationActions,
    viewModel: ExecuteRecipeViewModel = hiltViewModel()
) {
  val currentState by viewModel.state.collectAsState()

  when (currentState) {
    // Display the screen to select servings
    is RecipeExecutionState.SelectServings ->
        ServingsScreen(navigationActions, viewModel, onNext = { viewModel.nextState() })
    // Display the screen to select food items for an ingredient
    is RecipeExecutionState.SelectFood ->
        SelectFoodItemsForIngredientScreen(
            navigationActions,
            viewModel,
            onNext = { viewModel.nextState() },
            onPrevious = { viewModel.previousState() })
    // Display the instruction screen for executing the recipe
    is RecipeExecutionState.Instructions ->
        InstructionScreen(
            navigationActions,
            viewModel,
            onFinish = {
              navigationActions.goBack()
              navigationActions.goBack()
              navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
            })
  }
}
