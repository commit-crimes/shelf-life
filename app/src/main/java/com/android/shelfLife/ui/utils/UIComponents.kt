package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    searchBarTestTag: String
) {
  Box(
      modifier = Modifier.fillMaxWidth().padding(16.dp), // Outer padding for spacing
      contentAlignment = Alignment.Center // Center the SearchBar within the Box
      ) {
        androidx.compose.material3.SearchBar(
            colors =
                SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            shadowElevation = 3.dp,
            query = query,
            onQueryChange = onQueryChange,
            placeholder = { Text(placeholder) },
            onSearch = { /* Optional: Handle search action if needed */},
            active = false,
            onActiveChange = {},
            leadingIcon = {},
            trailingIcon = {
              IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = "Search Icon")
              }
            },
            modifier =
                Modifier.widthIn(
                        max = 600.dp) // Restrict max width to prevent over-stretching on large
                    // screens
                    .fillMaxWidth(0.9f) // Make it responsive and occupy 90% of available width
                    .testTag(searchBarTestTag)) {}
      }
}
