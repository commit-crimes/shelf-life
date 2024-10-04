package com.android.shelfLife.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

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
          onClick = { onTabSelect(tab) })
    }
  }
}
