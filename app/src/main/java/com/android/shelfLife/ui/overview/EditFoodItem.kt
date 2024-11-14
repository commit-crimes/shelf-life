package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.android.shelfLife.model.foodFacts.FoodFacts
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
import com.android.shelfLife.ui.utils.validateAmount
import com.android.shelfLife.ui.utils.validateBuyDate
import com.android.shelfLife.ui.utils.validateExpireDate
import com.android.shelfLife.ui.utils.validateOpenDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodItemScreen(
    navigationActions: NavigationActions,
    houseHoldViewModel: HouseholdViewModel,
    foodItemViewModel: ListFoodItemsViewModel,
    paddingValues: PaddingValues = PaddingValues(16.dp)
) {
  val selectedFoodItem by foodItemViewModel.selectedFoodItem.collectAsState()
  val selectedFood = selectedFoodItem ?: return

  var amount by remember { mutableStateOf(selectedFood.foodFacts.quantity.amount.toString()) }
  var location by remember { mutableStateOf(selectedFood.location) }
  var expireDate by remember { mutableStateOf(formatTimestampToDate(selectedFood.expiryDate!!)) }
  var openDate by
      if (selectedFood.openDate == null) {
        remember { mutableStateOf("") }
      } else remember { mutableStateOf(formatTimestampToDate(selectedFood.openDate)) }

  var buyDate by remember { mutableStateOf(formatTimestampToDate(selectedFood.buyDate)) }

  var amountError by remember { mutableStateOf<String?>(null) }
  var expireDateError by remember { mutableStateOf<String?>(null) }
  var openDateError by remember { mutableStateOf<String?>(null) }
  var buyDateError by remember { mutableStateOf<String?>(null) }

  var locationExpanded by remember { mutableStateOf(false) }

  val context = LocalContext.current

  fun validateAllFieldsWhenSubmitButton() {
    buyDateError = validateBuyDate(buyDate)
    expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
    openDateError = validateOpenDate(openDate, buyDate, buyDateError, expireDate, expireDateError)
  }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        androidx.compose.material3.TopAppBar(
            title = {
              Text(
                  modifier = Modifier.testTag("editFoodItemTitle"),
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
                    .testTag("editFoodItemScreen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
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
                            modifier = Modifier.testTag("editFoodAmount").fillMaxWidth())
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
                      // Unit
                      // TODO this doesnt look good on the UI
                      Card(
                          border = CardDefaults.outlinedCardBorder(),
                          modifier = Modifier.testTag("editFoodUnit")) {
                            Text(text = selectedFood.foodFacts.quantity.unit.name)
                          }
                    }
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
                    modifier = Modifier.testTag("editFoodLocation").fillMaxWidth())
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
                    modifier = Modifier.testTag("editFoodExpireDate").fillMaxWidth())
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
                    modifier = Modifier.testTag("editFoodOpenDate").fillMaxWidth())
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
                    modifier = Modifier.testTag("editFoodBuyDate").fillMaxWidth())
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

                      val expiryTimestamp = formatDateToTimestamp(expireDate)
                      val openTimestamp =
                          if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
                      val buyTimestamp = formatDateToTimestamp(buyDate)

                      if (isExpireDateValid &&
                          isOpenDateValid &&
                          isBuyDateValid &&
                          expiryTimestamp != null &&
                          buyTimestamp != null) {
                        val foodFacts =
                            FoodFacts(
                                name = selectedFood.foodFacts.name,
                                barcode = selectedFood.foodFacts.barcode,
                                quantity =
                                    Quantity(
                                        amount.toDouble(), selectedFood.foodFacts.quantity.unit),
                                category = selectedFood.foodFacts.category)

                        val newFoodItem =
                            FoodItem(
                                uid = foodItemViewModel.getUID(),
                                foodFacts = foodFacts,
                                location = location,
                                expiryDate = expiryTimestamp,
                                openDate = openTimestamp,
                                buyDate = buyTimestamp,
                                status = FoodStatus.CLOSED)
                        houseHoldViewModel.editFoodItem(newFoodItem, selectedFood)
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

                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "cancelButton") {
                Button(
                    onClick = { navigationActions.goBack() },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("cancelButton")) {
                      Text(text = "Cancel", fontSize = 18.sp)
                    }
              }
            }
      }
}
