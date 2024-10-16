package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.shelfLife.model.household.HouseholdViewModel

/**
 * Composable function to display the first time welcome screen for the user to create a new
 *
 * @param householdViewModel The ViewModel for the households the user has access to
 */
@Composable
fun FirstTimeWelcomeScreen(householdViewModel: HouseholdViewModel) {
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
            text = "Get started by naming your Household",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        // State for household name
        var householdName by remember { mutableStateOf("") }

        // Text Field for entering household name
        OutlinedTextField(
            value = householdName,
            onValueChange = { newValue -> householdName = newValue },
            label = { Text("Enter Household name") },
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .testTag("householdNameTextField"),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ))

        Spacer(modifier = Modifier.height(24.dp))

        // Create Household Button
        Button(
            onClick = { householdViewModel.addNewHousehold(householdName) },
            enabled = householdName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp).testTag("householdNameSaveButton"),
            shape = MaterialTheme.shapes.medium) {
              Text(text = "Create Household", style = MaterialTheme.typography.labelLarge)
            }
      }
}
