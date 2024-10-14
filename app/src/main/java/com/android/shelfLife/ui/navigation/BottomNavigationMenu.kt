package com.android.shelfLife.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

/**
 * Composable function for the bottom navigation menu.
 *
 * @param onTabSelect The callback to be invoked when a tab is selected.
 * @param tabList The list of tabs to be displayed.
 * @param selectedItem The currently selected tab.
 */
@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  NavigationBar(
      modifier = Modifier.testTag("bottomNavigationMenu"),
      containerColor = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    tabList.forEach { tab ->
      NavigationBarItem(
          modifier = Modifier.testTag(tab.textId),
          icon = { Icon(tab.icon, tab.textId) },
          label = { Text(tab.textId) },
          selected = selectedItem == tab.route,
          onClick = { onTabSelect(tab) },
          colors =
              NavigationBarItemDefaults.colors(
                  indicatorColor =
                      MaterialTheme.colorScheme
                          .secondary, // Using the secondary color for the selected tab indicator
                  selectedIconColor =
                      MaterialTheme.colorScheme.secondaryContainer, // Color for icons when selected
                  selectedTextColor =
                      MaterialTheme.colorScheme.onSurfaceVariant, // Color for text when selected
                  unselectedIconColor =
                      MaterialTheme.colorScheme.onSurfaceVariant, // Color for unselected icons
                  unselectedTextColor =
                      MaterialTheme.colorScheme.onSurfaceVariant // Color for unselected text
                  ))
    }
  }
}

@Preview
@Composable
fun BottomNavigationMenuPreview() {
  BottomNavigationMenu(
      onTabSelect = {},
      tabList =
          listOf(
              TopLevelDestinations.OVERVIEW,
              TopLevelDestinations.SCANNER,
              TopLevelDestinations.RECIPES,
              TopLevelDestinations.PROFILE),
      selectedItem = TopLevelDestinations.OVERVIEW.route)
}
