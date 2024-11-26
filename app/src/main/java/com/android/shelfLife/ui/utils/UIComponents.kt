package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onClick: () -> Unit,
    title: String,
    titleTestTag: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
  TopAppBar(
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
              navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
              actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer),
      modifier = Modifier.testTag("topBar"),
      navigationIcon = {
        // Back button to return to the previous screen
        IconButton(onClick = onClick, modifier = Modifier.testTag("goBackArrow")) {
          Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back Icon")
        }
      },
      title = {
        Text(
            text = title,
            modifier = Modifier.testTag(titleTestTag),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      },
      actions = actions)
}
