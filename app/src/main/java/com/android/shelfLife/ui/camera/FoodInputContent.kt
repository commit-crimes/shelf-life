// FoodInputContent.kt
package com.android.shelfLife.ui.camera

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.ui.utils.DateVisualTransformation
import com.android.shelfLife.ui.utils.formatDateToTimestamp
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.ui.utils.getDateErrorMessage
import com.android.shelfLife.ui.utils.isDateAfterOrEqual
import com.google.firebase.Timestamp

/** Composable function for the bottom sheet content with date input fields and error handling. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodInputContent(
    foodFacts: FoodFacts,
    onSubmit: (FoodItem) -> Unit,
    onCancel: () -> Unit,
    foodItemViewModel: ListFoodItemsViewModel,
) {
    val context = LocalContext.current
    var location by remember { mutableStateOf(FoodStorageLocation.PANTRY) }
    var expireDate by rememberSaveable { mutableStateOf("") }
    var openDate by rememberSaveable { mutableStateOf("") }
    var buyDate by rememberSaveable { mutableStateOf(formatTimestampToDate(Timestamp.now())) }

    var expireDateError by remember { mutableStateOf<String?>(null) }
    var openDateError by remember { mutableStateOf<String?>(null) }
    var buyDateError by remember { mutableStateOf<String?>(null) }

    var locationExpanded by remember { mutableStateOf(false) }

    // Add vertical scroll
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState) // Make the Column scrollable
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Food information
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = foodFacts.name,
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = Color(0xFF000000),
                    )
                )

                Text(
                    text = foodFacts.category.name,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Color(0xFF000000),
                    )
                )
            }

            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Food Image",
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Dropdown
        ExposedDropdownMenuBox(
            expanded = locationExpanded,
            onExpandedChange = { locationExpanded = !locationExpanded },
            modifier = Modifier.testTag("locationDropdown")
        ) {
            OutlinedTextField(
                value = location.name.lowercase(),
                onValueChange = {},
                label = { Text("Location") },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("locationTextField")
            )
            ExposedDropdownMenu(
                expanded = locationExpanded,
                onDismissRequest = { locationExpanded = false },
                modifier = Modifier.testTag("locationMenu")
            ) {
                FoodStorageLocation.entries.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.name) },
                        onClick = {
                            location = selectionOption
                            locationExpanded = false
                        },
                        modifier = Modifier.testTag("locationOption_${selectionOption.name}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expire Date Field with Error Handling and Input Masking
        OutlinedTextField(
            value = expireDate,
            onValueChange = { newValue ->
                expireDate = newValue.filter { it.isDigit() }
                expireDateError = getDateErrorMessage(expireDate)
            },
            label = { Text("Expire Date") },
            placeholder = { Text("dd/MM/yyyy") },
            isError = expireDateError != null,
            visualTransformation = DateVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("expireDateTextField")
        )
        if (expireDateError != null) {
            Text(
                text = expireDateError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Open Date Field with Error Handling and Input Masking
        OutlinedTextField(
            value = openDate,
            onValueChange = { newValue ->
                openDate = newValue.filter { it.isDigit() }
                openDateError = getDateErrorMessage(openDate, isRequired = false)

                // Additional validation only if openDate is not empty
                if (openDateError == null &&
                    openDate.isNotEmpty() &&
                    buyDateError == null &&
                    openDate.length == 8 &&
                    buyDate.length == 8
                ) {
                    if (!isDateAfterOrEqual(openDate, buyDate)) {
                        openDateError = "Open Date cannot be before Buy Date"
                    } else {
                        openDateError = null
                    }
                }
            },
            label = { Text("Open Date") },
            placeholder = { Text("dd/MM/yyyy") },
            isError = openDateError != null,
            visualTransformation = DateVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("openDateTextField")
        )
        if (openDateError != null) {
            Text(
                text = openDateError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Buy Date Field with Error Handling and Input Masking
        OutlinedTextField(
            value = buyDate,
            onValueChange = { newValue ->
                buyDate = newValue.filter { it.isDigit() }
                buyDateError = getDateErrorMessage(buyDate)

                // Re-validate openDate against buyDate
                if (openDateError == null &&
                    buyDateError == null &&
                    openDate.length == 8 &&
                    buyDate.length == 8
                ) {
                    if (!isDateAfterOrEqual(openDate, buyDate)) {
                        openDateError = "Open Date cannot be before Buy Date"
                    } else {
                        openDateError = null
                    }
                }
            },
            label = { Text("Buy Date") },
            placeholder = { Text("dd/MM/yyyy") },
            isError = buyDateError != null,
            visualTransformation = DateVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("buyDateTextField")
        )
        if (buyDateError != null) {
            Text(
                text = buyDateError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button
        Button(
            onClick = {
                // Validate all inputs before proceeding
                val isExpireDateValid = expireDateError == null && expireDate.isNotEmpty()
                val isOpenDateValid =
                    openDateError == null // No need to check length since it's optional
                val isBuyDateValid = buyDateError == null && buyDate.isNotEmpty()

                val expiryTimestamp = formatDateToTimestamp(expireDate)
                val openTimestamp =
                    if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
                val buyTimestamp = formatDateToTimestamp(buyDate)

                if (isExpireDateValid &&
                    isOpenDateValid &&
                    isBuyDateValid &&
                    expiryTimestamp != null &&
                    buyTimestamp != null
                ) {
                    val newFoodItem = FoodItem(
                        uid = foodItemViewModel.getUID(),
                        foodFacts = foodFacts,
                        location = location,
                        expiryDate = expiryTimestamp,
                        openDate = openTimestamp,
                        buyDate = buyTimestamp,
                        // Additional logic for status if needed
                    )
                    onSubmit(newFoodItem)
                } else {
                    // Handle the case where validation fails
                    Toast.makeText(
                        context,
                        "Please correct the errors before submitting.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submitButton")
        ) {
            Text(text = "Submit", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel Button
        Button(
            onClick = { onCancel() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("cancelButton")
        ) {
            Text(text = "Cancel", fontSize = 18.sp)
        }
    }
}