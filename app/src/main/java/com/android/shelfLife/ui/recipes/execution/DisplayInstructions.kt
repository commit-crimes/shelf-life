package com.android.shelfLife.ui.recipes.execution

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

@Composable
fun InstructionScreen(
    navigationActions: NavigationActions,
    viewModel: ExecuteRecipeViewModel = hiltViewModel())
{
    Text("Instruction screen")
}