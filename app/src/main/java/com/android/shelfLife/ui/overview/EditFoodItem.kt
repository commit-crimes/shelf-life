package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.utils.*

/**
 * Composable function to display the Edit Food Item screen.
 *
 * @param navigationActions The navigation actions to be used in the screen.
 * @param houseHoldViewModel The ViewModel for the household.
 * @param foodItemViewModel The ViewModel for the food items.
 * @param paddingValues The padding values to be applied to the screen.
 */
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

  var amountErrorResId by remember { mutableStateOf<Int?>(null) }
  var expireDateErrorResId by remember { mutableStateOf<Int?>(null) }
  var openDateErrorResId by remember { mutableStateOf<Int?>(null) }
  var buyDateErrorResId by remember { mutableStateOf<Int?>(null) }

  var locationExpanded by remember { mutableStateOf(false) }

  val context = LocalContext.current
  /** Validates all fields when the submit button is clicked. */
  fun validateAllFieldsWhenSubmitButton() {
    amountErrorResId = validateAmount(amount)
    buyDateErrorResId = validateBuyDate(buyDate)
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        CustomTopAppBar(
            onClick = { navigationActions.goBack() },
            title = stringResource(id = R.string.edit_food_item_title),
            titleTestTag = "editFoodItemTitle",
            actions = {
              IconButton(
                  onClick = {
                    selectedFood?.let {
                      houseHoldViewModel.deleteFoodItem(it)
                      navigationActions.navigateTo(Route.OVERVIEW)
                    }
                  },
                  modifier = Modifier.testTag("deleteFoodItem")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
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
                    verticalAlignment = Alignment.CenterVertically) {
                      AmountField(
                          amount = amount,
                          onAmountChange = { newValue ->
                            amount = newValue
                            amountErrorResId = validateAmount(amount)
                          },
                          amountErrorResId = amountErrorResId,
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
                      expireDateErrorResId =
                          validateExpireDate(expireDate, buyDate, buyDateErrorResId)
                      // Re-validate Open Date since it depends on Expire Date
                      openDateErrorResId =
                          validateOpenDate(
                              openDate,
                              buyDate,
                              buyDateErrorResId,
                              expireDate,
                              expireDateErrorResId)
                    },
                    dateErrorResId = expireDateErrorResId,
                    labelResId = R.string.expire_date_hint,
                    testTag = "editFoodExpireDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "openDate") {
                DateField(
                    date = openDate,
                    onDateChange = { newValue ->
                      openDate = newValue.filter { it.isDigit() }
                      openDateErrorResId =
                          validateOpenDate(
                              openDate,
                              buyDate,
                              buyDateErrorResId,
                              expireDate,
                              expireDateErrorResId)
                    },
                    dateErrorResId = openDateErrorResId,
                    labelResId = R.string.open_date_hint,
                    testTag = "editFoodOpenDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "buyDate") {
                DateField(
                    date = buyDate,
                    onDateChange = { newValue ->
                      buyDate = newValue.filter { it.isDigit() }
                      buyDateErrorResId = validateBuyDate(buyDate)
                      // Re-validate Expire Date and Open Date since they depend on Buy Date
                      expireDateErrorResId =
                          validateExpireDate(expireDate, buyDate, buyDateErrorResId)
                      openDateErrorResId =
                          validateOpenDate(
                              openDate,
                              buyDate,
                              buyDateErrorResId,
                              expireDate,
                              expireDateErrorResId)
                    },
                    dateErrorResId = buyDateErrorResId,
                    labelResId = R.string.buy_date_hint,
                    testTag = "editFoodBuyDate")
                Spacer(modifier = Modifier.height(32.dp))
              }

              item(key = "buttons") {
                CustomButtons(
                    button1OnClick = { navigationActions.goBack() },
                    button1TestTag = "cancelButton",
                    button1Text = stringResource(R.string.cancel_button),
                    button2OnClick = {
                      validateAllFieldsWhenSubmitButton()
                      val isAmountValid = amountErrorResId == null
                      val isExpireDateValid =
                          expireDateErrorResId == null && expireDate.isNotEmpty()
                      val isOpenDateValid = openDateErrorResId == null
                      val isBuyDateValid = buyDateErrorResId == null && buyDate.isNotEmpty()

                      val expiryTimestamp = formatDateToTimestamp(expireDate)
                      val openTimestamp =
                          if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
                      val buyTimestamp = formatDateToTimestamp(buyDate)

                      if (isAmountValid &&
                          isExpireDateValid &&
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
                                context, R.string.submission_error_message, Toast.LENGTH_SHORT)
                            .show()
                      }
                    },
                    button2TestTag = "foodSave",
                    button2Text = stringResource(R.string.submit_button_text))
              }
            }
      }
}
