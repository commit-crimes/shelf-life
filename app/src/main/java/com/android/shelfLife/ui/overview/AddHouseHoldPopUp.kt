package com.android.shelfLife.ui.overview

import HouseholdViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun AddHouseHoldPopUp(showDialog: Boolean, onDismiss: () -> Unit,
                      householdViewModel: HouseholdViewModel) {

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
                androidx.compose.material3.Button(
                    onClick = {
                        householdViewModel.addNewHousehold(newHouseHoldName)
                        onDismiss()
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                androidx.compose.material3.Button(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    }
}