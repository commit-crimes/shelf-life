package com.android.shelfLife.ui.recipes.execution

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.recipes.RecipeExecutionState
import com.android.shelfLife.viewmodel.recipes.newExecuteRecipeViewModel

@Composable
fun RecipeExecutionScreen(navigationActions: NavigationActions, viewModel: newExecuteRecipeViewModel = hiltViewModel()) {
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
        is RecipeExecutionState.Instructions -> InstructionsScreen(
            onFinish = { /* Navigate to finish or restart */ }
        )
    }
}



@Composable
fun InstructionsScreen(onFinish: () -> Unit) {
    // UI to display recipe instructions
    Column {
        Text(text = "Here are your instructions")
        Button(onClick = onFinish) {
            Text("Finish")
        }
    }
}