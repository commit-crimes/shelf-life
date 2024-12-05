package com.android.shelfLife.ui.recipes

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.RecipePrompt
import com.android.shelfLife.model.recipe.RecipeType
import com.android.shelfLife.ui.navigation.NavigationActions

@Composable
fun GenerateRecipeScreen(
  navigationActions: NavigationActions,
  listRecipesViewModel: ListRecipesViewModel,
) {
  val context = LocalContext.current

  // States for recipe name and food items
  var recipeName by remember { mutableStateOf("") }
  var recipeNameError by remember { mutableStateOf<String?>(null) }

  val foodItems = remember { mutableStateListOf<String>() }
  var newFoodItem by remember { mutableStateOf("") }
  var foodItemError by remember { mutableStateOf<String?>(null) }

  Column(modifier = Modifier.padding(top = 50.dp, start = 16.dp, end = 16.dp)) {
    // Header
    Text(
      text = "Generate Recipe",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 16.dp))

    // Recipe name input
    OutlinedTextField(
      value = recipeName,
      onValueChange = {
        recipeName = it
        recipeNameError = if (recipeName.isEmpty()) "Recipe name cannot be empty" else null
      },
      label = { Text("Recipe Name") },
      modifier = Modifier.fillMaxWidth())

    // Error message for recipe name
    recipeNameError?.let {
      Text(
        text = it,
        color = androidx.compose.material.MaterialTheme.colors.error,
        style = androidx.compose.material.MaterialTheme.typography.body2)
    }

    // Section for adding food items
    Text(
      text = "Food Items",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(vertical = 16.dp))

    // Input field for a new food item
    Row(verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = newFoodItem,
        onValueChange = {
          newFoodItem = it
          foodItemError = if (newFoodItem.isEmpty()) "Food item name cannot be empty" else null
        },
        label = { Text("Add Food Item") },
        modifier = Modifier.weight(1f))

      Button(
        onClick = {
          if (newFoodItem.isNotBlank()) {
            foodItems.add(newFoodItem.trim())
            newFoodItem = "" // Reset the input field
          } else {
            foodItemError = "Please enter a food item name"
          }
        },
        modifier = Modifier.padding(start = 8.dp)) {
        Text("Add")
      }
    }

    // Error message for food item
    foodItemError?.let {
      Text(
        text = it,
        color = androidx.compose.material.MaterialTheme.colors.error,
        style = androidx.compose.material.MaterialTheme.typography.body2)
    }

    // Display the list of food items
    foodItems.forEachIndexed { index, foodItem ->
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = "${index + 1}. $foodItem", modifier = Modifier.weight(1f), fontSize = 16.sp)

        Button(onClick = { foodItems.removeAt(index) }) { Text("Remove") }
      }
    }

    // Generate button
    Button(
      onClick = {
        if (recipeName.isNotBlank() && foodItems.isNotEmpty()) {
          val testIngredients =
            foodItems.mapIndexed { index, name ->
              FoodItem(
                uid = index.toString(), foodFacts = FoodFacts(name, quantity = Quantity(1.0)))
            }
          navigationActions.goBack()
          listRecipesViewModel.generateRecipe(
            recipePrompt =
            RecipePrompt(
              name = recipeName,
              ingredients = testIngredients,
              recipeType = RecipeType.HIGH_PROTEIN),
            onSuccess = { recipe ->
              Log.d("GenerateRecipe", "Recipe generated successfully: $recipe")
              listRecipesViewModel.saveRecipe(recipe)
            },
            onFailure = { error ->
              Log.e("GenerateRecipe", "Error generating recipe: $error")
              Toast.makeText(context, "Failed to generate recipe.", Toast.LENGTH_SHORT).show()
            })
        } else {
          recipeNameError = if (recipeName.isBlank()) "Please enter a recipe name" else null
          if (foodItems.isEmpty()) {
            Toast.makeText(context, "Please add at least one food item.", Toast.LENGTH_SHORT)
              .show()
          }
        }
      },
      modifier = Modifier.padding(top = 16.dp)) {
      Text("Generate Recipe")
    }
  }
}