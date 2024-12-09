package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.newFoodItem.FoodStorageLocation

/**
 * Composable function to display a text field for entering the food name.
 *
 * @param foodName The current value of the food name.
 * @param onFoodNameChange Callback function to handle changes to the food name.
 * @param foodNameErrorResId Resource ID for the error message, if any.
 * @param modifier Modifier to be applied to the text field.
 */
@Composable
fun FoodNameField(
    foodName: String,
    onFoodNameChange: (String) -> Unit,
    foodNameErrorResId: Int?,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = foodName,
      onValueChange = onFoodNameChange,
      label = { Text(stringResource(id = R.string.food_name_hint)) },
      isError = foodNameErrorResId != null,
      modifier = modifier.fillMaxWidth().testTag("inputFoodName"))
  if (foodNameErrorResId != null) {
    Text(
        text = stringResource(id = foodNameErrorResId),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start)
  }
}

/**
 * Composable function to display a text field for entering the amount.
 *
 * @param amount The current value of the amount.
 * @param onAmountChange Callback function to handle changes to the amount.
 * @param amountErrorResId Resource ID for the error message, if any.
 * @param modifier Modifier to be applied to the text field.
 * @param testTag Test tag for the text field.
 */
@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    amountErrorResId: Int?,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
  Column(modifier = modifier) {
    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text(stringResource(id = R.string.amount_hint)) },
        isError = amountErrorResId != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag(testTag))
    if (amountErrorResId != null) {
      Text(
          text = stringResource(id = amountErrorResId),
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Start)
    }
  }
}

/**
 * Composable function to display a dropdown menu for selecting the unit.
 *
 * @param unit The currently selected unit.
 * @param onUnitChange Callback function to handle changes to the selected unit.
 * @param unitExpanded Whether the dropdown menu is expanded.
 * @param onUnitExpandedChange Callback function to handle changes to the expanded state.
 * @param modifier Modifier to be applied to the dropdown menu.
 * @param testTag Test tag for the dropdown menu.
 */
@Composable
fun UnitDropdownField(
    unit: FoodUnit,
    onUnitChange: (FoodUnit) -> Unit,
    unitExpanded: Boolean,
    onUnitExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
  DropdownFields(
      label = stringResource(id = R.string.unit_label),
      options = FoodUnit.entries.toTypedArray(),
      selectedOption = unit,
      onOptionSelected = onUnitChange,
      expanded = unitExpanded,
      onExpandedChange = onUnitExpandedChange,
      optionLabel = { fromCapitalStringToLowercaseString(it.name) },
      modifier = modifier.testTag(testTag))
}

/**
 * Composable function to display a dropdown menu for selecting the category.
 *
 * @param category The currently selected category.
 * @param onCategoryChange Callback function to handle changes to the selected category.
 * @param categoryExpanded Whether the dropdown menu is expanded.
 * @param onCategoryExpandedChange Callback function to handle changes to the expanded state.
 * @param modifier Modifier to be applied to the dropdown menu.
 */
@Composable
fun CategoryDropdownField(
    category: FoodCategory,
    onCategoryChange: (FoodCategory) -> Unit,
    categoryExpanded: Boolean,
    onCategoryExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  DropdownFields(
      label = stringResource(id = R.string.category_label),
      options = FoodCategory.entries.toTypedArray(),
      selectedOption = category,
      onOptionSelected = onCategoryChange,
      expanded = categoryExpanded,
      onExpandedChange = onCategoryExpandedChange,
      optionLabel = { fromCapitalStringToLowercaseString(it.name) },
      modifier = modifier.fillMaxWidth().testTag("inputFoodCategory"))
}

/**
 * Composable function to display a dropdown menu for selecting the location.
 *
 * @param location The currently selected location.
 * @param onLocationChange Callback function to handle changes to the selected location.
 * @param locationExpanded Whether the dropdown menu is expanded.
 * @param onExpandedChange Callback function to handle changes to the expanded state.
 * @param modifier Modifier to be applied to the dropdown menu.
 * @param testTag Test tag for the dropdown menu.
 */
@Composable
fun LocationDropdownField(
    location: FoodStorageLocation,
    onLocationChange: (FoodStorageLocation) -> Unit,
    locationExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
) {
  DropdownFields(
      label = stringResource(id = R.string.location_label),
      options = FoodStorageLocation.entries.toTypedArray(),
      selectedOption = location,
      onOptionSelected = onLocationChange,
      expanded = locationExpanded,
      onExpandedChange = onExpandedChange,
      optionLabel = { fromCapitalStringToLowercaseString(it.name) },
      modifier = modifier.testTag(testTag))
}

// TODO Delete this after everything is compatible
@Composable
fun NewLocationDropdownField(
    location: com.android.shelfLife.model.newFoodItem.FoodStorageLocation,
    onLocationChange: (com.android.shelfLife.model.newFoodItem.FoodStorageLocation) -> Unit,
    locationExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
) {
  DropdownFields(
      label = stringResource(id = R.string.location_label),
      options = FoodStorageLocation.entries.toTypedArray(),
      selectedOption = location,
      onOptionSelected = onLocationChange,
      expanded = locationExpanded,
      onExpandedChange = onExpandedChange,
      optionLabel = { fromCapitalStringToLowercaseString(it.name) },
      modifier = modifier.testTag(testTag))
}

/**
 * Composable function to display a text field for entering a date.
 *
 * @param date The current value of the date.
 * @param onDateChange Callback function to handle changes to the date.
 * @param dateErrorResId Resource ID for the error message, if any.
 * @param labelResId Resource ID for the label of the text field.
 * @param modifier Modifier to be applied to the text field.
 * @param testTag Test tag for the text field.
 */
@Composable
fun DateField(
    date: String,
    onDateChange: (String) -> Unit,
    dateErrorResId: Int?,
    labelResId: Int,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
  OutlinedTextField(
      value = date,
      onValueChange = onDateChange,
      label = { Text(stringResource(id = labelResId)) },
      placeholder = { Text(stringResource(id = R.string.date_placeholder)) },
      isError = dateErrorResId != null,
      visualTransformation = DateVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
      modifier = modifier.fillMaxWidth().testTag(testTag))
  if (dateErrorResId != null) {
    Text(
        text = stringResource(id = dateErrorResId),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start)
  }
}
