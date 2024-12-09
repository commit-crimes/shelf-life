package com.android.shelfLife.ui.newutils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Creates a working dropDown menu in which you can choose from different items
 *
 * @param T: Type
 * @param label: Label of field
 * @param options: Array of elements to be dropped down
 * @param selectedOption: Variable that stores the selected field
 * @param onOptionSelected: Lambda function that describes the behaviour when an element is selected
 * @param expanded: Boolean suggesting if the dropdown is expanded or not
 * @param onExpandedChange: Lambda function that describes the behaviour then the menu changes state
 * @param optionLabel: Option to format how the label is displayed in the UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownFields(
    label: String,
    options: Array<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
  ExposedDropdownMenuBox(
      expanded = expanded, onExpandedChange = onExpandedChange, modifier = modifier) {
        TextField(
            readOnly = true,
            value = optionLabel(selectedOption),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("dropdownMenu_$label"))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
          options.forEach { selectionOption ->
            DropdownMenuItem(
                text = { Text(optionLabel(selectionOption)) },
                onClick = {
                  onOptionSelected(selectionOption)
                  onExpandedChange(false)
                },
                modifier = Modifier.testTag("dropDownItem_${optionLabel(selectionOption)}"))
          }
        }
      }
}
