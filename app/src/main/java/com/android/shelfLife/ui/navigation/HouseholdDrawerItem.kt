package com.android.shelfLife.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.android.shelfLife.model.household.HouseHold

/**
 * Composable function for a single household drawer item in the navigation drawer This function
 * displays a single household element in the navigation drawer.
 *
 * @param household The household to display
 * @param selectedHousehold The currently selected household
 * @param onHouseholdSelected The lambda to be called when the household is selected
 */
@Composable
fun HouseholdDrawerItem(
    household: HouseHold,
    selectedHousehold: HouseHold,
    editMode: Boolean,
    onHouseholdSelected: (HouseHold) -> Unit,
    onHouseholdEditSelected: (HouseHold) -> Unit,
    modifier: Modifier = Modifier
) {
  NavigationDrawerItem(
      colors =
          NavigationDrawerItemDefaults.colors(
              selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
          ),
      label = {
        Text(
            text = household.name,
            fontWeight = if (household == selectedHousehold) FontWeight.Bold else FontWeight.Normal,
            color =
                if (household == selectedHousehold) MaterialTheme.colorScheme.primary
                else Color.Unspecified)
      },
      icon = {
        if (editMode) {
          IconButton(
              modifier = Modifier.testTag("editHouseholdIndicatorIcon"),
              onClick = { onHouseholdEditSelected(household) }) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Icon",
                )
              }
        }
      },
      selected = household == selectedHousehold,
      onClick = {
        if (editMode) {
          onHouseholdEditSelected(household)
        } else {
          onHouseholdSelected(household)
        }
      },
      modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
}
