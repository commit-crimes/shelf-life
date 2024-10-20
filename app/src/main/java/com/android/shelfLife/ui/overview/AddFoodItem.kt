package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.android.shelfLife.ui.utils.DropdownFields
import com.android.shelfLife.ui.utils.ErrorPopUp
import com.android.shelfLife.ui.utils.formatDateToTimestamp
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.ui.utils.fromCapitalStringToLowercaseString
import com.example.compose.primaryContainerLight
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * A composable function that displays the screen for adding a new food item to the user's household
 * inventory.
 *
 * @param navigationActions An instance of [NavigationActions] that handles navigation actions.
 * @param houseHoldViewModel An instance of [HouseholdViewModel] that provides access to the user's
 *   household data.
 * @param foodItemViewModel An instance of [ListFoodItemsViewModel] that provides access to the
 *   user's food item data.
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
      modifier = Modifier.fillMaxSize().testTag("addScreen"),
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
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Go Back")
                  }
            })
      },
  ) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
          OutlinedTextField(
              value = foodName,
              onValueChange = { foodName = it },
              label = { Text(stringResource(id = R.string.food_name_hint)) },
              modifier = Modifier.testTag("inputFoodName").fillMaxWidth().padding(bottom = 16.dp))

          Row(
              modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(id = R.string.amount_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.testTag("inputFoodAmount").weight(1f).padding(end = 8.dp))

                DropdownFields(
                    label = stringResource(id = R.string.unit_label),
                    options = FoodUnit.values(),
                    selectedOption = unit,
                    onOptionSelected = { unit = it },
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it },
                    optionLabel = { fromCapitalStringToLowercaseString(it.name) },
                    modifier = Modifier.weight(1f).testTag("inputFoodUnit"))
              }

          // Category dropdown
          DropdownFields(
              label = stringResource(id = R.string.category_label),
              options = FoodCategory.values(),
              selectedOption = category,
              onOptionSelected = { category = it },
              expanded = categoryExpanded,
              onExpandedChange = { categoryExpanded = it },
              optionLabel = { fromCapitalStringToLowercaseString(it.name) },
              modifier = Modifier.testTag("inputFoodCategory"))

          Spacer(modifier = Modifier.height(16.dp))

          DropdownFields(
              label = stringResource(id = R.string.location_label),
              options = FoodStorageLocation.values(),
              selectedOption = location,
              onOptionSelected = { location = it },
              expanded = locationExpanded,
              onExpandedChange = { locationExpanded = it },
              optionLabel = { fromCapitalStringToLowercaseString(it.name) },
              modifier = Modifier.testTag("inputFoodLocation"))

          OutlinedTextField(
              value = expireDate,
              onValueChange = { expireDate = it },
              label = { Text(stringResource(id = R.string.expire_date_hint)) },
              placeholder = { Text("dd/mm/yyyy") },
              modifier =
                  Modifier.testTag("inputFoodExpireDate").fillMaxWidth().padding(bottom = 16.dp),
          )

          OutlinedTextField(
              value = openDate,
              onValueChange = { openDate = it },
              label = { Text(stringResource(id = R.string.open_date_hint)) },
              placeholder = { Text("dd/mm/yyyy") },
              modifier =
                  Modifier.testTag("inputFoodOpenDate").fillMaxWidth().padding(bottom = 16.dp),
          )

          OutlinedTextField(
              value = buyDate,
              onValueChange = { buyDate = it },
              label = { Text(stringResource(id = R.string.buy_date_hint)) },
              placeholder = { Text("dd/mm/yyyy") },
              modifier =
                  Modifier.testTag("inputFoodBuyDate").fillMaxWidth().padding(bottom = 32.dp),
          )

          Button(
              onClick = {
                errorMessages.clear()

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                try {
                  val expireDateParsed = sdf.parse(expireDate)
                  val openDateParsed = sdf.parse(openDate)
                  val buyDateParsed = sdf.parse(buyDate)

                  // Error if the expiration date is before the open date
                  if (expireDateParsed.before(openDateParsed)) {
                    errorMessages["date"] = "Expiration date cannot be before the open date."
                  }

                  // Error if the buy date is after the open date or expiration date
                  if (buyDateParsed.after(openDateParsed) ||
                      buyDateParsed.after(expireDateParsed)) {
                    errorMessages["buyDate"] =
                        "Buy date cannot be after the open date or expiration date."
                  }
                } catch (e: Exception) {
                  errorMessages["dateFormat"] = "Invalid date format. Please use dd/mm/yyyy."
                }

                // Error is the food name field is empty
                if (foodName.isBlank()) {
                  errorMessages["foodName"] = "Food name cannot be empty."
                }

                // Error is the food amount is blank or not a number
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
                          category = category)
                  val newFoodItem =
                      FoodItem(
                          uid = foodItemViewModel.getUID(),
                          foodFacts = foodFacts,
                          location = location,
                          expiryDate = formatDateToTimestamp(expireDate),
                          openDate = formatDateToTimestamp(openDate),
                          buyDate = formatDateToTimestamp(buyDate),
                          status = FoodStatus.CLOSED)
                  houseHoldViewModel.addFoodItem(newFoodItem)
                  navigationActions.goBack()
                }
              },
              modifier = Modifier.testTag("foodSave").fillMaxWidth().height(50.dp),
              colors = ButtonDefaults.buttonColors(containerColor = primaryContainerLight)) {
                Text(text = "Submit", fontSize = 18.sp)
              }

          ErrorPopUp(
              showDialog = showDialog,
              onDismiss = { showDialog = false },
              errorMessages = errorMessages.values.toList())
        }
  }
}
