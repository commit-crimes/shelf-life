package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.*
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodItemScreen(
    navigationActions: NavigationActions,
    houseHoldViewModel: HouseholdViewModel,
    foodItemViewModel: ListFoodItemsViewModel,
    paddingValues: PaddingValues = PaddingValues(16.dp)
) {

    var foodName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(FoodUnit.GRAM) }
    var category by remember { mutableStateOf(FoodCategory.OTHER) }
    var location by remember { mutableStateOf(FoodStorageLocation.PANTRY) }
    var expireDate by remember { mutableStateOf("") }
    var openDate by remember { mutableStateOf("") }
    var buyDate by remember { mutableStateOf(formatTimestampToDate(Timestamp.now())) }

    var foodNameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var expireDateError by remember { mutableStateOf<String?>(null) }
    var openDateError by remember { mutableStateOf<String?>(null) }
    var buyDateError by remember { mutableStateOf<String?>(null) }

    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun validateAllFieldsWhenSubmitButton() {
        // Food Name validation
        foodNameError = validateFoodName(foodName)

        // Amount validation
        amountError = validateAmount(amount)

        // Expire Date validation
        expireDateError = validateDateField(expireDate, isRequired = true)

        // Open Date validation
        openDateError = validateDateField(openDate, isRequired = false)

        // Buy Date validation
        buyDateError = validateDateField(buyDate, isRequired = true)
    }

    fun performCrossFieldValidations() {
        // Validate that expire date is after or equal to buy date
        if (expireDateError == null && buyDateError == null && expireDate.length == 8 && buyDate.length == 8) {
            if (!isDateAfterOrEqual(expireDate, buyDate)) {
                expireDateError = "Expire Date cannot be before Buy Date"
            } else if (!isValidDateNotPast(expireDate)) {
                expireDateError = "Expire Date cannot be in the past"
            }
        }

        // Validate that open date is after or equal to buy date
        if (openDateError == null && buyDateError == null && openDate.isNotEmpty() && openDate.length == 8 && buyDate.length == 8) {
            if (!isDateAfterOrEqual(openDate, buyDate)) {
                openDateError = "Open Date cannot be before Buy Date"
            }
        }

        // Validate that open date is before or equal to expire date
        if (openDateError == null && expireDateError == null && openDate.isNotEmpty() && openDate.length == 8 && expireDate.length == 8) {
            if (!isDateAfterOrEqual(expireDate, openDate)) {
                openDateError = "Open Date cannot be after Expire Date"
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.testTag("addFoodItemTitle"),
                        text = stringResource(id = R.string.add_food_item_title)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("goBackButton")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding)
                .testTag("addFoodItemScreen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item(key = "foodName") {
                InputFieldWithError(
                    value = foodName,
                    onValueChange = { newValue ->
                        foodName = newValue
                        foodNameError = validateFoodName(foodName)
                    },
                    label = stringResource(id = R.string.food_name_hint),
                    isError = foodNameError != null,
                    errorMessage = foodNameError,
                    testTag = "inputFoodName"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "amount") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        InputFieldWithError(
                            value = amount,
                            onValueChange = { newValue ->
                                amount = newValue
                                amountError = validateAmount(amount)
                            },
                            label = stringResource(id = R.string.amount_hint),
                            isError = amountError != null,
                            errorMessage = amountError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            testTag = "inputFoodAmount"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Unit Dropdown
                    DropdownFields(
                        label = stringResource(id = R.string.unit_label),
                        options = FoodUnit.entries.toTypedArray(),
                        selectedOption = unit,
                        onOptionSelected = { unit = it },
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        optionLabel = { fromCapitalStringToLowercaseString(it.name) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inputFoodUnit")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "category") {
                // Category Dropdown
                DropdownFields(
                    label = stringResource(id = R.string.category_label),
                    options = FoodCategory.entries.toTypedArray(),
                    selectedOption = category,
                    onOptionSelected = { category = it },
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    optionLabel = { fromCapitalStringToLowercaseString(it.name) },
                    modifier = Modifier
                        .testTag("inputFoodCategory")
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "location") {
                // Location Dropdown
                DropdownFields(
                    label = stringResource(id = R.string.location_label),
                    options = FoodStorageLocation.entries.toTypedArray(),
                    selectedOption = location,
                    onOptionSelected = { location = it },
                    expanded = locationExpanded,
                    onExpandedChange = { locationExpanded = it },
                    optionLabel = { fromCapitalStringToLowercaseString(it.name) },
                    modifier = Modifier
                        .testTag("inputFoodLocation")
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "expireDate") {
                InputFieldWithError(
                    value = expireDate,
                    onValueChange = { newValue ->
                        expireDate = newValue.filter { it.isDigit() }
                        expireDateError = validateDateField(expireDate, isRequired = true)
                        // Cross-field validations
                        performCrossFieldValidations()
                    },
                    label = stringResource(id = R.string.expire_date_hint),
                    placeholder = "dd/mm/yyyy",
                    isError = expireDateError != null,
                    errorMessage = expireDateError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = DateVisualTransformation(),
                    testTag = "inputFoodExpireDate"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "openDate") {
                InputFieldWithError(
                    value = openDate,
                    onValueChange = { newValue ->
                        openDate = newValue.filter { it.isDigit() }
                        openDateError = validateDateField(openDate, isRequired = false)
                        // Cross-field validations
                        performCrossFieldValidations()
                    },
                    label = stringResource(id = R.string.open_date_hint),
                    placeholder = "dd/mm/yyyy",
                    isError = openDateError != null,
                    errorMessage = openDateError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = DateVisualTransformation(),
                    testTag = "inputFoodOpenDate"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "buyDate") {
                InputFieldWithError(
                    value = buyDate,
                    onValueChange = { newValue ->
                        buyDate = newValue.filter { it.isDigit() }
                        buyDateError = validateDateField(buyDate, isRequired = true)
                        // Cross-field validations
                        performCrossFieldValidations()
                    },
                    label = stringResource(id = R.string.buy_date_hint),
                    placeholder = "dd/mm/yyyy",
                    isError = buyDateError != null,
                    errorMessage = buyDateError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = DateVisualTransformation(),
                    testTag = "inputFoodBuyDate"
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item(key = "submitButton") {
                // Submit Button
                Button(
                    onClick = {
                        validateAllFieldsWhenSubmitButton()
                        val isExpireDateValid = expireDateError == null && expireDate.isNotEmpty()
                        val isOpenDateValid = openDateError == null
                        val isBuyDateValid = buyDateError == null && buyDate.isNotEmpty()
                        val isFoodNameValid = foodNameError == null
                        val isAmountValid = amountError == null

                        val expiryTimestamp = formatDateToTimestamp(expireDate)
                        val openTimestamp = if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
                        val buyTimestamp = formatDateToTimestamp(buyDate)

                        if (isExpireDateValid &&
                            isOpenDateValid &&
                            isBuyDateValid &&
                            isFoodNameValid &&
                            isAmountValid &&
                            expiryTimestamp != null &&
                            buyTimestamp != null
                        ) {
                            val foodFacts = FoodFacts(
                                name = foodName,
                                barcode = "",
                                quantity = Quantity(amount.toDouble(), unit),
                                category = category
                            )
                            val newFoodItem = FoodItem(
                                uid = foodItemViewModel.getUID(),
                                foodFacts = foodFacts,
                                location = location,
                                expiryDate = expiryTimestamp,
                                openDate = openTimestamp,
                                buyDate = buyTimestamp,
                                status = FoodStatus.CLOSED
                            )
                            houseHoldViewModel.addFoodItem(newFoodItem)
                            navigationActions.goBack()
                        } else {
                            Toast.makeText(
                                context,
                                "Please correct the errors before submitting.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .testTag("foodSave")
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Submit", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun InputFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isError: Boolean,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            isError = isError,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}

// Validation functions
fun validateFoodName(name: String): String? {
    val namePattern = Regex("^[a-zA-Z0-9\\s\\-,'()]+\$")
    return when {
        name.isBlank() -> "Food name cannot be empty."
        !namePattern.matches(name) -> "Food name contains invalid characters."
        else -> null
    }
}

fun validateAmount(amount: String): String? {
    return when {
        amount.isBlank() -> "Amount cannot be empty."
        amount.toDoubleOrNull() == null -> "Amount must be a number."
        amount.toDouble() <= 0 -> "Amount must be positive."
        else -> null
    }
}

fun validateDateField(date: String, isRequired: Boolean = true): String? {
    return getDateErrorMessage(date, isRequired)
}
