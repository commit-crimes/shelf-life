package com.android.shelfLife.ui.recipes.execution

import InstructionScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.viewmodel.recipes.RecipeExecutionState
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

@Composable
fun RecipeExecutionScreen(navigationActions: NavigationActions, viewModel: ExecuteRecipeViewModel = hiltViewModel()) {
    val currentState by viewModel.state.collectAsState()

    when (currentState) {
        is RecipeExecutionState.SelectServings -> ServingsScreen(
            navigationActions,
            viewModel,
            onNext = { viewModel.nextState() }
        )
        is RecipeExecutionState.SelectFood -> SelectFoodItemsForIngredientScreen(
            navigationActions,
            viewModel,
            onNext = { viewModel.nextState() },
            onPrevious = { viewModel.previousState() }
        )
        is RecipeExecutionState.Instructions -> InstructionScreen(
            navigationActions,
            viewModel,
            onFinish = { navigationActions.navigateTo(Route.OVERVIEW) }
        )
    }
}

