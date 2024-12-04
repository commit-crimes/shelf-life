package com.android.shelfLife.ui.newutils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.deletionConfirmation.DeletionConfirmationViewModel
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository

@Composable
fun DeletionConfirmationPopUp(
    showDeleteDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    houseHoldRepository: HouseHoldRepository,
    userRepository: UserRepository
) {
  val deletionConfirmationViewModel: DeletionConfirmationViewModel = viewModel {
    DeletionConfirmationViewModel(
        houseHoldRepository = houseHoldRepository, userRepository = userRepository)
  }
  val householdToDelete by deletionConfirmationViewModel.householdToEdit.collectAsState()

  if (showDeleteDialog) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Delete Household") },
        text = { Text("Are you sure you want to delete '${householdToDelete!!.name}'?") },
        modifier = Modifier.testTag("DeleteConfirmationDialog"),
        confirmButton = {
          TextButton(
              onClick = {
                deletionConfirmationViewModel.deleteHouseholdById(householdToDelete!!.uid)
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
