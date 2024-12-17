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

@Composable
fun RecipeExecutionScreen(
    navigationActions: NavigationActions,
    viewModel: ExecuteRecipeViewModel = hiltViewModel()
) {
  val currentState by viewModel.state.collectAsState()

  when (currentState) {
    is RecipeExecutionState.SelectServings ->
        ServingsScreen(navigationActions, viewModel, onNext = { viewModel.nextState() })
    is RecipeExecutionState.SelectFood ->
        SelectFoodItemsForIngredientScreen(
            navigationActions,
            viewModel,
            onNext = { viewModel.nextState() },
            onPrevious = { viewModel.previousState() })
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
