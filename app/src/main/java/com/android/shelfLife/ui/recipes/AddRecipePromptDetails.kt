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
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.theme.errorContainerDark
import com.android.shelfLife.ui.theme.onSecondaryDark
import com.android.shelfLife.ui.theme.primaryContainerDark
import com.android.shelfLife.ui.theme.primaryContainerLight
import com.android.shelfLife.ui.theme.secondaryContainerDark
import com.android.shelfLife.ui.theme.secondaryContainerLight
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipePromptDetailsScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
    householdViewModel: HouseholdViewModel
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(false) }
    val ingredientsOwned = remember { mutableStateListOf<FoodItem>() }
    val ingredientsNotOwned = remember { mutableStateListOf<Ingredient>() }
    val specialInstructions = remember { mutableStateOf("") }

    // Dialog visibility state
    var showIngredientDialog by remember { mutableStateOf(false) }

    var error = false
    var titleError by remember { mutableStateOf<String?>(null) }
    var servingsError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.testTag("addRecipePromptDetailsScreen"),
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
                        text = "Generate your own recipe",
                        style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp, fontWeight = FontWeight.Bold))
                })
        }) {

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
            item { ErrorTextBox(titleError, "titleErrorMessage") }

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
            item { ErrorTextBox(servingsError, "servingsErrorMessage") }

            // recipe time
            item {
                //TODO add dropdown for whether they want fast recipe or slow recipe
            }

            // recipe Ingredients owned
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Ingredients In Your Inventory",
                        modifier = Modifier.testTag("ingredientSection"))
                }
            }

            // list of ingredients
            itemsIndexed(ingredientsOwned) { index, ingredient ->
                IngredientOwnedItem(
                    index = index,
                    ingredient = ingredient,
                    onRemoveClick = {
                        if (ingredientsOwned.size > 0) {
                            ingredientsOwned.removeAt(index)
                        }
                    })
            }

            // recipe Ingredients not owned
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Other Ingredients",
                        modifier = Modifier.testTag("ingredientSection"))
                }
            }

            itemsIndexed(ingredientsNotOwned) { index, ingredient ->
                IngredientItem(
                    index = index,
                    ingredient = ingredient,
                    onRemoveClick = {
                        if (ingredientsNotOwned.size > 0) {
                            ingredientsNotOwned.removeAt(index)
                        }
                    })
            }

            // add button for more ingredients
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showIngredientDialog = true }, // State change to show dialog
                    modifier = Modifier.height(40.dp).testTag("addIngredientButton"),
                    content = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ingredient")
                    })
            }

            // recipe instructions
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Instructions", modifier = Modifier.testTag("instructionSection"))
                }
            }

            // add instructions text
            item {
                Spacer(modifier = Modifier.height(4.dp))
                //TODO add a text field for special instruction
            }

            // Footer buttons: Cancel and Add
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center) {
                    // Cancel button
                    Button(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.height(40.dp).testTag("cancelButton"),
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
                                        servings.isEmpty() ||
                                        ingredientsOwned.isEmpty() ||
                                        ingredientsNotOwned.isEmpty()
                            if (!error) {
                                //TODO give to backend
                            } else {
                                // if not a Toast appears
                                Toast.makeText(
                                    context,
                                    "Please correct the errors before submitting.",
                                    Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        modifier = Modifier.height(40.dp).testTag("addButton"),
                        colors =
                        ButtonDefaults.buttonColors(containerColor = primaryContainerLight)) {
                        Text(text = "Add", fontSize = 18.sp, color = onSecondaryDark)
                    }
                }
            }
        }
    }
}

//TODO change this so it looks nicer and has the image of the ingredient
@Composable
fun IngredientOwnedItem(index: Int, ingredient: FoodItem, onRemoveClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        // title of ingredient
        Text(
            text = "Ingredient${index + 1} : ${ingredient.foodFacts.name}",
            modifier = Modifier.testTag("ingredientItem"))
        // delete button
        IconButton(onClick = onRemoveClick, modifier = Modifier.testTag("deleteIngredientButton")) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Ingredient")
        }
    }
}
