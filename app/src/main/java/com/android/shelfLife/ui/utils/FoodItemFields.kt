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
import com.android.shelfLife.model.foodItem.FoodStorageLocation

@Composable
fun FoodNameField(
    foodName: String,
    onFoodNameChange: (String) -> Unit,
    foodNameError: String?,
    modifier: Modifier = Modifier
) {
  OutlinedTextField(
      value = foodName,
      onValueChange = onFoodNameChange,
      label = { Text(stringResource(id = R.string.food_name_hint)) },
      isError = foodNameError != null,
      modifier = modifier.fillMaxWidth().testTag("inputFoodName"))
  if (foodNameError != null) {
    Text(
        text = foodNameError,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start)
  }
}

@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    amountError: String?,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
  Column(modifier = modifier) {
    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text(stringResource(id = R.string.amount_hint)) },
        isError = amountError != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag(testTag))
    if (amountError != null) {
      Text(
          text = amountError,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Start)
    }
  }
}

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

@Composable
fun DateField(
    date: String,
    onDateChange: (String) -> Unit,
    dateError: String?,
    labelResId: Int,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
  OutlinedTextField(
      value = date,
      onValueChange = onDateChange,
      label = { Text(stringResource(id = labelResId)) },
      placeholder = { Text("dd/mm/yyyy") },
      isError = dateError != null,
      visualTransformation = DateVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
      modifier = modifier.fillMaxWidth().testTag(testTag))
  if (dateError != null) {
    Text(
        text = dateError,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start)
  }
}
