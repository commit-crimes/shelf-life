package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Creates a working dropDown menu in which you can choose from different items.
 *
 * @param T The type of the items in the dropdown menu.
 * @param label The label of the dropdown field.
 * @param options An array of elements to be displayed in the dropdown menu.
 * @param selectedOption The currently selected option.
 * @param onOptionSelected A lambda function that describes the behavior when an element is
 *   selected.
 * @param expanded A boolean indicating if the dropdown is expanded or not.
 * @param onExpandedChange A lambda function that describes the behavior when the menu changes
 *   state.
 * @param optionLabel A function to format how the label is displayed in the UI.
 * @param modifier A modifier for the dropdown field.
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
            modifier =
                Modifier.fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .testTag("dropdownMenu_$label"))
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
