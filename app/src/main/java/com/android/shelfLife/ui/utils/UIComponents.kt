package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.ui.theme.onSecondaryDark
import com.android.shelfLife.ui.theme.primaryContainerDark
import com.android.shelfLife.ui.theme.secondaryContainerLight

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
          Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = stringResource(R.string.go_back_button_description))
        }
      },
      // title of TopappBar
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
            onSearch = {},
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

@Composable
fun CustomButtons(
    button1OnClick: () -> Unit,
    button1TestTag: String,
    button1Text: String,
    button2OnClick: () -> Unit,
    button2TestTag: String,
    button2Text: String,
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp).fillMaxWidth(),
      horizontalArrangement = Arrangement.Center) {
        // Button 1
        Button(
            onClick = button1OnClick,
            modifier = Modifier.height(50.dp).weight(1f).testTag(button1TestTag),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = secondaryContainerLight, contentColor = onSecondaryDark)) {
              Text(text = button1Text, fontSize = 18.sp)
            }

        Spacer(Modifier.width(24.dp))

        // Button 2
        Button(
            onClick = button2OnClick,
            modifier = Modifier.height(50.dp).weight(1f).testTag(button2TestTag),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = primaryContainerDark,
                    contentColor = secondaryContainerLight)) {
              Text(text = button2Text, fontSize = 18.sp)
            }
      }
}
