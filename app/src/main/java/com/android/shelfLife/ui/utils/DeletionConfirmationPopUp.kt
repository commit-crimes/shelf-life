package com.android.shelfLife.ui.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.shelfLife.model.household.HouseholdViewModel

@Composable
fun DeletionConfirmationPopUp(
    showDeleteDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    householdViewModel: HouseholdViewModel
) {
  val householdToDelete = householdViewModel.householdToEdit.collectAsState().value
  val selectedHousehold = householdViewModel.selectedHousehold.collectAsState().value

  if (showDeleteDialog) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Delete Household") },
        text = { Text("Are you sure you want to delete '${householdToDelete!!.name}'?") },
        confirmButton = {
          TextButton(
              onClick = {
                householdViewModel.deleteHouseholdById(householdToDelete!!.uid)
                if (householdToDelete == selectedHousehold) {
                  // If the deleted household was selected, deselect it
                  householdViewModel.selectHousehold(null)
                }
                onConfirm()
              },
              modifier = Modifier.testTag("confirmDeleteHouseholdButton")) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
              }
        },
        dismissButton = {
          TextButton(
              onClick = { onDismiss() },
              modifier = Modifier.testTag("cancelDeleteHouseholdButton")) {
                Text("Cancel")
              }
        })
  }
}
