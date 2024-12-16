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
 * A composable function that displays a confirmation dialog for deleting a household.
 *
 * The dialog prompts the user to confirm their action and provides options to either confirm or
 * cancel the deletion. The household name is displayed dynamically based on the current selection.
 *
 * @param showDeleteDialog A boolean indicating whether the dialog should be visible.
 * @param onConfirm A lambda function triggered when the user confirms the deletion.
 * @param onDismiss A lambda function triggered when the user cancels or dismisses the dialog.
 * @param deletionConfirmationViewModel The ViewModel used to manage household deletion state.
 */
@Composable
fun DeletionConfirmationPopUp(
    showDeleteDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    deletionConfirmationViewModel: DeletionConfirmationViewModel = hiltViewModel()
) {
    // Observe the household to delete from the ViewModel
    val householdToDelete by deletionConfirmationViewModel.householdToEdit.collectAsState()

    // Show the dialog only if `showDeleteDialog` is true
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() }, // Triggered when the user taps outside the dialog
            title = { Text("Delete Household") }, // Title of the dialog
            text = {
                // Display the name of the household to delete dynamically
                Text("Are you sure you want to delete '${householdToDelete!!.name}'?")
            },
            modifier = Modifier.testTag("DeleteConfirmationDialog"), // Tag for testing
            confirmButton = {
                // Button to confirm the deletion
                TextButton(
                    onClick = {
                        // Trigger the ViewModel's delete function
                        deletionConfirmationViewModel.deleteHouseholdById(householdToDelete!!.uid)
                        onConfirm() // Notify the parent composable of the confirmation
                    },
                    modifier = Modifier.testTag("confirmDeleteHouseholdButton")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                // Button to cancel or dismiss the dialog
                TextButton(
                    onClick = { onDismiss() }, // Trigger the dismissal callback
                    modifier = Modifier.testTag("cancelDeleteHouseholdButton")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}