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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

/**
 * Composable function to display the screen for selecting food items for an ingredient.
 *
 * @param navigationActions The actions to handle navigation.
 * @param viewModel The ViewModel for managing the state of the recipe execution.
 * @param onNext Callback function to handle the next action.
 * @param onPrevious Callback function to handle the previous action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFoodItemsForIngredientScreen(
    navigationActions: NavigationActions,
    viewModel: ExecuteRecipeViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
  val ingredientName by viewModel.currentIngredientName.collectAsState()
  val availableFoodItems by viewModel.foodItems.collectAsState()
  val selectedMap by viewModel.selectedFoodItemsForIngredients.collectAsState()
  val currentlySelectedItems = selectedMap[ingredientName] ?: emptyList()

  Log.d("SelectFoodItemsScreen", "Current ingredient: $ingredientName")
  Log.d(
      "SelectFoodItemsScreen",
      "Available food items: ${availableFoodItems.joinToString { it.foodFacts.name }}")
  Log.d("SelectFoodItemsScreen", "Currently selected items: $currentlySelectedItems")

  if (ingredientName == null) {
    Log.e("SelectFoodItemsScreen", "Ingredient name is null. Navigating back.")
    navigationActions.goBack()
    return
  }

  Scaffold(
      modifier = Modifier.testTag("selectFoodItemsScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Select Items for $ingredientName") },
            navigationIcon = {
              IconButton(
                  onClick = {
                    Log.d("SelectFoodItemsScreen", "Back button clicked. calling onPrevious")
                    onPrevious()
                  }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer))
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { destination ->
              navigationActions.navigateTo(destination)
              Log.d("InstructionScreen", "BottomNavigationMenu: Navigated to $destination")
            },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.RECIPES)
      },
      floatingActionButton = {
        FloatingActionButton(
            modifier = Modifier.testTag("doneButton"),
            onClick = {
              Log.d("SelectFoodItemsScreen", "Floating action button clicked.")

              // Retrieve the currently selected items for the ingredient
              val selectedItems = currentlySelectedItems
              val selectedAmounts = selectedItems.map { it.foodFacts.quantity.amount.toFloat() }

              // Temporarily consume the selected items
              viewModel.temporarilyConsumeItems(selectedItems, selectedAmounts)

              if (viewModel.hasMoreIngredients()) {
                Log.d("SelectFoodItemsScreen", "Navigating to the next ingredient.")
                viewModel.nextIngredient()
              } else {
                Log.d("SelectFoodItemsScreen", "No more ingredients. Navigating to instructions.")
                viewModel.consumeSelectedItems()
                onNext()
              }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary) {
              Text("Done")
            }
      }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
          LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.padding(16.dp)) {
                items(availableFoodItems) { foodItem ->
                  var expanded by rememberSaveable { mutableStateOf(false) }
                  val currentAmount =
                      currentlySelectedItems
                          .filter { it.uid == foodItem.uid }
                          .sumOf { it.foodFacts.quantity.amount }

                  val maxAmount = foodItem.foodFacts.quantity.amount.toFloat()

                  FoodItemSelectionCard(
                      foodItem = foodItem,
                      amount = currentAmount.toFloat(),
                      maxAmount = maxAmount,
                      expanded = expanded,
                      onCardClick = {
                        expanded = !expanded
                        Log.d(
                            "FoodItemSelectionCard",
                            "Card clicked for food item: ${foodItem.foodFacts.name}. Expanded: $expanded")
                      },
                      onAmountChange = { newAmount ->
                        Log.d(
                            "FoodItemSelectionCard",
                            "Amount changed for ${foodItem.foodFacts.name} to $newAmount")
                        viewModel.selectFoodItemForIngredient(ingredientName!!, foodItem, newAmount)
                      })
                }
              }
        }
      }
}

/**
 * Composable function to display a card for selecting a food item.
 *
 * @param foodItem The food item to be displayed.
 * @param amount The amount of the food item selected.
 * @param maxAmount The maximum amount of the food item available.
 * @param expanded Whether the card is expanded to show more details.
 * @param onCardClick Callback function to handle card click.
 * @param onAmountChange Callback function to handle amount change.
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
      modifier =
          Modifier.fillMaxWidth().padding(horizontal = 8.dp).testTag("foodItemCard").clickable {
            onCardClick()
          }) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Display the name of the food item
          Text(text = foodItem.foodFacts.name, style = MaterialTheme.typography.titleMedium)

          // Display the amount selected and the total available quantity
          Text(
              text =
                  "Selected: ${amount} ${foodItem.foodFacts.quantity.unit.name.lowercase()} of ${foodItem.foodFacts.quantity}",
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(top = 8.dp))

          // If the card is expanded, show the slider for adjusting the amount
          if (expanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp)) {
                  Text(text = "Adjust amount:")
                  androidx.compose.material3.Slider(
                      modifier = Modifier.testTag("amountSlider"),
                      value = amount,
                      onValueChange = { newVal ->
                        Log.d(
                            "FoodItemSelectionCard",
                            "Slider value changed to $newVal for ${foodItem.foodFacts.name}")
                        onAmountChange(newVal) // Trigger state updates
                      },
                      valueRange = 0f..maxAmount,
                      steps = 0)
                }
          }
        }
      }
}
