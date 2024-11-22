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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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

  /** Validates all fields when the submit button is clicked. */
  fun validateAllFieldsWhenSubmitButton() {
    buyDateError = validateBuyDate(buyDate)
    expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
    openDateError = validateOpenDate(openDate, buyDate, buyDateError, expireDate, expireDateError)
  }

  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        // Food information
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = foodFacts.name,
                style =
                    TextStyle(
                        fontSize = 20.sp,
                        color = Color(0xFF000000),
                    ))

            Text(
                text = foodFacts.category.name,
                style =
                    TextStyle(
                        fontSize = 13.sp,
                        color = Color(0xFF000000),
                    ))
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
              expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
              // Re-validate Open Date since it depends on Expire Date
              openDateError =
                  validateOpenDate(openDate, buyDate, buyDateError, expireDate, expireDateError)
            },
            dateError = expireDateError,
            labelResId = R.string.expire_date_hint,
            testTag = "expireDateTextField")

        Spacer(modifier = Modifier.height(8.dp))

        // Open Date Field with Error Handling
        DateField(
            date = openDate,
            onDateChange = { newValue ->
              openDate = newValue.filter { it.isDigit() }
              openDateError =
                  validateOpenDate(openDate, buyDate, buyDateError, expireDate, expireDateError)
            },
            dateError = openDateError,
            labelResId = R.string.open_date_hint,
            testTag = "openDateTextField")

        Spacer(modifier = Modifier.height(8.dp))

        // Buy Date Field with Error Handling
        DateField(
            date = buyDate,
            onDateChange = { newValue ->
              buyDate = newValue.filter { it.isDigit() }
              buyDateError = validateBuyDate(buyDate)
              // Re-validate Expire Date and Open Date since they depend on Buy Date
              expireDateError = validateExpireDate(expireDate, buyDate, buyDateError)
              openDateError =
                  validateOpenDate(openDate, buyDate, buyDateError, expireDate, expireDateError)
            },
            dateError = buyDateError,
            labelResId = R.string.buy_date_hint,
            testTag = "buyDateTextField")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                  onClick = { onCancel() },
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.secondary),
                  modifier = Modifier.weight(1f).height(50.dp).testTag("cancelButton")) {
                    Text(text = "Cancel", fontSize = 18.sp)
                  }
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
                      val newFoodItem =
                          FoodItem(
                              uid = foodItemViewModel.getUID(),
                              foodFacts = foodFacts,
                              location = location,
                              expiryDate = expiryTimestamp,
                              openDate = openTimestamp,
                              buyDate = buyTimestamp)

                      Toast.makeText(context, "Food item added", Toast.LENGTH_SHORT).show()
                      onSubmit(newFoodItem)
                    } else {
                      Toast.makeText(
                              context,
                              "Please correct the errors before submitting.",
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                  },
                  modifier = Modifier.weight(1f).height(50.dp).testTag("submitButton")) {
                    Text(text = "Submit", fontSize = 18.sp)
                  }
            }
      }
}
