package com.android.shelfLife.ui.recipes.execution

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * Composable function to display the screen where users can select food items for a specific ingredient.
         *
         * This screen displays a list of available food items for a selected ingredient, allowing users to
         * select the amount of each item they want to use. Users can navigate through the list of ingredients
         * and select appropriate food items. The screen also provides a "Done" button to finalize the selections
         * and move on to the next step.
         *
         * @param navigationActions The actions to be used for navigating between screens.
         * @param viewModel The [ExecuteRecipeViewModel] responsible for managing the data and actions related to recipe execution.
         * @param onNext Lambda function to call when the user finishes selecting food items and is ready to move to the next step.
         * @param onPrevious Lambda function to call when the user goes back to the previous step.
         */
fun SelectFoodItemsForIngredientScreen(
    navigationActions: NavigationActions,
    viewModel: ExecuteRecipeViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val ingredientName by viewModel.currentIngredientName.collectAsState() // Current ingredient name
    val availableFoodItems by viewModel.foodItems.collectAsState() // List of available food items
    val selectedMap by viewModel.selectedFoodItemsForIngredients.collectAsState() // Map of selected food items for each ingredient
    val currentlySelectedItems = selectedMap[ingredientName] ?: emptyList() // Items currently selected for this ingredient

    Log.d("SelectFoodItemsScreen", "Current ingredient: $ingredientName")
    Log.d("SelectFoodItemsScreen", "Available food items: ${availableFoodItems.joinToString { it.foodFacts.name }}")
    Log.d("SelectFoodItemsScreen", "Currently selected items: $currentlySelectedItems")

    // If ingredient name is null, navigate back
    if (ingredientName == null) {
        Log.e("SelectFoodItemsScreen", "Ingredient name is null. Navigating back.")
        navigationActions.goBack()
        return
    }

    // Scaffold to display the screen content with top app bar, bottom bar, and floating action button
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Items for $ingredientName") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("SelectFoodItemsScreen", "Back button clicked. calling onPrevious")
                        onPrevious() // Handle the back action
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close") // Close icon for navigation
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        },
        bottomBar = {
            // Bottom navigation menu
            BottomNavigationMenu(
                onTabSelect = { destination ->
                    navigationActions.navigateTo(destination)
                    Log.d("InstructionScreen", "BottomNavigationMenu: Navigated to $destination")
                },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.RECIPES
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("SelectFoodItemsScreen", "Floating action button clicked.")

                    // Retrieve the currently selected items for the ingredient
                    val selectedItems = currentlySelectedItems
                    val selectedAmounts = selectedItems.map { it.foodFacts.quantity.amount.toFloat() }

                    // Temporarily consume the selected items
                    viewModel.temporarilyConsumeItems(selectedItems, selectedAmounts)

                    // If there are more ingredients, navigate to the next ingredient
                    if (viewModel.hasMoreIngredients()) {
                        Log.d("SelectFoodItemsScreen", "Navigating to the next ingredient.")
                        viewModel.nextIngredient()
                    } else {
                        // If no more ingredients, finalize the selection and proceed to instructions
                        Log.d("SelectFoodItemsScreen", "No more ingredients. Navigating to instructions.")
                        viewModel.consumeSelectedItems()
                        onNext() // Call onNext when finished selecting ingredients
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("Done") // Button label
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // LazyColumn to display the list of available food items
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                items(availableFoodItems) { foodItem ->
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    val currentAmount = currentlySelectedItems
                        .filter { it.uid == foodItem.uid }
                        .sumOf { it.foodFacts.quantity.amount }

                    val maxAmount = foodItem.foodFacts.quantity.amount.toFloat()

                    // Food item selection card
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
                            viewModel.selectFoodItemForIngredient(ingredientName!!, foodItem, newAmount)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Composable function to display a selection card for each food item.
 *
 * This card displays the name of the food item, the selected amount, and a slider to adjust the amount.
 * The slider allows the user to select the amount of the food item to use for the ingredient.
 *
 * @param foodItem The food item to display.
 * @param amount The currently selected amount for this food item.
 * @param maxAmount The maximum available amount of this food item.
 * @param expanded Boolean to control if the slider should be displayed.
 * @param onCardClick Lambda function to toggle the expanded state of the card.
 * @param onAmountChange Lambda function to handle changes in the selected amount.
 */
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
            .clickable { onCardClick() } // Toggle expanded state when the card is clicked
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Display the name of the food item
            Text(
                text = foodItem.foodFacts.name,
                style = MaterialTheme.typography.titleMedium
            )

            // Display the amount selected and the total available quantity
            Text(
                text = "Selected: ${amount} ${foodItem.foodFacts.quantity.unit.name.lowercase()} of ${foodItem.foodFacts.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            // If the card is expanded, show the slider for adjusting the amount
            if (expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Adjust amount:")
                    androidx.compose.material3.Slider(
                        value = amount,
                        onValueChange = { newVal ->
                            Log.d(
                                "FoodItemSelectionCard",
                                "Slider value changed to $newVal for ${foodItem.foodFacts.name}"
                            )
                            onAmountChange(newVal) // Trigger state updates
                        },
                        valueRange = 0f..maxAmount,
                        steps = 0 // No intermediate steps for the slider
                    )
                }
            }
        }
    }
}