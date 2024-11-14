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

/**
 * Composable function to display the Add Food Item screen.
 *
 * @param navigationActions The navigation actions to be used in the screen.
 * @param houseHoldViewModel The ViewModel for the household.
 * @param foodItemViewModel The ViewModel for the food items.
 * @param paddingValues The padding values to be applied to the screen.
 */
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

    /**
     * Validates all fields when the submit button is clicked.
     */
    fun validateAllFieldsWhenSubmitButton() {
        foodNameError = validateFoodName(foodName)
        amountError = validateAmount(amount)
        buyDateError = validateBuyDate(buyDate)
        expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
        openDateError = validateOpenDate(openDate, buyDate, buyDateError, expireDate, expireDateError)
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
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding)
                    .testTag("addFoodItemScreen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              item(key = "foodName") {
                // Food Name Field with Error Handling
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { newValue ->
                      foodName = newValue
                      foodNameError = validateFoodName(foodName)
                    },
                    label = { Text(stringResource(id = R.string.food_name_hint)) },
                    isError = foodNameError != null,
                    modifier = Modifier.testTag("inputFoodName").fillMaxWidth())
                if (foodNameError != null) {
                  Text(
                      text = foodNameError!!,
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.bodySmall,
                      modifier = Modifier.fillMaxWidth(),
                      textAlign = TextAlign.Start)
                }
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "amount") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Column(modifier = Modifier.weight(1f)) {
                        // Amount Field with Error Handling
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { newValue ->
                              amount = newValue
                              amountError = validateAmount(amount)
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
                              modifier = Modifier.fillMaxWidth(),
                              textAlign = TextAlign.Start)
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
                    modifier = Modifier.testTag("inputFoodCategory").fillMaxWidth())
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
                    modifier = Modifier.testTag("inputFoodLocation").fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "expireDate") {
                // Expire Date Field with Error Handling
                OutlinedTextField(
                    value = expireDate,
                    onValueChange = { newValue ->
                      expireDate = newValue.filter { it.isDigit() }
                      expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
                      // Re-validate Open Date since it depends on Expire Date
                      openDateError =
                          validateOpenDate(
                              openDate, buyDate, buyDateError, expireDate, expireDateError)
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
                      modifier = Modifier.fillMaxWidth(),
                      textAlign = TextAlign.Start)
                }
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "openDate") {
                // Open Date Field with Error Handling
                OutlinedTextField(
                    value = openDate,
                    onValueChange = { newValue ->
                      openDate = newValue.filter { it.isDigit() }
                      openDateError =
                          validateOpenDate(
                              openDate, buyDate, buyDateError, expireDate, expireDateError)
                    },
                    label = { Text(stringResource(id = R.string.open_date_hint)) },
                    placeholder = { Text("dd/mm/yyyy") },
                    isError = openDateError != null,
                    visualTransformation = DateVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.testTag("inputFoodOpenDate").fillMaxWidth())
                if (openDateError != null) {
                  Text(
                      text = openDateError!!,
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.bodySmall,
                      modifier = Modifier.fillMaxWidth(),
                      textAlign = TextAlign.Start)
                }
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "buyDate") {
                // Buy Date Field with Error Handling
                OutlinedTextField(
                    value = buyDate,
                    onValueChange = { newValue ->
                      buyDate = newValue.filter { it.isDigit() }
                      buyDateError = validateBuyDate(buyDate)
                      // Re-validate Expire Date and Open Date since they depend on Buy Date
                      expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
                      openDateError =
                          validateOpenDate(
                              openDate, buyDate, buyDateError, expireDate, expireDateError)
                    },
                    label = { Text(stringResource(id = R.string.buy_date_hint)) },
                    placeholder = { Text("dd/mm/yyyy") },
                    isError = buyDateError != null,
                    visualTransformation = DateVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.testTag("inputFoodBuyDate").fillMaxWidth())
                if (buyDateError != null) {
                  Text(
                      text = buyDateError!!,
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.bodySmall,
                      modifier = Modifier.fillMaxWidth(),
                      textAlign = TextAlign.Start)
                }
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
                        Toast.makeText(
                                context,
                                "Please correct the errors before submitting.",
                                Toast.LENGTH_SHORT)
                            .show()
                      }
                    },
                    modifier = Modifier.testTag("foodSave").fillMaxWidth().height(50.dp)) {
                      Text(text = "Submit", fontSize = 18.sp)
                    }
              }
            }
      }
}
