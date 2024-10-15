package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.ErrorPopUp
import com.android.shelfLife.ui.utils.formatDateToTimestamp
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.ui.utils.fromCapitalStringtoLowercaseString
import com.example.compose.primaryContainerLight
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * A composable function that displays the screen for adding a new food item to the user's
 * household inventory.
 *
 * @param navigationActions An instance of [NavigationActions] that handles navigation actions.
 * @param houseHoldViewModel An instance of [HouseholdViewModel] that provides access to the user's
 * household data.
 * @param foodItemViewModel An instance of [ListFoodItemsViewModel] that provides access to the user's
 * food item data.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodItemScreen(
    navigationActions: NavigationActions,
    houseHoldViewModel: HouseholdViewModel,
    foodItemViewModel: ListFoodItemsViewModel
) {
    var foodName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(FoodUnit.GRAM) }
    var category by remember { mutableStateOf(FoodCategory.OTHER) }
    var location by remember { mutableStateOf(FoodStorageLocation.PANTRY) }
    var expireDate by remember { mutableStateOf("") }
    var openDate by remember { mutableStateOf("") }
    var buyDate by remember { mutableStateOf(formatTimestampToDate(Timestamp.now())) }

    val errorMessages by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var showDialog by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Add Food Item") }) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Name of food") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = fromCapitalStringtoLowercaseString(unit.name),
                        onValueChange = {},
                        label = { Text("Unit") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded, onDismissRequest = { unitExpanded = false }
                    ) {
                        FoodUnit.values().forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(fromCapitalStringtoLowercaseString(selectionOption.name)) },
                                onClick = {
                                    unit = selectionOption
                                    unitExpanded = false // Close dropdown
                                }
                            )
                        }
                    }
                }
            }

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = fromCapitalStringtoLowercaseString(category.name),
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }
                ) {
                    FoodCategory.values().forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(fromCapitalStringtoLowercaseString(selectionOption.name)) },
                            onClick = {
                                category = selectionOption
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = !locationExpanded }
            ) {
                OutlinedTextField(
                    value = fromCapitalStringtoLowercaseString(location.name),
                    onValueChange = {},
                    label = { Text("Location") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = locationExpanded, onDismissRequest = { locationExpanded = false }
                ) {
                    FoodStorageLocation.values().forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(fromCapitalStringtoLowercaseString(selectionOption.name)) },
                            onClick = {
                                location = selectionOption
                                locationExpanded = false
                            }
                        )
                    }
                }
            }

            // For dates a future improvement could be having a calendar interfare rather than manual
            // input
            OutlinedTextField(
                value = expireDate,
                onValueChange = { expireDate = it },
                label = { Text("Expire Date") },
                placeholder = { Text("dd/mm/yyyy") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )

            OutlinedTextField(
                value = openDate,
                onValueChange = { openDate = it },
                label = { Text("Open Date") },
                placeholder = { Text("dd/mm/yyyy") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )

            OutlinedTextField(
                value = buyDate,
                onValueChange = { buyDate = it },
                label = { Text("Buy Date") },
                placeholder = { Text("dd/mm/yyyy") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            )

            Button(
                onClick = {
                    errorMessages.clear()


                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                    try {
                        val expireDateParsed = sdf.parse(expireDate)
                        val openDateParsed = sdf.parse(openDate)

                        //Error if the expiration date is before the open date
                        if (expireDateParsed.before(openDateParsed)) {
                            errorMessages["date"] = "Expiration date cannot be before the open date."
                        }
                    } catch (e: Exception) {
                        errorMessages["dateFormat"] = "Invalid date format. Please use dd/mm/yyyy."
                    }

                    //Error is the food name field is empty
                    if (foodName.isBlank()) {
                        errorMessages["foodName"] = "Food name cannot be empty."
                    }

                    //Error is the food amount is blank or not a number
                    if (amount.isBlank()) {
                        errorMessages["amount"] = "Amount cannot be empty."
                    } else if (amount.toDoubleOrNull() == null) {
                        errorMessages["amountFormat"] = "Amount must be a number."
                    }

                    if (errorMessages.isNotEmpty()) {
                        showDialog = true
                    } else {
                        val foodFacts =
                            FoodFacts(
                                name = foodName,
                                barcode = "",
                                quantity = Quantity(amount.toDouble(), unit),
                                category = category
                            )
                        val newFoodItem =
                            FoodItem(
                                uid = foodItemViewModel.getUID(),
                                foodFacts = foodFacts,
                                location = location,
                                expiryDate = formatDateToTimestamp(expireDate),
                                openDate = formatDateToTimestamp(openDate),
                                buyDate = formatDateToTimestamp(buyDate),
                                status = FoodStatus.CLOSED
                            )
                        houseHoldViewModel.addFoodItem(newFoodItem)
                        navigationActions.goBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryContainerLight)
            ) {
                Text(text = "Submit", fontSize = 18.sp)
            }

            ErrorPopUp(
                showDialog = showDialog,
                onDismiss = { showDialog = false },
                errorMessages = errorMessages.values.toList()
            )
        }
    }
}
