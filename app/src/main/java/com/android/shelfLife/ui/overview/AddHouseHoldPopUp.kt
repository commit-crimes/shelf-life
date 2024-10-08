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
import com.android.shelfLife.model.household.HouseholdViewModel

@Composable
fun AddHouseHoldPopUp(showDialog: Boolean, onDismiss: () -> Unit,
                      householdViewModel: HouseholdViewModel
) {

    var newHouseHoldName by remember { mutableStateOf("") }

    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { onDismiss()},
            title = { Text("Add New Household") },
            text = {
                Column {
                    Text(text = "Enter the name of the new household:")
                    TextField(
                        value = newHouseHoldName,
                        onValueChange = { newHouseHoldName = it },
                        placeholder = { Text("Household Name") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        householdViewModel.addNewHousehold(newHouseHoldName)
                        onDismiss()
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    }
}