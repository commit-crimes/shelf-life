package com.android.shelfLife.ui.newnavigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.shelfLife.model.newhousehold.HouseHold

/**
 * Composable function for the top navigation bar of the app
 *
 * @param houseHold The current household
 * @param onHamburgerClick The lambda to be called when the hamburger icon is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    houseHold: HouseHold,
    onHamburgerClick: () -> Unit = {},
    filters: List<String>,
    selectedFilters: List<String>,
    onFilterChange: (String, Boolean) -> Unit,
    showDeleteOption: Boolean = false,
    onDeleteClick: () -> Unit = {}
) {
  var showFilterBar by remember { mutableStateOf(false) }
  Column {
    TopAppBar(
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer),
        navigationIcon = {
          IconButton(
              modifier = Modifier.testTag("hamburgerIcon"), onClick = { onHamburgerClick() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Icon",
                )
              }
        },
        title = {
          Row(modifier = Modifier.padding(end = 8.dp)) {
            Text(
                text = houseHold.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }
        },
        actions = {
          if (filters.isNotEmpty()) {
            IconButton(
                modifier = Modifier.testTag("filterIcon"),
                onClick = { showFilterBar = !showFilterBar }) {
                  Icon(
                      imageVector = Icons.Default.FilterList,
                      contentDescription = "Filter Icon",
                  )
                }
          }
          if (showDeleteOption) {
            IconButton(
                modifier = Modifier.testTag("deleteFoodItems"), onClick = { onDeleteClick() }) {
                  Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Icon",
                  )
                }
          }
        })

    if (filters.isNotEmpty()) {
      AnimatedVisibility(
          visible = showFilterBar,
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically()) {
            FilterBar(
                filters = filters,
                selectedFilters = selectedFilters,
                onFilterChange = onFilterChange)
          }
    }
  }
}

/**
 * Composable function for the filter bar in the top navigation bar This function displays a
 * horizontal list of filter chips that can be selected by the user.
 */
@Composable
fun FilterBar(
    filters: List<String>,
    selectedFilters: List<String>,
    onFilterChange: (String, Boolean) -> Unit
) {
  val scrollState = rememberScrollState()

  Row(
      modifier =
          Modifier.horizontalScroll(scrollState)
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("filterBar")) {
        filters.forEach { filter ->
          val isSelected = selectedFilters.contains(filter)
          FilterChipItem(
              text = filter,
              isSelected = isSelected,
              onClick = { onFilterChange(filter, !isSelected) })
        }
      }
}

/**
 * Composable function for a filter chip item This function displays a single filter chip that can
 * be selected by the user.
 *
 * @param text The text to display on the filter chip
 * @param isSelected Whether the filter chip is selected or not
 * @param onClick The lambda to be called when the filter chip is clicked
 */
@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
  FilterChip(
      selected = isSelected,
      onClick = onClick,
      label = { Text(text = text) },
      leadingIcon =
          if (isSelected) {
            { Icon(imageVector = Icons.Default.Check, contentDescription = "Selected") }
          } else null,
      colors = FilterChipDefaults.filterChipColors(),
      modifier =
          Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
              .testTag(text) // Add padding between chips
      )
}

/**
 * Composable function for a single household element in the navigation drawer This function
 * displays a single household element in the navigation drawer.
 *
 * @param household The household to display
 * @param selectedHousehold The currently selected household
 * @param onHouseholdSelected The lambda to be called when the household is selected
 */
@Composable
fun HouseHoldElement(
    household: HouseHold,
    selectedHousehold: HouseHold,
    onHouseholdSelected: (HouseHold) -> Unit,
    modifier: Modifier = Modifier,
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
      selected = household == selectedHousehold,
      onClick = { onHouseholdSelected(household) },
      modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
}
