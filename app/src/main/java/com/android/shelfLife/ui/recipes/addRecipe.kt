package com.android.shelfLife.ui.recipes

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.theme.onSecondaryDark
import com.android.shelfLife.ui.theme.primaryContainerDark
import com.android.shelfLife.ui.theme.primaryContainerLight
import com.android.shelfLife.ui.theme.secondaryContainerDark
import com.android.shelfLife.ui.theme.secondaryContainerLight
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
) {
  val context = LocalContext.current

  var title by remember { mutableStateOf("") }
  var servings by remember { mutableStateOf("") }
  var time by remember { mutableStateOf("") }
  val ingredients = remember { mutableStateListOf<Ingredient>() }
  val instructions = remember { mutableStateListOf<String>() }

  var showIngredientDialog by remember { mutableStateOf(false) }

  var error = false
  var titleError by remember { mutableStateOf<String?>(null) }
  var servingsError by remember { mutableStateOf<String?>(null) }
  var timeError by remember { mutableStateOf<String?>(null) }
  var instructionsError by remember { mutableStateOf(false) }

  // Helper function to validate if any instruction is empty
  fun validateInstructions() {
    instructionsError = instructions.any { it.isBlank() }
  }

  Scaffold(
      modifier = Modifier.testTag("addRecipeScreen"),
      topBar = {
        CustomTopAppBar(
            onClick = { navigationActions.goBack() },
            title = stringResource(R.string.title_of_AddRecipeScreen),
            titleTestTag = "addRecipeTitle")
      },
      content = { paddingValues -> // New content parameter
        LazyColumn(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 20.dp).fillMaxSize()) {
              // Recipe title
              item {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                      title = it
                      titleError =
                          if (title.isEmpty())
                              context.getString(R.string.error_message_title_of_recipe)
                          else null
                    },
                    label = { Text(stringResource(R.string.title_of_recipe)) },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .testTag("inputRecipeTitle"))
              }

              // Error message for title
              item { ErrorTextBox(titleError, "titleErrorMessage") }

              // Recipe servings
              item {
                OutlinedTextField(
                    value = servings,
                    onValueChange = {
                      servings = it
                      servingsError =
                          if (servings.isEmpty()) context.getString(R.string.error_message_servings)
                          else null
                    },
                    label = { Text(stringResource(R.string.servings)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .testTag("inputRecipeServings"))
              }

              // Error message for servings
              item { ErrorTextBox(servingsError, "servingsErrorMessage") }

              // Recipe time
              item {
                OutlinedTextField(
                    value = time,
                    onValueChange = {
                      time = it
                      timeError =
                          if (time.isEmpty()) context.getString(R.string.error_message_time)
                          else null
                    },
                    label = { Text(stringResource(R.string.time)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .testTag("inputRecipeTime"))
              }

              // Error message for time
              item { ErrorTextBox(timeError, "timeErrorMessage") }

              // Recipe Ingredients Section
              item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                      text = stringResource(R.string.ingredients),
                      modifier = Modifier.testTag("ingredientSection"))
                }
              }

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

              // Add Ingredient Button
              item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showIngredientDialog = true },
                    modifier = Modifier.height(40.dp).testTag("addIngredientButton"),
                    content = {
                      Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ingredient")
                    })
              }

              // Instructions Section
              item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                      text = stringResource(R.string.instructions),
                      modifier = Modifier.testTag("instructionSection"))
                }
              }

              itemsIndexed(instructions) { index, instruction ->
                InstructionItem(
                    index = index,
                    instruction = instruction,
                    onInstructionChange = { newInstruction ->
                      instructions[index] = newInstruction
                      validateInstructions()
                    },
                    onRemoveClick = { instructions.removeAt(index) })
              }

              // Add Instruction Button
              item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { instructions.add("") },
                    modifier = Modifier.height(40.dp).testTag("addInstructionButton"),
                    content = {
                      Icon(imageVector = Icons.Default.Add, contentDescription = "Add Instruction")
                    })
              }

              // Footer Buttons
              item {
                CustomButtons(
                    button1OnClick = { navigationActions.goBack() },
                    button1TestTag = "cancelButton",
                    button1Text = stringResource(R.string.cancel_button),
                    button2OnClick = {
                        validateInstructions()
                        error =
                            title.isEmpty() ||
                                    time.isEmpty() ||
                                    servings.isEmpty() ||
                                    ingredients.isEmpty() ||
                                    instructions.isEmpty() ||
                                    instructionsError
                        if (!error) {
                            navigationActions.goBack()
                            listRecipesViewModel.saveRecipe(
                                recipe =
                                Recipe(
                                    uid = "",
                                    name = title,
                                    instructions = instructions.toList(),
                                    servings = servings.toFloat(),
                                    time = (time.toDouble() * 60.0).seconds,
                                    ingredients = ingredients.toList()))
                        } else {
                            // if not a Toast appears
                            Toast.makeText(
                                context,
                                "Please correct the errors before submitting.",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    button2TestTag = "addButton",
                    button2Text = stringResource(R.string.add_button))
              }
            }

        // Ingredient Dialog, inside the composable block
        if (showIngredientDialog) {
          IngredientDialog(
              ingredients = ingredients,
              onDismiss = { showIngredientDialog = false },
              onAddIngredient = { showIngredientDialog = false })
        }
      })
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
  val context = LocalContext.current

  var instructionError by remember { mutableStateOf<String?>(null) }
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = instruction,
        onValueChange = { newInstruction ->
          onInstructionChange(newInstruction)
          instructionError =
              if (newInstruction.isEmpty()) context.getString(R.string.error_message_instructions)
              else null
        },
        label = { Text(stringResource(R.string.instruction_step, index + 1)) },
        modifier = Modifier.weight(1f).testTag("inputRecipeInstruction"))
    IconButton(onClick = onRemoveClick, modifier = Modifier.testTag("deleteInstructionButton")) {
      Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Step")
    }
  }
  // Display error message if needed
  ErrorTextBox(instructionError, "instructionErrorMessage")
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
    Text(
        text = stringResource(R.string.ingredient_item, index + 1, ingredient.name),
        modifier = Modifier.testTag("ingredientItem"))
    // delete button
    IconButton(onClick = onRemoveClick, modifier = Modifier.testTag("deleteIngredientButton")) {
      Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Ingredient")
    }
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
      title = { Text(stringResource(R.string.add_ingredient)) },
      text = {
        Column(modifier = Modifier.testTag("addIngredientPopUp")) {
          // ingredient name
          OutlinedTextField(
              value = ingredientName,
              onValueChange = {
                ingredientName = it
                ingredientNameError =
                    if (ingredientName.isEmpty())
                        context.getString(R.string.error_message_ingredient_name)
                    else null
              },
              label = { Text(stringResource(R.string.ingredient_name)) },
              modifier = Modifier.fillMaxWidth().testTag("inputIngredientName"))
          // error message if ingredient name is empty
          ErrorTextBox(ingredientNameError, "ingredientNameErrorMessage")

          // ingredient quantity (it is a string but will be transformed later on)
          OutlinedTextField(
              value = ingredientQuantity,
              onValueChange = {
                ingredientQuantity = it
                quantityError =
                    if (ingredientQuantity.isEmpty())
                        context.getString(R.string.error_message_ingredient_quantity)
                    else null
              },
              label = { Text(stringResource(R.string.ingredient_quantity)) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth().testTag("inputIngredientQuantity"))

          // error message if the ingredient quantity is empty
          ErrorTextBox(quantityError, "ingredientQuantityErrorMessage")

          // Quantity Unit Dropdown
          Row {
            FoodUnit.values().forEach { unit ->
              Button(
                  onClick = {
                    ingredientUnit = unit
                    selectedUnit = unit
                  },
                  modifier = Modifier.testTag("ingredientUnitButton"),
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
                    Ingredient(name = ingredientName, quantity = Quantity(quantity, ingredientUnit))
                // adding it into our list of ingredients
                ingredients.add(newIngredient)
                onAddIngredient()
              } else {
                Toast.makeText(
                        context, "Please correct the errors before submitting.", Toast.LENGTH_SHORT)
                    .show()
              }
            },
            modifier = Modifier.testTag("addIngredientButton2"),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = primaryContainerDark,
                    contentColor = secondaryContainerLight)) {
              Text(stringResource(R.string.add_ingredient))
            }
      },
      // Cancel button
      dismissButton = {
        Button(
            onClick = onDismiss,
            modifier = Modifier.testTag("cancelIngredientButton"),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = secondaryContainerLight, contentColor = onSecondaryDark)) {
              Text(stringResource(R.string.cancel_button))
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
fun ErrorTextBox(errorMessage: String?, testTag: String) {
  if (errorMessage != null) {
    Text(
        text = errorMessage,
        modifier = Modifier.testTag(testTag),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
  }
}
