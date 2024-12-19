package com.android.shelfLife.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.shelfLife.model.household.HouseHold

/**
 * Composable function for a single household drawer item in the navigation drawer. This function
 * displays a single household element in the navigation drawer.
 *
 * @param household The household to display.
 * @param selectedHousehold The currently selected household.
 * @param editMode Boolean indicating if the edit mode is enabled.
 * @param onHouseholdSelected The lambda to be called when the household is selected.
 * @param onHouseholdEditSelected The lambda to be called when the edit button is clicked.
 * @param onHouseholdDeleteSelected The lambda to be called when the delete button is clicked.
 * @param modifier The modifier to be applied to the item.
 */
@Composable
fun HouseholdDrawerItem(
    household: HouseHold,
    selectedHousehold: HouseHold,
    editMode: Boolean,
    onHouseholdSelected: (HouseHold) -> Unit,
    onHouseholdEditSelected: (HouseHold) -> Unit,
    onHouseholdDeleteSelected: (HouseHold) -> Unit, // New callback for delete action
    modifier: Modifier = Modifier
) {
  NavigationDrawerItem(
      colors =
          NavigationDrawerItemDefaults.colors(
              selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
          ),
      label = {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          if (editMode) {
            // Edit button on the left
            IconButton(
                modifier = Modifier.testTag("editHouseholdIndicatorIcon"),
                onClick = { onHouseholdEditSelected(household) }) {
                  Icon(
                      imageVector = Icons.Outlined.Edit,
                      contentDescription = "Edit Icon",
                  )
                }
          }

          Text(
              text = household.name,
              fontWeight =
                  if (household == selectedHousehold) FontWeight.Bold else FontWeight.Normal,
              color =
                  if (household == selectedHousehold) MaterialTheme.colorScheme.primary
                  else Color.Unspecified,
              modifier = Modifier.weight(1f).padding(start = if (editMode) 8.dp else 0.dp))
          if (editMode) {
            // Delete button on the right
            IconButton(
                modifier = Modifier.testTag("deleteHouseholdIcon"),
                onClick = { onHouseholdDeleteSelected(household) }) {
                  Icon(
                      imageVector = Icons.Outlined.Delete,
                      contentDescription = "Delete Icon",
                      tint = MaterialTheme.colorScheme.error)
                }
          }
        }
      },
      selected = household == selectedHousehold,
      onClick = {
        if (!editMode) {
          onHouseholdSelected(household)
        }
      },
      modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
}
