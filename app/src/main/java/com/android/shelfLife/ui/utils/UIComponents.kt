package com.android.shelfLife.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.theme.onSecondaryDark
import com.android.shelfLife.ui.theme.primaryContainerDark
import com.android.shelfLife.ui.theme.secondaryContainerLight

/**
 * A customizable top app bar with a navigation icon and optional action items.
 *
 * @param onClick Callback for the navigation icon (typically used for "back" navigation).
 * @param title The title text to display in the app bar.
 * @param titleTestTag A test tag identifier for the title text.
 * @param actions A composable lambda to provide custom action buttons/icons in the app bar.
 */
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
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

/**
 * A custom search bar with a placeholder and query handling logic.
 *
 * @param query The current search query text.
 * @param onQueryChange Callback invoked when the query text changes.
 * @param placeholder The placeholder text displayed when the query is empty.
 * @param searchBarTestTag A test tag identifier for the search bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onDeleteTextClicked: () -> Unit,
    placeholder: String,
    searchBarTestTag: String
) {
  Box(
      modifier = Modifier.fillMaxWidth().padding(16.dp), // Outer padding for spacing
      contentAlignment = Alignment.Center // Center the SearchBar within the Box
      ) {
        val colors1 =
            SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            )
        SearchBar(
            inputField = {
              SearchBarDefaults.InputField(
                  modifier = Modifier.testTag("searchBarInputField"),
                  query = query,
                  onQueryChange = onQueryChange,
                  onSearch = {},
                  expanded = false,
                  onExpandedChange = {},
                  enabled = true,
                  placeholder = { Text(placeholder) },
                  leadingIcon = {},
                  trailingIcon = {
                    if (query.isBlank()) {
                      IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                      }
                    } else {
                      IconButton(
                          modifier = Modifier.testTag("deleteTextButton"),
                          onClick = onDeleteTextClicked) {
                            Icon(Icons.Default.Close, contentDescription = "Delete text Icon")
                          }
                    }
                  },
                  colors = colors1.inputFieldColors,
                  interactionSource = null,
              )
            },
            expanded = false,
            onExpandedChange = {},
            modifier =
                Modifier.widthIn(
                        max = 600.dp) // Restrict max width to prevent over-stretching on large
                    // screens
                    .fillMaxWidth(0.9f) // Make it responsive and occupy 90% of available width
                    .testTag(searchBarTestTag),
            shape = SearchBarDefaults.inputFieldShape,
            colors = colors1,
            tonalElevation = SearchBarDefaults.TonalElevation,
            shadowElevation = 3.dp,
            windowInsets = SearchBarDefaults.windowInsets,
            content = {},
        )
      }
}

/**
 * A row of two customizable buttons with separate actions and styles.
 *
 * @param button1OnClick Callback for the first button click.
 * @param button1TestTag A test tag identifier for the first button.
 * @param button1Text The text to display on the first button.
 * @param button2OnClick Callback for the second button click.
 * @param button2TestTag A test tag identifier for the second button.
 * @param button2Text The text to display on the second button.
 */
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

@Composable
fun ExpandableFAB(
    fabExpanded: MutableState<Boolean>,
    navigationActions: NavigationActions,
    firstScreen: String = Screen.GENERATE_RECIPE,
    secondScreen: String = Screen.ADD_RECIPE
) {
  Column(
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)) {
        // Secondary FAB for "Generate" option
        if (fabExpanded.value) {
          ExtendedFloatingActionButton(
              text = { Text("Generate") },
              icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Generate") },
              onClick = {
                // Navigate to Generate Recipe screen
                navigationActions.navigateTo(firstScreen)
              },
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.testTag("generateRecipeFab").width(150.dp))
        }

        // Primary FAB
        ExtendedFloatingActionButton(
            text = { Text(if (fabExpanded.value) "Manual" else "") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
            onClick = {
              if (fabExpanded.value) {
                // Navigate to Add Recipe screen
                navigationActions.navigateTo(secondScreen)
              } else {
                // Expand the FABs
                fabExpanded.value = true
              }
            },
            expanded = fabExpanded.value, // Bind to the state
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier =
                Modifier.testTag("addRecipeFab").width(if (fabExpanded.value) 150.dp else 56.dp))
      }
}
