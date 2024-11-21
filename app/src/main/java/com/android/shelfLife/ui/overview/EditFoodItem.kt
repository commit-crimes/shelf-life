package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.*

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
  var openDate by remember {
    mutableStateOf(
        if (selectedFood.openDate == null) "" else formatTimestampToDate(selectedFood.openDate))
  }
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
        TopAppBar(
            title = {
              Text(
                  modifier = Modifier.testTag("editFoodItemTitle"),
                  text = stringResource(id = R.string.edit_food_item_title))
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
              item(key = "amountAndUnit") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      AmountField(
                          amount = amount,
                          onAmountChange = { newValue ->
                            amount = newValue
                            amountError = validateAmount(amount)
                          },
                          amountError = amountError,
                          modifier = Modifier.weight(1f),
                          testTag = "editFoodAmount")
                      Spacer(modifier = Modifier.width(8.dp))
                      // Display unit as card (non-editable)
                      Card(
                          border = CardDefaults.outlinedCardBorder(),
                          shape = MaterialTheme.shapes.large,
                          modifier = Modifier.weight(1f).testTag("editFoodUnit")) {
                            Text(
                                text = selectedFood.foodFacts.quantity.unit.name,
                                modifier = Modifier.padding(12.dp))
                          }
                    }
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "location") {
                LocationDropdownField(
                    location = location,
                    onLocationChange = { location = it },
                    locationExpanded = locationExpanded,
                    onExpandedChange = { locationExpanded = it },
                    testTag = "editFoodLocation")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "expireDate") {
                DateField(
                    date = expireDate,
                    onDateChange = { newValue ->
                      expireDate = newValue.filter { it.isDigit() }
                      expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
                      openDateError =
                          validateOpenDate(
                              openDate, buyDate, buyDateError, expireDate, expireDateError)
                    },
                    dateError = expireDateError,
                    labelResId = R.string.expire_date_hint,
                    testTag = "editFoodExpireDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "openDate") {
                DateField(
                    date = openDate,
                    onDateChange = { newValue ->
                      openDate = newValue.filter { it.isDigit() }
                      openDateError =
                          validateOpenDate(
                              openDate, buyDate, buyDateError, expireDate, expireDateError)
                    },
                    dateError = openDateError,
                    labelResId = R.string.open_date_hint,
                    testTag = "editFoodOpenDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "buyDate") {
                DateField(
                    date = buyDate,
                    onDateChange = { newValue ->
                      buyDate = newValue.filter { it.isDigit() }
                      buyDateError = validateBuyDate(buyDate)
                      expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
                      openDateError =
                          validateOpenDate(
                              openDate, buyDate, buyDateError, expireDate, expireDateError)
                    },
                    dateError = buyDateError,
                    labelResId = R.string.buy_date_hint,
                    testTag = "editFoodBuyDate")
                Spacer(modifier = Modifier.height(32.dp))
              }

              item(key = "submitButton") {
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
                                category = selectedFood.foodFacts.category,
                                imageUrl = selectedFood.foodFacts.imageUrl,
                            )

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
                        foodItemViewModel.selectFoodItem(newFoodItem)
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
