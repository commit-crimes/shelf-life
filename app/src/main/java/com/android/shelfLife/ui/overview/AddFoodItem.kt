package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
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
import com.android.shelfLife.ui.utils.DateVisualTransformation
import com.android.shelfLife.ui.utils.DropdownFields
import com.android.shelfLife.ui.utils.formatDateToTimestamp
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.ui.utils.fromCapitalStringToLowercaseString
import com.android.shelfLife.ui.utils.getDateErrorMessage
import com.android.shelfLife.ui.utils.isDateAfterOrEqual
import com.android.shelfLife.ui.utils.isValidDateNotPast
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
    val namePattern = Regex("^[a-zA-Z0-9\\s\\-,'()]+\$")
    foodNameError =
        when {
          foodName.isBlank() -> "Food name cannot be empty."
          !namePattern.matches(foodName) -> "Food name contains invalid characters."
          else -> null
        }

    // Amount validation
    amountError =
        when {
          amount.isBlank() -> "Amount cannot be empty."
          amount.toDoubleOrNull() == null -> "Amount must be a number."
          amount.toDoubleOrNull() != null && amount.toDouble() <= 0 -> "Amount must be positive."
          else -> null
        }

    // Expire Date validation
    expireDateError = getDateErrorMessage(expireDate)
    if (expireDateError == null &&
        expireDate.length == 8 &&
        buyDateError == null &&
        buyDate.length == 8) {
      if (!isDateAfterOrEqual(expireDate, buyDate)) {
        expireDateError = "Expire Date cannot be before Buy Date"
      }
    }
    if (expireDateError == null && expireDate.length == 8) {
      if (!isValidDateNotPast(expireDate)) {
        expireDateError = "Expire Date cannot be in the past"
      }
    }

    // Open Date validation
    openDateError = getDateErrorMessage(openDate, isRequired = false)
    if (openDateError == null &&
        openDate.isNotEmpty() &&
        buyDateError == null &&
        openDate.length == 8 &&
        buyDate.length == 8) {
      if (!isDateAfterOrEqual(openDate, buyDate)) {
        openDateError = "Open Date cannot be before Buy Date"
      }
    }
    if (openDateError == null &&
        openDate.isNotEmpty() &&
        expireDateError == null &&
        openDate.length == 8 &&
        expireDate.length == 8) {
      if (!isDateAfterOrEqual(expireDate, openDate)) {
        openDateError = "Open Date cannot be after Expire Date"
      }
    }

    // Buy Date validation
    buyDateError = getDateErrorMessage(buyDate)
    if (buyDateError == null && buyDate.length == 8) {
      if (openDate.isNotEmpty() && openDateError == null && openDate.length == 8) {
        if (!isDateAfterOrEqual(openDate, buyDate)) {
          openDateError = "Open Date cannot be before Buy Date"
        } else {
          openDateError = null
        }
      }
      if (expireDateError == null && expireDate.length == 8) {
        if (!isDateAfterOrEqual(expireDate, buyDate)) {
          expireDateError = "Expire Date cannot be before Buy Date"
        } else if (!isValidDateNotPast(expireDate)) {
          expireDateError = "Expire Date cannot be in the past"
        } else {
          expireDateError = null
        }
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
                  text = stringResource(id = R.string.add_food_item_title))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                  }
            })
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding)
                    .testTag("addFoodItemScreen")
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              // Food Name Field with Error Handling
              OutlinedTextField(
                  value = foodName,
                  onValueChange = { newValue ->
                    foodName = newValue
                    // Allow letters, numbers, spaces, and basic punctuation
                    val namePattern = Regex("^[a-zA-Z0-9\\s\\-,'()]+\$")
                    foodNameError =
                        when {
                          foodName.isBlank() -> "Food name cannot be empty."
                          !namePattern.matches(foodName) -> "Food name contains invalid characters."
                          else -> null
                        }
                  },
                  label = { Text(stringResource(id = R.string.food_name_hint)) },
                  isError = foodNameError != null,
                  modifier = Modifier.testTag("inputFoodName").fillMaxWidth())
              if (foodNameError != null) {
                Text(
                    text = foodNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start))
              }
              Spacer(modifier = Modifier.height(16.dp))

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                      // Amount Field with Error Handling
                      OutlinedTextField(
                          value = amount,
                          onValueChange = { newValue ->
                            amount = newValue
                            amountError =
                                when {
                                  amount.isBlank() -> "Amount cannot be empty."
                                  amount.toDoubleOrNull() == null -> "Amount must be a number."
                                  amount.toDoubleOrNull() != null && amount.toDouble() <= 0 ->
                                      "Amount must be positive."
                                  else -> null
                                }
                          },
                          label = { Text(stringResource(id = R.string.amount_hint)) },
                          isError = amountError != null,
                          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                          modifier = Modifier.testTag("inputFoodAmount").fillMaxWidth())
                      if (amountError != null) {
                        Text(
                            text = amountError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start))
                      }
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
                        modifier = Modifier.weight(1f).testTag("inputFoodUnit"))
                  }
              Spacer(modifier = Modifier.height(16.dp))

              // Category dropdown
              DropdownFields(
                  label = stringResource(id = R.string.category_label),
                  options = FoodCategory.entries.toTypedArray(),
                  selectedOption = category,
                  onOptionSelected = { category = it },
                  expanded = categoryExpanded,
                  onExpandedChange = { categoryExpanded = it },
                  optionLabel = { fromCapitalStringToLowercaseString(it.name) },
                  modifier = Modifier.testTag("inputFoodCategory").fillMaxWidth())
              Spacer(modifier = Modifier.height(16.dp))

              // Location Dropdown
              DropdownFields(
                  label = stringResource(id = R.string.location_label),
                  options = FoodStorageLocation.entries.toTypedArray(),
                  selectedOption = location,
                  onOptionSelected = { location = it },
                  expanded = locationExpanded,
                  onExpandedChange = { locationExpanded = it },
                  optionLabel = { fromCapitalStringToLowercaseString(it.name) },
                  modifier = Modifier.testTag("inputFoodLocation").fillMaxWidth())
              Spacer(modifier = Modifier.height(16.dp))

              // Expire Date Field with Error Handling
              OutlinedTextField(
                  value = expireDate,
                  onValueChange = { newValue ->
                    expireDate = newValue.filter { it.isDigit() }
                    expireDateError = getDateErrorMessage(expireDate)
                    if (expireDateError == null &&
                        expireDate.length == 8 &&
                        buyDateError == null &&
                        buyDate.length == 8) {
                      // Additional validation: openDate >= buyDate
                      if (!isDateAfterOrEqual(expireDate, buyDate)) {
                        expireDateError = "Expire Date cannot be before Buy Date"
                      }
                    }
                    // Check if expire date is not in the past
                    if (expireDateError == null && expireDate.length == 8) {
                      if (!isValidDateNotPast(expireDate)) {
                        expireDateError = "Expire Date cannot be in the past"
                      }
                    }
                  },
                  label = { Text(stringResource(id = R.string.expire_date_hint)) },
                  placeholder = { Text("dd/mm/yyyy") },
                  isError = expireDateError != null,
                  visualTransformation = DateVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                  modifier = Modifier.testTag("inputFoodExpireDate").fillMaxWidth())
              if (expireDateError != null) {
                Text(
                    text = expireDateError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start))
              }
              Spacer(modifier = Modifier.height(16.dp))

              // Open Date Field with Error Handling
              OutlinedTextField(
                  value = openDate,
                  onValueChange = { newValue ->
                    openDate = newValue.filter { it.isDigit() }
                    openDateError = getDateErrorMessage(openDate, isRequired = false)
                    if (openDateError == null &&
                        openDate.isNotEmpty() &&
                        buyDateError == null &&
                        openDate.length == 8 &&
                        buyDate.length == 8) {
                      // Additional validation: openDate >= buyDate
                      if (!isDateAfterOrEqual(openDate, buyDate)) {
                        openDateError = "Open Date cannot be before Buy Date"
                      }
                    }
                    // Additional validation: openDate <= expireDate
                    if (openDateError == null &&
                        openDate.isNotEmpty() &&
                        expireDateError == null &&
                        openDate.length == 8 &&
                        expireDate.length == 8) {
                      if (!isDateAfterOrEqual(expireDate, openDate)) {
                        openDateError = "Open Date cannot be after Expire Date"
                      }
                    }
                  },
                  label = { Text(stringResource(id = R.string.open_date_hint)) },
                  placeholder = { Text("dd/mm/yyyy") },
                  isError = openDateError != null,
                  visualTransformation = DateVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                  modifier = Modifier.testTag("inputFoodOpenDate").fillMaxWidth(),
              )
              if (openDateError != null) {
                Text(
                    text = openDateError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start))
              }
              Spacer(modifier = Modifier.height(16.dp))

              // Buy Date Field with Error Handling
              OutlinedTextField(
                  value = buyDate,
                  onValueChange = { newValue ->
                    buyDate = newValue.filter { it.isDigit() }
                    buyDateError = getDateErrorMessage(buyDate)
                    // Re-validate openDate and expireDate against buyDate
                    if (buyDateError == null && buyDate.length == 8) {
                      if (openDate.isNotEmpty() && openDateError == null && openDate.length == 8) {
                        if (!isDateAfterOrEqual(openDate, buyDate)) {
                          openDateError = "Open Date cannot be before Buy Date"
                        } else {
                          openDateError = null
                        }
                      }
                      if (expireDateError == null && expireDate.length == 8) {
                        if (!isDateAfterOrEqual(expireDate, buyDate)) {
                          expireDateError = "Expire Date cannot be before Buy Date"
                        } else if (!isValidDateNotPast(expireDate)) {
                          expireDateError = "Expire Date cannot be in the past"
                        } else {
                          expireDateError = null
                        }
                      }
                    }
                  },
                  label = { Text(stringResource(id = R.string.buy_date_hint)) },
                  placeholder = { Text("dd/mm/yyyy") },
                  isError = buyDateError != null,
                  visualTransformation = DateVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                  modifier = Modifier.testTag("inputFoodBuyDate").fillMaxWidth(),
              )
              if (buyDateError != null) {
                Text(
                    text = buyDateError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start))
              }
              Spacer(modifier = Modifier.height(32.dp))

              // Submit Button
              Button(
                  onClick = {
                    validateAllFieldsWhenSubmitButton()
                    // Validate all inputs before proceeding
                    val isExpireDateValid = expireDateError == null && expireDate.isNotEmpty()
                    val isOpenDateValid = openDateError == null
                    val isBuyDateValid = buyDateError == null && buyDate.isNotEmpty()
                    val isFoodNameValid = foodNameError == null
                    val isAmountValid = amountError == null

                    val expiryTimestamp = formatDateToTimestamp(expireDate)
                    val openTimestamp =
                        if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
                    val buyTimestamp = formatDateToTimestamp(buyDate)

                    if (isExpireDateValid &&
                        isOpenDateValid &&
                        isBuyDateValid &&
                        isFoodNameValid &&
                        isAmountValid &&
                        expiryTimestamp != null &&
                        buyTimestamp != null) {
                      val foodFacts =
                          FoodFacts(
                              name = foodName,
                              barcode = "",
                              quantity = Quantity(amount.toDouble(), unit),
                              category = category)
                      val newFoodItem =
                          FoodItem(
                              uid = foodItemViewModel.getUID(),
                              foodFacts = foodFacts,
                              location = location,
                              expiryDate = expiryTimestamp,
                              openDate = openTimestamp,
                              buyDate = buyTimestamp,
                              status = FoodStatus.CLOSED)
                      houseHoldViewModel.addFoodItem(newFoodItem)
                      navigationActions.goBack()
                    } else {
                      // Handle the case where validation fails
                      Toast.makeText(
                              context,
                              "Please correct the errors before submitting.",
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                  },
                  modifier = Modifier.testTag("foodSave").fillMaxWidth().height(50.dp),
              ) {
                Text(text = "Submit", fontSize = 18.sp)
              }
            }
      }
}
