package com.android.shelfLife.ui.overview

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.overview.FirstTimeWelcomeScreenViewModel
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel

/**
 * Composable function to display the first time welcome screen for the user. Uses Hilt to obtain
 * the [OverviewScreenViewModel].
 *
 * @param navigationActions The actions to navigate between screens.
 * @param overviewScreenViewModel The ViewModel for managing the state of the first time welcome
 *   screen.
 */
@Composable
fun FirstTimeWelcomeScreen(
    navigationActions: NavigationActions,
    overviewScreenViewModel: FirstTimeWelcomeScreenViewModel = hiltViewModel()
) {

  Log.d("FirstTimeWelcomeScreen", "FirstTimeWelcomeScreen")
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
              overviewScreenViewModel.selectHouseholdToEdit(null)
              navigationActions.navigateTo(Screen.HOUSEHOLD_CREATION)
            },
            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp).testTag("householdNameSaveButton"),
            shape = MaterialTheme.shapes.medium) {
              Text(text = "Create Household", style = MaterialTheme.typography.labelLarge)
            }
      }
}
