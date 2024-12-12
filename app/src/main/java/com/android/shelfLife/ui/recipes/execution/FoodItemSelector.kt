package com.android.shelfLife.ui.recipes.execution

import android.util.Log
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

    Log.d("SelectFoodItemsScreen", "Current ingredient: $ingredientName")
    Log.d("SelectFoodItemsScreen", "Available food items: ${availableFoodItems.forEach { it.foodFacts.name }}")
    Log.d("SelectFoodItemsScreen", "Currently selected items: $currentlySelectedItems")

    if (ingredientName == null) {
        Log.e("SelectFoodItemsScreen", "Ingredient name is null. Navigating back.")
        navigationActions.goBack()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Items for $ingredientName") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("SelectFoodItemsScreen", "Back button clicked.")
                        navigationActions.goBack()
                    }) {
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
                    Log.d("SelectFoodItemsScreen", "Floating action button clicked.")
                    if (viewModel.hasMoreIngredients()) {
                        Log.d("SelectFoodItemsScreen", "Navigating to the next ingredient.")
                        viewModel.nextIngredient()
                        navigationActions.navigateTo(Screen.FOOD_ITEM_SELECTION)
                    } else {
                        Log.d("SelectFoodItemsScreen", "No more ingredients. Navigating back.")
                        navigationActions.goBack()
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
                        onCardClick = {
                            expanded = !expanded
                            Log.d("FoodItemSelectionCard", "Card clicked for food item: ${foodItem.foodFacts.name}. Expanded: $expanded")
                        },
                        onAmountChange = { newAmount ->
                            Log.d("FoodItemSelectionCard", "Amount changed for ${foodItem.foodFacts.name} to $newAmount")
                            val updatedList = currentlySelectedItems.toMutableList()
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
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = foodItem.foodFacts.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Amount selected: ${amount}g")
                    androidx.compose.material3.Slider(
                        value = amount,
                        onValueChange = { newVal ->
                            Log.d("FoodItemSelectionCard", "Slider value changed to $newVal for ${foodItem.foodFacts.name}")
                            onAmountChange(newVal) // This will now trigger state updates.
                        },
                        valueRange = 0f..maxAmount,
                        steps = 0
                    )
                }
            }
        }
    }
}