package com.android.shelfLife.ui.recipes

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.ui.navigation.NavigationActions
import com.example.compose.errorContainerDark
import com.example.compose.onSecondaryDark
import com.example.compose.primaryContainerDark
import com.example.compose.primaryContainerLight
import com.example.compose.secondaryContainerDark
import com.example.compose.secondaryContainerLight
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
    householdViewModel: HouseholdViewModel
) {
  val context = LocalContext.current

  var title by remember { mutableStateOf("") }
  var servings by remember { mutableStateOf("") }
  var time by remember { mutableStateOf("") }
  val ingredients = remember { mutableStateListOf<Ingredient>() }
  val instructions = remember { mutableStateListOf<String>() }

  // Dialog visibility state
  var showIngredientDialog by remember { mutableStateOf(false) }

  var error = false
  var titleError by remember { mutableStateOf<String?>(null) }
  var servingsError by remember { mutableStateOf<String?>(null) }
  var timeError by remember { mutableStateOf<String?>(null) }

  Scaffold(
      modifier = Modifier.testTag("addRecipeScreen"),
      topBar = {
        // add a topBar
        TopAppBar(
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            modifier = Modifier.testTag("topBar"),
            navigationIcon = {
              // Back button to return to the previous screen
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackArrow")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back Icon")
                  }
            },
            // Title of the screen: Recipe name
            title = {
              Text(
                  text = "Add your own recipe",
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontSize = 24.sp, fontWeight = FontWeight.Bold))
            })
      }) {
        // I use a lazy column so that I can add more ingredients and steps, and it also makes sure
        // its scrollable
        LazyColumn(modifier = Modifier.padding(horizontal = 20.dp).fillMaxSize()) {
          // Recipe title
          item {
            OutlinedTextField(
                value = title,
                onValueChange = {
                  title = it
                  titleError = if (title.isEmpty()) "Incomplete title" else null
                },
                label = { Text("Recipe title") },
                modifier =
                    Modifier.fillMaxWidth().padding(vertical = 10.dp).testTag("inputRecipeTitle"))
          }

          // error message if the title is empty
          item { ErrorTextBox(titleError) }

          // recipe servings
          item {
            OutlinedTextField(
                value = servings,
                onValueChange = {
                  servings = it
                  servingsError = if (servings.isEmpty()) "Incomplete serving number" else null
                },
                label = { Text("Servings") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .testTag("inputRecipeServings"))
          }

          // error message if the servings is empty
          item { ErrorTextBox(servingsError) }

          // recipe time
          item {
            OutlinedTextField(
                value = time,
                onValueChange = {
                  time = it
                  timeError = if (time.isEmpty()) "Incomplete time" else null
                },
                label = { Text("Time in minutes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier =
                    Modifier.fillMaxWidth().padding(vertical = 10.dp).testTag("inputRecipeTime"))
          }

          // error message if the time is empty
          item { ErrorTextBox(timeError) }

          // recipe Ingredients
          item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "Ingredients")
            }
          }

          // list of ingredients
          itemsIndexed(ingredients) { index, ingredient ->
            IngredientItem(
                index = index,
                ingredient = ingredient,
                onRemoveClick = {
                  if (ingredients.size > 0) {
                    ingredients.removeAt(index)
                  }
                })
          }

          // add button for more ingredients
          item {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { showIngredientDialog = true }, // State change to show dialog
                modifier = Modifier.height(40.dp),
                content = {
                  Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ingredient")
                })
          }

          // recipe instructions
          item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "Instructions")
            }
          }

          // list of instructions
          itemsIndexed(instructions) { index, instruction ->
            InstructionItem(
                index = index,
                instruction = instruction,
                onInstructionChange = { newInstruction -> instructions[index] = newInstruction },
                onRemoveClick = { instructions.removeAt(index) })
          }

          // add instructions button
          item {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { instructions.add("") },
                modifier = Modifier.height(40.dp),
                content = {
                  Icon(imageVector = Icons.Default.Add, contentDescription = "Add Instruction")
                })
          }

          // Footer buttons: Cancel and Add
          item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center) {
                  // Cancel button
                  Button(
                      onClick = { navigationActions.goBack() },
                      modifier = Modifier.height(40.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = errorContainerDark)) {
                        Text(text = "Cancel", fontSize = 18.sp)
                      }

                  Spacer(Modifier.width(24.dp))

                  // Add button
                  Button(
                      // check that everything has been entered
                      onClick = {
                        error =
                            title.isEmpty() ||
                                time.isEmpty() ||
                                servings.isEmpty() ||
                                ingredients.isEmpty() ||
                                instructions.isEmpty()
                        if (!error) {
                          listRecipesViewModel.addRecipeToList(
                              recipe =
                                  Recipe(
                                      name = title,
                                      instructions = instructions.toList(),
                                      servings = servings.toInt(),
                                      time = (time.toDouble() * 60.0).seconds,
                                      ingredients = ingredients.toList()))
                          navigationActions.goBack()
                        } else {
                          // if not a Toast appears
                          Toast.makeText(
                                  context,
                                  "Please correct the errors before submitting.",
                                  Toast.LENGTH_SHORT)
                              .show()
                        }
                      },
                      modifier = Modifier.height(40.dp),
                      colors =
                          ButtonDefaults.buttonColors(containerColor = primaryContainerLight)) {
                        Text(text = "Add", fontSize = 18.sp, color = onSecondaryDark)
                      }
                }
          }
        }

        // Ingredient Dialog, inside the composable block
        if (showIngredientDialog) {
          IngredientDialog(
              ingredients = ingredients,
              onDismiss = { showIngredientDialog = false }, // Hide dialog
              onAddIngredient = { showIngredientDialog = false } // Dismiss dialog after adding
              )
        }
      }
}

/**
 * A composable function that displays a single instruction step in a recipe.
 *
 * This component renders a row containing:
 * 1. An `OutlinedTextField` to input or edit the instruction step.
 * 2. A `Delete` icon button to remove the instruction step.
 *
 * The `index` is used to display the step number (e.g., "Step 1", "Step 2"). The `instruction`
 * parameter holds the current instruction text. The `onInstructionChange` callback is called when
 * the instruction text changes. The `onRemoveClick` callback is called when the delete button is
 * clicked, allowing removal of the instruction step.
 *
 * @param index The index of the current instruction in the list (used to display step number).
 * @param instruction The current instruction text that is displayed and can be edited.
 * @param onInstructionChange A lambda function that is triggered when the instruction text is
 *   updated.
 * @param onRemoveClick A lambda function that is triggered when the delete button is clicked to
 *   remove the instruction.
 */
@Composable
fun InstructionItem(
    index: Int,
    instruction: String,
    onInstructionChange: (String) -> Unit,
    onRemoveClick: () -> Unit
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    // OTF for the individual step
    OutlinedTextField(
        value = instruction,
        onValueChange = onInstructionChange,
        label = { Text("Step ${index + 1}") },
        modifier = Modifier.weight(1f))

    // delete that step button
    IconButton(
        onClick = onRemoveClick,
    ) {
      Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Step")
    }
  }
}

/**
 * A composable function that displays a single ingredient in a recipe.
 *
 * This component renders:
 * 1. A `Text` view showing the ingredient name, prefixed by its step number (e.g., "Ingredient 1",
 *    "Ingredient 2").
 * 2. A `Delete` icon button that removes the ingredient from the list.
 *
 * The `index` is used to display the ingredient number in the format "Ingredient 1", "Ingredient
 * 2", etc. The `ingredient` parameter holds the ingredient's details, particularly its name. The
 * `onRemoveClick` callback is triggered when the delete icon button is clicked, allowing the
 * ingredient to be removed.
 *
 * @param index The index of the ingredient in the list (used to display ingredient number).
 * @param ingredient The ingredient object that holds information about the ingredient, including
 *   its name.
 * @param onRemoveClick A lambda function that is triggered when the delete button is clicked to
 *   remove the ingredient.
 */
@Composable
fun IngredientItem(index: Int, ingredient: Ingredient, onRemoveClick: () -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    // title of ingredient
    Text(text = "Ingredient${index + 1} : ${ingredient.foodFacts.name}")
  }
  // delete button
  IconButton(
      onClick = onRemoveClick,
  ) {
    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Ingredient")
  }
}

/**
 * A composable dialog that allows the user to add an ingredient with its name, quantity, and unit.
 * It validates the inputs and only allows the addition if the data is complete and correct.
 *
 * @param ingredients The mutable list of ingredients where the new ingredient will be added.
 * @param onDismiss A callback function that is called when the dialog is dismissed.
 * @param onAddIngredient A callback function that is called when a new ingredient is successfully
 *   added.
 */
@Composable
fun IngredientDialog(
    ingredients: MutableList<Ingredient>,
    onDismiss: () -> Unit,
    onAddIngredient: () -> Unit
) {
  val context = LocalContext.current

  var ingredientName by remember { mutableStateOf("") }
  var ingredientQuantity by remember { mutableStateOf("") }
  var ingredientUnit by remember { mutableStateOf(FoodUnit.COUNT) } // default value is COUNT

  var selectedUnit by remember {
    mutableStateOf(FoodUnit.COUNT)
  } // We store this so that the colour of the button can change depending on its status

  var ingredientNameError by remember { mutableStateOf<String?>("") }
  var quantityError by remember { mutableStateOf<String?>("") }
  var error = false

  androidx.compose.material3.AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Add Ingredient") },
      text = {
        Column {
          // ingredient name
          OutlinedTextField(
              value = ingredientName,
              onValueChange = {
                ingredientName = it
                ingredientNameError = if (ingredientName.isEmpty()) "Incomplete name" else null
              },
              label = { Text("Ingredient Name") },
              modifier = Modifier.fillMaxWidth())
          // error message if ingredient name is empty
          ErrorTextBox(ingredientNameError)

          // ingredient quantity (it is a string but will be transformed later on)
          OutlinedTextField(
              value = ingredientQuantity,
              onValueChange = {
                ingredientQuantity = it
                quantityError = if (ingredientQuantity.isEmpty()) "Incomplete quantity" else null
              },
              label = { Text("Quantity") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth())

          // error message if the ingredient quantity is empty
          ErrorTextBox(quantityError)

          // Quantity Unit Dropdown
          Row {
            FoodUnit.values().forEach { unit ->
              Button(
                  onClick = {
                    ingredientUnit = unit
                    selectedUnit = unit
                  },
                  // colour of button changes if selected
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor =
                              if (selectedUnit == unit) primaryContainerDark
                              else primaryContainerLight,
                          contentColor =
                              if (selectedUnit == unit) secondaryContainerLight
                              else secondaryContainerDark),
              ) {
                Text(text = unit.name)
              }
              Spacer(Modifier.padding(2.dp))
            }
          }
        }
      },
      // add button
      confirmButton = {
        Button(
            onClick = {
              error = ingredientName.isEmpty() || ingredientQuantity.isEmpty()
              if (!error) {
                // turn quantity into double
                val quantity = ingredientQuantity.toDouble()
                // create the new ingredient
                val newIngredient =
                    Ingredient(
                        foodFacts =
                            FoodFacts(
                                name = ingredientName,
                                quantity = Quantity(quantity, ingredientUnit)),
                        isOwned = false)
                // adding it into our list of ingredients
                ingredients.add(newIngredient)
                onAddIngredient()
              } else {
                Toast.makeText(
                        context, "Please correct the errors before submitting.", Toast.LENGTH_SHORT)
                    .show()
              }
            },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLight,
                    contentColor = secondaryContainerDark)) {
              Text("Add Ingredient")
            }
      },
      // Cancel button
      dismissButton = {
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = errorContainerDark)) {
              Text("Cancel")
            }
      })
}

/**
 * A composable function that displays an error message if the provided string is not null or empty.
 *
 * This composable is typically used to show validation or error messages for user inputs. It
 * displays the message in the color defined in the Material Theme's error color and uses a smaller
 * body text style to distinguish it as an error.
 *
 * @param errorMessage The error message to display. If null or empty, no text is shown.
 */
@Composable
fun ErrorTextBox(errorMessage: String?) {
  if (errorMessage != null) {
    Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
  }
}
