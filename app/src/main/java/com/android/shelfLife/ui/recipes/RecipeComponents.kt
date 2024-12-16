package com.android.shelfLife.ui.recipes

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.ui.theme.onSecondaryDark
import com.android.shelfLife.ui.theme.primaryContainerDark
import com.android.shelfLife.ui.theme.secondaryContainerLight
import com.android.shelfLife.ui.utils.UnitDropdownField
import com.android.shelfLife.viewmodel.recipes.AddRecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeTitleOutlinedTextField(addRecipeViewModel: AddRecipeViewModel){
    OutlinedTextField(
        value = addRecipeViewModel.title.collectAsState().value,
        onValueChange = {newTitle -> addRecipeViewModel.changeTitle(newTitle)},
        label = { Text(stringResource(R.string.title_of_recipe)) },
        modifier =
        Modifier.fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("inputRecipeTitle")
    )
}

@Composable
fun RecipeServingsOutlinedTextField(addRecipeViewModel: AddRecipeViewModel){
    OutlinedTextField(
        value = addRecipeViewModel.servings.collectAsState().value,
        onValueChange = { newServings ->
            addRecipeViewModel.changeServings(newServings)
        },
        label = { Text(stringResource(R.string.servings)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier =
        Modifier.fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("inputRecipeServings")
    )
}

@Composable
fun RecipeTimeOutlinedTextField(addRecipeViewModel: AddRecipeViewModel){
    androidx.compose.material3.OutlinedTextField(
        value = addRecipeViewModel.time.collectAsState().value,
        onValueChange = { newTime -> addRecipeViewModel.changeTime(newTime) },
        label = { Text(stringResource(R.string.time)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier =
        Modifier.fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("inputRecipeTime")
    )
}

@Composable
fun RecipeIngredientsText(){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.ingredients),
            modifier = Modifier.testTag("ingredientSection"))
    }
}

@Composable
fun RecipeAddIngredientButton(addRecipeViewModel: AddRecipeViewModel){
    Spacer(modifier = Modifier.height(4.dp))
    Button(
        onClick = { addRecipeViewModel.createNewIngredient() },
        modifier = Modifier.height(40.dp).testTag("addIngredientButton"),
        content = {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ingredient")
        })
}

@Composable
fun RecipeInstructionText(){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.instructions),
            modifier = Modifier.testTag("instructionSection"))
    }
}

@Composable
fun RecipeAddIngredient(addRecipeViewModel: AddRecipeViewModel){
    Spacer(modifier = Modifier.height(4.dp))
    Button(
        onClick = { addRecipeViewModel.createNewInstruction() },
        modifier = Modifier.height(40.dp).testTag("addInstructionButton"),
        content = {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Instruction")
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
    addRecipeViewModel: AddRecipeViewModel,
) {

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        androidx.compose.material3.OutlinedTextField(
            value = addRecipeViewModel.instructions.collectAsState().value[index],
            onValueChange = { newInstruction ->
                Log.i("RecipeComponents", "Change in instruction")
                Log.i("RecipeComponent", "instructions : ${addRecipeViewModel.instructions.value.toString()}")
                Log.i("RecipeComponent", "instruction errors _ ${addRecipeViewModel.instructionError.value.toString()}")
                addRecipeViewModel.changeInstruction(index, newInstruction)
            },
            label = { Text(stringResource(R.string.instruction_step, index + 1)) },
            modifier = Modifier.weight(1f).testTag("inputRecipeInstruction"))
        IconButton(
            onClick = { addRecipeViewModel.removeInstruction(index) },
            modifier = Modifier.testTag("deleteInstructionButton")) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Step")
        }
    }

    if(addRecipeViewModel.instructionError.collectAsState().value.isNotEmpty() ){
        if (addRecipeViewModel.instructionError.collectAsState().value[index] != -1){
            // Display error message if needed
            ErrorTextBoxNEW(
                addRecipeViewModel.instructionError.collectAsState().value[index], "instructionErrorMessage")
        }
    }
}

val UNITS = mapOf(FoodUnit.GRAM to "gram", FoodUnit.ML to "mL", FoodUnit.COUNT to "")

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
            text =
            stringResource(
                R.string.ingredient_item,
                index + 1,
                ingredient.quantity.amount,
                UNITS.get(ingredient.quantity.unit)!!,
                ingredient.name),
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
fun IngredientDialog(addRecipeViewModel: AddRecipeViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val ingredientUnit = addRecipeViewModel.ingredientQuantityUnit.collectAsState()

    androidx.compose.material3.AlertDialog(
        onDismissRequest = { addRecipeViewModel.createNewIngredient() },
        title = { Text(stringResource(R.string.add_ingredient)) },
        text = {
            Column(modifier = Modifier.testTag("addIngredientPopUp")) {
                // ingredient name
                androidx.compose.material3.OutlinedTextField(
                    value = addRecipeViewModel.ingredientName.collectAsState().value,
                    onValueChange = { newName -> addRecipeViewModel.changeIngredientName(newName) },
                    label = { Text(stringResource(R.string.ingredient_name)) },
                    modifier = Modifier.fillMaxWidth().testTag("inputIngredientName")
                )
                // error message if ingredient name is empty
                ErrorTextBoxNEW(
                    addRecipeViewModel.ingredientNameError.collectAsState().value,
                    "ingredientNameErrorMessage")

                // ingredient quantity (it is a string but will be transformed later on)
                androidx.compose.material3.OutlinedTextField(
                    value = addRecipeViewModel.ingredientQuantityAmount.collectAsState().value,
                    onValueChange = { newAmount ->
                        addRecipeViewModel.changeIngredientQuantityAmount(newAmount)
                    },
                    label = { Text(stringResource(R.string.ingredient_quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("inputIngredientQuantity")
                )

                // error message if the ingredient quantity is empty
                ErrorTextBoxNEW(
                    addRecipeViewModel.ingredientQuantityAmountError.collectAsState().value,
                    "ingredientQuantityErrorMessage")

                Spacer(Modifier.padding(8.dp))

                // Quantity Unit Dropdown
                Row {
                    UnitDropdownField(
                        unit = ingredientUnit.value,
                        onUnitChange = { newUnit ->
                            addRecipeViewModel.changeIngredientQuantityUnit(newUnit)
                        },
                        unitExpanded = addRecipeViewModel.unitExpanded,
                        onUnitExpandedChange = { addRecipeViewModel.changeUnitExpanded() },
                        modifier = Modifier.weight(1f),
                        testTag = "inputIngredientUnit")
                }
            }
        },
        // add button
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val ingredientAdded = addRecipeViewModel.addNewIngredient()
                        if (ingredientAdded) {
                            addRecipeViewModel.closeIngredientDialog()
                        } else {
                            Toast.makeText(
                                context,
                                "Please correct the errors before submitting.",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                modifier = Modifier.testTag("addIngredientButton2"),
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = primaryContainerDark,
                    contentColor = secondaryContainerLight
                )) {
                Text(stringResource(R.string.add_ingredient))
            }
        },
        // Cancel button
        dismissButton = {
            Button(
                onClick = { addRecipeViewModel.closeIngredientDialog() },
                modifier = Modifier.testTag("cancelIngredientButton"),
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = secondaryContainerLight, contentColor = onSecondaryDark
                )) {
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
 * @param errorMessageId The error message to display. If null or empty, no text is shown.
 */
@Composable
fun ErrorTextBoxNEW(errorMessageId: Int?, testTag: String) {
    if (errorMessageId != null) {
        Text(
            text = stringResource(errorMessageId),
            modifier = Modifier.testTag(testTag),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
