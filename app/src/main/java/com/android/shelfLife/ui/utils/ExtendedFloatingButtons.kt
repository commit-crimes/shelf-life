package com.android.shelfLife.ui.newutils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.dp
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.recipes.RecipesViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel


@Composable
fun ExtendedActionButtons(
    fabExpanded: MutableState<Boolean>,
    navigationActions: NavigationActions,
    firstScreen: String = Screen.GENERATE_RECIPE,
    secondScreen: String = Screen.ADD_RECIPE,
    firstScreenTestTag: String = "generateRecipeFab",
    secondScreenTestTag: String = "addRecipeFab",
    foodItemViewModel: FoodItemViewModel = hiltViewModel()
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),

    ) {
        // Secondary FAB for "Generate" option
        if (fabExpanded.value) {
            ExtendedFloatingActionButton(
                text = { Text("Generate") },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Generate") },
                onClick = {
                    // Navigate to Generate Recipe screen
                    foodItemViewModel.setIsGenerated(true)
                    foodItemViewModel.resetSelectFoodItem()
                    navigationActions.navigateTo(firstScreen)
                    fabExpanded.value = false
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.testTag(firstScreenTestTag).width(150.dp)
            )
        }

        // Primary FAB
        ExtendedFloatingActionButton(
            text = { Text(if (fabExpanded.value) "Manual" else "") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
            onClick = {
                if (fabExpanded.value) {
                    // Navigate to Add Recipe screen
                    foodItemViewModel.setIsGenerated(false)
                    navigationActions.navigateTo(secondScreen)
                    fabExpanded.value = false
                } else {
                    // Expand the FABs
                    fabExpanded.value = true
                }
            },
            expanded = fabExpanded.value, // Bind to the state
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.testTag(secondScreenTestTag).width(if (fabExpanded.value) 150.dp else 56.dp)
        )
    }
}