package com.android.shelfLife.ui.recipes

import android.annotation.SuppressLint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.android.shelfLife.ui.navigation.HouseHoldSelectionDrawer
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.overview.FirstTimeWelcomeScreen
import com.example.compose.errorContainerDark
import com.example.compose.onPrimaryContainerDark
import com.example.compose.onSecondaryContainerDark
import com.example.compose.onSecondaryDark
import com.example.compose.primaryContainerLight
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navigationActions: NavigationActions,
    listRecipesViewModel: ListRecipesViewModel,
    householdViewModel: HouseholdViewModel
) {
    var title by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("0") }
    var time by remember { mutableStateOf("0.0") }
    val ingredients = remember { mutableStateListOf<Ingredient>() }
    val instructions = remember { mutableStateListOf("") }

    // Dialog visibility state
    var showIngredientDialog by remember { mutableStateOf(false) }

    // Ingredient data state
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQuantity by remember { mutableStateOf("") } // Ensure this stays as String for user input
    var ingredientUnit by remember { mutableStateOf(FoodUnit.COUNT) }

    Scaffold(
        modifier = Modifier.testTag("addRecipeScreen"),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.testTag("topBar"),
                navigationIcon = {
                    // Back button to return to the previous screen
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("goBackArrow")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back Icon"
                        )
                    }
                },
                // Title of the screen: Recipe name
                title = {
                    Text(
                        text = "Add your own recipe",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp, fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recipe title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                        .testTag("inputRecipeTitle")
                )
            }

            item {
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
                    label = { Text("Servings") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                        .testTag("inputRecipeServings")
                )
            }

            item {
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time in minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                        .testTag("inputRecipeTime")
                )
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Ingredients")
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showIngredientDialog = true }, // State change to show dialog
                    modifier = Modifier.height(40.dp),
                    content = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ingredient")
                    }
                )
            }

            itemsIndexed(ingredients) { index, ingredient ->
                IngredientItem(
                    index = index,
                    ingredient = ingredient,
                    onRemoveClick = {
                        if (ingredients.size > 0) {
                            ingredients.removeAt(index)
                        }
                    }
                )
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Instructions")
                }
            }

            itemsIndexed(instructions) { index, instruction ->
                InstructionItem(
                    index = index,
                    instruction = instruction,
                    onInstructionChange = { newInstruction ->
                        instructions[index] = newInstruction
                    },
                    onRemoveClick = {
                        if (instructions.size > 1) {
                            instructions.removeAt(index)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { instructions.add("") },
                    modifier = Modifier.height(40.dp),
                    content = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Instruction")
                    }
                )
            }

            // Footer buttons: Cancel and Add
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = errorContainerDark)
                    ) {
                        Text(text = "Cancel", fontSize = 18.sp)
                    }

                    Spacer(Modifier.width(24.dp))

                    Button(
                        onClick = {
                            listRecipesViewModel.addRecipeToList(recipe = Recipe(name = title,
                                instructions = instructions.toList(),
                                servings = servings.toInt(),
                                time = (time.toDouble()*60.0).seconds,
                                ingredients = ingredients.toList()
                            ))
                            navigationActions.goBack()},
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryContainerLight)
                    ) {
                        Text(text = "Add", fontSize = 18.sp, color = onSecondaryDark)
                    }
                }
            }
        }

        // Ingredient Dialog, now inside the composable block
        if (showIngredientDialog) {
            IngredientDialog(
                ingredientName = ingredientName,
                onNameChange = { ingredientName = it },
                ingredientQuantity = ingredientQuantity,
                onQuantityChange = { ingredientQuantity = it },
                ingredientUnit = ingredientUnit,
                onUnitChange = { ingredientUnit = it },
                onDismiss = { showIngredientDialog = false }, // Hide dialog
                onAddIngredient = {
                    // Try to safely parse the quantity
                    val quantity = ingredientQuantity.toDoubleOrNull()
                    if (quantity != null) {
                        val newIngredient = Ingredient(
                            foodFacts = FoodFacts(
                                name = ingredientName,
                                quantity = Quantity(quantity, ingredientUnit)
                            ),
                            isOwned = false
                        )
                        ingredients.add(newIngredient)
                        showIngredientDialog = false // Dismiss dialog after adding
                    } else {
                        // Handle invalid input (e.g., show a message to the user)
                    }
                }
            )
        }
    }
}


@Composable
fun InstructionItem(
    index: Int,
    instruction: String,
    onInstructionChange: (String) -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = instruction,
            onValueChange = onInstructionChange,
            label = { Text("Step ${index + 1}") },
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRemoveClick,
            enabled = index > 0 // Disable removal for the first item to ensure at least one instruction remains
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Step"
            )
        }
    }
}

@Composable
fun IngredientItem(
    index :Int,
    ingredient: Ingredient,
    onRemoveClick: () -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(text = "Ingredient${index} : ${ingredient.foodFacts.name}")
    }
    IconButton(
        onClick = onRemoveClick,
        enabled = index > 0 // Disable removal for the first item to ensure at least one instruction remains
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Ingredient"
        )
    }
}

@Composable
fun IngredientDialog(
    ingredientName: String,
    onNameChange: (String) -> Unit,
    ingredientQuantity: String,
    onQuantityChange: (String) -> Unit,
    ingredientUnit: FoodUnit,
    onUnitChange: (FoodUnit) -> Unit,
    onDismiss: () -> Unit,
    onAddIngredient: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column {
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = onNameChange,
                    label = { Text("Ingredient Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredientQuantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                // Quantity Unit Dropdown
                FoodUnit.values().forEach { unit ->
                    Button(onClick = { onUnitChange(unit) }) {
                        Text(text = unit.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onAddIngredient) {
                Text("Add Ingredient")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
