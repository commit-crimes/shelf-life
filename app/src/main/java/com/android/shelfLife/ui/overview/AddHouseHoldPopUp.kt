package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.shelfLife.model.household.HouseholdViewModel

/**
 * Composable function for the pop-up dialog to add a new household
 *
 * @param showDialog A boolean to determine if the dialog should be shown
 * @param onDismiss The lambda to be called when the dialog is dismissed
 * @param householdViewModel The view model for the household
 */
@Composable
fun AddHouseHoldPopUp(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    householdViewModel: HouseholdViewModel
) {

  var newHouseHoldName by remember { mutableStateOf("") }

  if (showDialog) {
    androidx.compose.material3.AlertDialog(
        modifier = Modifier.testTag("addHouseholdPopup"),
        onDismissRequest = { onDismiss() },
        title = { Text("Add New Household") },
        text = {
          Column {
            Text(text = "Enter the name of the new household:")
            TextField(
                value = newHouseHoldName,
                onValueChange = { newHouseHoldName = it },
                placeholder = { Text("Household Name") })
          }
        },
        confirmButton = {
          Button(
              onClick = {
                householdViewModel.addNewHousehold(newHouseHoldName)
                onDismiss()
              }) {
                Text("Add")
              }
        },
        dismissButton = { Button(onClick = { onDismiss() }) { Text("Cancel") } })
  }
}
