package com.android.shelfLife.ui.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.signOutUser

/**
 * Composable function to display the first time welcome screen for the user to create a new
 *
 * @param householdViewModel The ViewModel for the households the user has access to
 */
@Composable
fun FirstTimeWelcomeScreen(
    navigationActions: NavigationActions,
    householdViewModel: HouseholdViewModel
) {
  val currentContext = LocalContext.current
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("firstTimeWelcomeScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Welcome Text
        Text(
            text = "Welcome to ShelfLife!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle Text
        Text(
            text = "Get started by creating your Household",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        // Create Household Button
        Button(
            onClick = {
              householdViewModel.selectHouseholdToEdit(null)
              navigationActions.navigateTo(Screen.HOUSEHOLD_CREATION)
            },
            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp).testTag("householdNameSaveButton"),
            shape = MaterialTheme.shapes.medium) {
              Text(text = "Create Household", style = MaterialTheme.typography.labelLarge)
            }

        OutlinedButton(
            onClick = {
              signOutUser(currentContext) {
                navigationActions.navigateToAndClearBackStack(Route.AUTH)
              }
            },
            modifier = Modifier.fillMaxWidth().testTag("logoutButton"),
            border = BorderStroke(1.dp, Color.Red) // Outline color matches the current status
            ) {
              Text(text = "Log out", color = Color.Red)
            }
      }
}
