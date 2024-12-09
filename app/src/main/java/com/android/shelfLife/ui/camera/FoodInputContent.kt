// FoodInputContent.kt

package com.android.shelfLife.ui.camera

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.ui.utils.*
import com.google.firebase.Timestamp

/**
 * Composable function to display the food input content.
 *
 * @param foodFacts The food facts to be displayed.
 * @param onSubmit Callback function to handle the submission of the food item.
 * @param onCancel Callback function to handle the cancellation of the input.
 * @param foodItemViewModel The ViewModel for the food items.
 */
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

  var expireDateErrorResId by remember { mutableStateOf<Int?>(null) }
  var openDateErrorResId by remember { mutableStateOf<Int?>(null) }
  var buyDateErrorResId by remember { mutableStateOf<Int?>(null) }

  var locationExpanded by remember { mutableStateOf(false) }

  /** Validates all fields when the submit button is clicked. */
  fun validateAllFieldsWhenSubmitButton() {
    buyDateErrorResId = validateBuyDate(buyDate)
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        // Food information
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = foodFacts.name, style = TextStyle(fontSize = 20.sp))

            Text(text = foodFacts.category.name, style = TextStyle(fontSize = 13.sp))
          }

          Image(
              painter = painterResource(id = R.drawable.app_logo),
              contentDescription = "Food Image",
              modifier = Modifier.size(60.dp).padding(end = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Dropdown
        LocationDropdownField(
            location = location,
            onLocationChange = { location = it },
            locationExpanded = locationExpanded,
            onExpandedChange = { locationExpanded = it },
            modifier = Modifier,
            testTag = "locationDropdown")

        Spacer(modifier = Modifier.height(16.dp))

        // Expire Date Field with Error Handling
        DateField(
            date = expireDate,
            onDateChange = { newValue ->
              expireDate = newValue.filter { it.isDigit() }
              expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
              // Re-validate Open Date since it depends on Expire Date
              openDateErrorResId =
                  validateOpenDate(
                      openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
            },
            dateErrorResId = expireDateErrorResId,
            labelResId = R.string.expire_date_hint,
            testTag = "expireDateTextField")

        Spacer(modifier = Modifier.height(8.dp))

        // Open Date Field with Error Handling
        DateField(
            date = openDate,
            onDateChange = { newValue ->
              openDate = newValue.filter { it.isDigit() }
              openDateErrorResId =
                  validateOpenDate(
                      openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
            },
            dateErrorResId = openDateErrorResId,
            labelResId = R.string.open_date_hint,
            testTag = "openDateTextField")

        Spacer(modifier = Modifier.height(8.dp))

        // Buy Date Field with Error Handling
        DateField(
            date = buyDate,
            onDateChange = { newValue ->
              buyDate = newValue.filter { it.isDigit() }
              buyDateErrorResId = validateBuyDate(buyDate)
              // Re-validate Expire Date and Open Date since they depend on Buy Date
              expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
              openDateErrorResId =
                  validateOpenDate(
                      openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
            },
            dateErrorResId = buyDateErrorResId,
            labelResId = R.string.buy_date_hint,
            testTag = "buyDateTextField")

        Spacer(modifier = Modifier.height(16.dp))

        CustomButtons(
            button1OnClick = { onCancel() },
            button1TestTag = "cancelButton",
            button1Text = stringResource(R.string.cancel_button),
            button2OnClick = {
              validateAllFieldsWhenSubmitButton()
              val isExpireDateValid = expireDateErrorResId == null && expireDate.isNotEmpty()
              val isOpenDateValid = openDateErrorResId == null
              val isBuyDateValid = buyDateErrorResId == null && buyDate.isNotEmpty()

              val expiryTimestamp = formatDateToTimestamp(expireDate)
              val openTimestamp =
                  if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
              val buyTimestamp = formatDateToTimestamp(buyDate)

              if (isExpireDateValid &&
                  isOpenDateValid &&
                  isBuyDateValid &&
                  expiryTimestamp != null &&
                  buyTimestamp != null) {
                val newFoodItem =
                    FoodItem(
                        uid = foodItemViewModel.getUID(),
                        foodFacts = foodFacts,
                        location = location,
                        expiryDate = expiryTimestamp,
                        openDate = openTimestamp,
                        buyDate = buyTimestamp)

                Toast.makeText(context, R.string.food_added_message, Toast.LENGTH_SHORT).show()
                onSubmit(newFoodItem)
              } else {
                Toast.makeText(context, R.string.submission_error_message, Toast.LENGTH_SHORT)
                    .show()
              }
            },
            button2TestTag = "submitButton",
            button2Text = stringResource(R.string.submit_button_text),
        )
      }
}
