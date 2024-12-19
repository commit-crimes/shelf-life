package com.android.shelfLife.ui.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.viewmodel.utils.DeletionConfirmationViewModel

/**
 * Composable function to display a deletion confirmation pop-up dialog.
 *
 * This dialog asks the user to confirm the deletion of a household. It shows the name of the household
 * to be deleted and provides 'Delete' and 'Cancel' buttons.
 *
 * @param showDeleteDialog A boolean indicating whether the dialog should be shown.
 * @param onConfirm A lambda function to be called when the user confirms the deletion.
 * @param onDismiss A lambda function to be called when the user dismisses the dialog.
 * @param deletionConfirmationViewModel The ViewModel for managing the state of the deletion confirmation.
 */
@Composable
fun DeletionConfirmationPopUp(
    showDeleteDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    deletionConfirmationViewModel: DeletionConfirmationViewModel = hiltViewModel()
) {

    // Collect the household to delete from the ViewModel
    val householdToDelete by deletionConfirmationViewModel.householdToEdit.collectAsState()

    // Show the dialog if showDeleteDialog is true
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