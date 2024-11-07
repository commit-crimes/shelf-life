package com.android.shelfLife.ui.camera

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/** Composable function for the bottom sheet content with date input fields and error handling. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodInputContent(
    foodFacts: FoodFacts,
    onSubmit: (FoodItem) -> Unit,
    onCancel: () -> Unit,
    foodItemViewModel: ListFoodItemsViewModel,
    householdViewModel: HouseholdViewModel,
    isExpanded: Boolean
) {
  val context = LocalContext.current
  var location by remember { mutableStateOf(FoodStorageLocation.PANTRY) }
  var expireDate by remember { mutableStateOf("") }
  var openDate by remember { mutableStateOf("") }
  var buyDate by remember { mutableStateOf(formatTimestampToDate(Timestamp.now())) }

  var expireDateError by remember { mutableStateOf<String?>(null) }
  var openDateError by remember { mutableStateOf<String?>(null) }
  var buyDateError by remember { mutableStateOf<String?>(null) }

  var locationExpanded by remember { mutableStateOf(false) }

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
              modifier = Modifier.size(30.dp).padding(end = 8.dp))
        }

      if (isExpanded) {
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
                  modifier = Modifier.fillMaxWidth().menuAnchor().testTag("locationTextField")
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
            modifier = Modifier.fillMaxWidth().testTag("expireDateTextField"))
        if (expireDateError != null) {
          Text(
              text = expireDateError!!,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.align(Alignment.Start))
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
                  buyDate.length == 8) {
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
            modifier = Modifier.fillMaxWidth().testTag("openDateTextField"))
        if (openDateError != null) {
          Text(
              text = openDateError!!,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.align(Alignment.Start))
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
                  buyDate.length == 8) {
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
            modifier = Modifier.fillMaxWidth().testTag("buyDateTextField"))
        if (buyDateError != null) {
          Text(
              text = buyDateError!!,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                  buyTimestamp != null) {
                val newFoodItem =
                    FoodItem(
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
                        context, "Please correct the errors before submitting.", Toast.LENGTH_SHORT)
                    .show()
              }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submitButton")) {
              Text(text = "Submit", fontSize = 18.sp)
            }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onCancel() },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("cancelButton")) {
              Text(text = "Cancel", fontSize = 18.sp)
            }
      }
}

// Custom VisualTransformation with proper OffsetMapping
class DateVisualTransformation : VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    // Remove any non-digit characters
    val digits = text.text.filter { it.isDigit() }

    // Build the formatted text with slashes
    val formattedText = buildString {
      for (i in digits.indices) {
        append(digits[i])
        if ((i == 1 || i == 3) && i != digits.lastIndex) {
          append('/')
        }
      }
    }

    // Create an OffsetMapping for the cursor position
    val offsetMapping =
        object : OffsetMapping {
          override fun originalToTransformed(offset: Int): Int {
            var transformedOffset = offset
            if (offset > 2) transformedOffset++
            if (offset > 4) transformedOffset++
            return transformedOffset.coerceAtMost(formattedText.length)
          }

          override fun transformedToOriginal(offset: Int): Int {
            var originalOffset = offset
            if (offset > 2) originalOffset--
            if (offset > 5) originalOffset--
            return originalOffset.coerceAtMost(digits.length)
          }
        }

    return TransformedText(AnnotatedString(formattedText), offsetMapping)
  }
}

// Helper function to get error message for date input
fun getDateErrorMessage(dateStr: String, isRequired: Boolean = true): String? {
  if (dateStr.isEmpty()) {
    return if (isRequired) "Date cannot be empty" else null
  }
  if (dateStr.length != 8) {
    return "Incomplete date"
  }
  val formattedDateStr = insertSlashes(dateStr)
  return if (isValidDate(formattedDateStr)) null else "Invalid date"
}

// Function to insert slashes into the date string
fun insertSlashes(input: String): String {
  // Input is expected to be up to 8 digits
  val sb = StringBuilder()
  val digits = input.take(8) // Ensure no more than 8 digits
  for (i in digits.indices) {
    sb.append(digits[i])
    if ((i == 1 || i == 3) && i != digits.lastIndex) {
      sb.append('/')
    }
  }
  return sb.toString()
}

// Function to validate date in dd/MM/yyyy format without using exceptions
fun isValidDate(dateStr: String): Boolean {
  // Check if the dateStr matches the pattern dd/MM/yyyy
  val datePattern = Regex("""\d{2}/\d{2}/\d{4}""")
  if (!datePattern.matches(dateStr)) {
    return false
  }

  val parts = dateStr.split("/")
  val day = parts[0].toIntOrNull() ?: return false
  val month = parts[1].toIntOrNull() ?: return false
  val year = parts[2].toIntOrNull() ?: return false

  // Check if month is valid
  if (month !in 1..12) {
    return false
  }

  // Check if day is valid for the given month
  val daysInMonth =
      when (month) {
        4,
        6,
        9,
        11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
      }

  if (day !in 1..daysInMonth) {
    return false
  }

  // Additional checks can be added (e.g., year range)
  return true
}

// Helper function to check if a year is a leap year
fun isLeapYear(year: Int): Boolean {
  return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))
}

// Function to compare two dates (returns true if date1 >= date2)
fun isDateAfterOrEqual(dateStr1: String, dateStr2: String): Boolean {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val date1 = sdf.parse(insertSlashes(dateStr1)) ?: return false
  val date2 = sdf.parse(insertSlashes(dateStr2)) ?: return false
  return !date1.before(date2)
}

// Function to convert a string date to Timestamp, handling exceptions
fun formatDateToTimestamp(dateString: String): Timestamp? {
  return try {
    val formattedDateStr = insertSlashes(dateString)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = sdf.parse(formattedDateStr)
    if (date != null) Timestamp(date) else null
  } catch (e: Exception) {
    null
  }
}

// Function to format a Timestamp to a date string (stored as digits without slashes)
fun formatTimestampToDate(timestamp: Timestamp): String {
  val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
  return sdf.format(timestamp.toDate())
}
