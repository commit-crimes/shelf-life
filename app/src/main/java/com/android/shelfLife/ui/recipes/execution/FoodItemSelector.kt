package com.android.shelfLife.ui.recipes.execution

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel
import io.ktor.websocket.Frame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFoodItemsForIngredientScreen(
    navigationActions: NavigationActions,
    viewModel: ExecuteRecipeViewModel = hiltViewModel()
) {
    val ingredientName = viewModel.currentIngredientName()
    val availableFoodItems by viewModel.foodItems.collectAsState()
    val selectedMap by viewModel.selectedFoodItemsForIngredients.collectAsState()
    val currentlySelectedItems = selectedMap[ingredientName] ?: emptyList()

    if (ingredientName == null) {
        // No ingredient found, just navigate back or show an error
        navigationActions.goBack()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Items for $ingredientName") },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Once user is happy with their selection for this ingredient, go to next ingredient if any
                    if (viewModel.hasMoreIngredients()) {
                        viewModel.nextIngredient()
                        // Navigate to the same screen to show the next ingredient
                        navigationActions.navigateTo(Screen.FOOD_ITEM_SELECTION)
                    } else {
                        // No more ingredients, navigate to a done screen or consume items
                        // For example:
                        // viewModel.consumeSelectedItems()
                        navigationActions.goBack() // or navigate to a "Done" screen
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("Done")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                items(availableFoodItems) { foodItem ->
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    val currentAmount = currentlySelectedItems
                        .filter { it.uid == foodItem.uid }
                        .sumOf { it.foodFacts.quantity.amount }

                    val maxAmount = 300f

                    FoodItemSelectionCard(
                        foodItem = foodItem,
                        amount = currentAmount.toFloat(),
                        maxAmount = maxAmount,
                        expanded = expanded,
                        onCardClick = { expanded = !expanded },
                        onAmountChange = { newAmount ->
                            // Here, you'd actually need to update your data structure that holds
                            // the exact amount per food item. For now, the code is incomplete.
                            // If you need a more detailed data structure (e.g. per item amount),
                            // you can implement that in the ViewModel.
                            // For now, just simulate selecting the item by adding it to the list.
                            val updatedList = currentlySelectedItems.toMutableList()
                            // Replace or add the new item with updated amount.
                            // This requires that the FoodItem or your model reflect the chosen amount.
                            // Consider storing a separate structure or a custom data class with FoodItem + chosenAmount.

                            // Since the current viewModel stores only List<FoodItem>,
                            // you'd have to create a new FoodItem with adjusted quantity or a different structure.
                            // We'll just call selectFoodItemsForIngredient with the existing items for demonstration.
                            viewModel.selectFoodItemsForIngredient(ingredientName, updatedList)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FoodItemSelectionCard(
    foodItem: FoodItem,
    amount: Float,
    maxAmount: Float,
    expanded: Boolean,
    onCardClick: () -> Unit,
    onAmountChange: (Float) -> Unit
) {
    // A simple card that expands to show a slider
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row with basic info
            Text(
                text = foodItem.foodFacts.name,
                style = MaterialTheme.typography.titleMedium
            )

            // If expanded, show slider for amount selection
            if (expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Amount selected: ${amount}g")
                    androidx.compose.material3.Slider(
                        value = amount,
                        onValueChange = { newVal -> onAmountChange(newVal) },
                        valueRange = 0f..maxAmount,
                        steps = 0
                    )
                }
            }
        }
    }
}