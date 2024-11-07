package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseHoldCreationScreen(
    navigationActions: NavigationActions,
    householdViewModel: HouseholdViewModel,
) {
  val householdToEdit = householdViewModel.householdToEdit.collectAsState().value
  var isError by remember { mutableStateOf(false) }
  var houseHoldName by rememberSaveable { mutableStateOf(householdToEdit?.name ?: "") }

  var showConfirmationDialog by remember { mutableStateOf(false) }

  // New state variables for email input
  var emailInput by rememberSaveable { mutableStateOf("") }
  var friendEmails by rememberSaveable { mutableStateOf(mutableListOf<String>()) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("") },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag("CloseButton"),
                  onClick = { navigationActions.goBack() }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                  }
            },
            actions = {
              if (householdToEdit != null) {
                IconButton(
                    modifier = Modifier.testTag("DeleteButton"),
                    onClick = { showConfirmationDialog = true }) {
                      Icon(
                          Icons.Outlined.Delete,
                          contentDescription = "Delete",
                          tint = MaterialTheme.colorScheme.error)
                    }
              }
            })
      },
      modifier = Modifier.testTag("HouseHoldCreationScreen")) { paddingValues ->
        Column(
            modifier =
                Modifier.padding(paddingValues).padding(30.dp).padding(top = 50.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Existing household name input
              OutlinedTextField(
                  value = houseHoldName,
                  onValueChange = { houseHoldName = it },
                  textStyle = TextStyle(fontSize = 30.sp, textAlign = TextAlign.Start),
                  singleLine = true,
                  isError = isError,
                  supportingText = {
                    if (isError) {
                      Text(
                          modifier = Modifier.fillMaxWidth(),
                          text = "Household name already exists",
                          color = MaterialTheme.colorScheme.error)
                    }
                  },
                  label = { Text("Household Name") },
                  placeholder = { Text("Enter Household Name") },
                  modifier =
                      Modifier.padding(30.dp).fillMaxWidth().testTag("HouseHoldNameTextField"))

              // Section for household members
              Text(
                  "Household Members",
                  style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                  textAlign = TextAlign.Start,
                  modifier =
                      Modifier.fillMaxWidth().padding(top = 20.dp).testTag("HouseHoldMembersText"))

              // Email input field and add button
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Friend's Email") },
                        placeholder = { Text("Enter email") },
                        modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                          if (emailInput.isNotBlank()) {
                            friendEmails.add(emailInput.trim())
                            emailInput = ""
                          }
                        },
                        modifier = Modifier.testTag("AddEmailButton")) {
                          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Email")
                        }
                  }

              // Display list of added emails
              LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)) {
                items(friendEmails) { email ->
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(
                            text = email,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontSize = 16.sp))
                        IconButton(
                            onClick = { friendEmails.remove(email) },
                            modifier = Modifier.testTag("RemoveEmailButton")) {
                              Icon(
                                  imageVector = Icons.Default.Delete,
                                  contentDescription = "Remove Email")
                            }
                      }
                }
              }

              // Spacer to push buttons to the bottom
              Spacer(modifier = Modifier.weight(1f))

              // Existing buttons
              Row(
                  modifier =
                      Modifier.fillMaxWidth().padding(bottom = 60.dp, start = 45.dp, end = 45.dp),
                  verticalAlignment = Alignment.Bottom,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        modifier = Modifier.testTag("ConfirmButton"),
                        onClick = {
                          // TODO: Update this logic to include multiple members
                          if (householdViewModel.checkIfHouseholdNameExists(houseHoldName) &&
                              (householdToEdit == null || houseHoldName != householdToEdit.name)) {
                            isError = true
                          } else {
                            if (householdToEdit != null) {
                              val updatedHouseHold = householdToEdit.copy(name = houseHoldName)
                              householdViewModel.updateHousehold(updatedHouseHold)
                            } else {
                              householdViewModel.addNewHousehold(houseHoldName, friendEmails)
                            }
                            navigationActions.goBack()
                          }
                        },
                    ) {
                      Text(
                          "Confirm",
                          style = TextStyle(fontSize = 22.sp),
                          modifier = Modifier.padding(10.dp))
                    }
                    Button(
                        modifier = Modifier.testTag("CancelButton"),
                        onClick = { navigationActions.goBack() },
                    ) {
                      Text(
                          "Cancel",
                          style = TextStyle(fontSize = 22.sp),
                          modifier = Modifier.padding(10.dp))
                    }
                  }

              // Existing confirmation dialog
              if (showConfirmationDialog) {
                AlertDialog(
                    modifier = Modifier.testTag("DeleteConfirmationDialog"),
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text("Delete household") },
                    text = { Text("Are you sure?") },
                    confirmButton = {
                      TextButton(
                          modifier = Modifier.testTag("ConfirmDeleteButton"),
                          onClick = {
                            if (householdToEdit != null) {
                              householdViewModel.deleteHouseholdById(householdToEdit.uid)
                            }
                            navigationActions.goBack()
                            showConfirmationDialog = false
                          }) {
                            Text("Confirm")
                          }
                    },
                    dismissButton = {
                      TextButton(
                          modifier = Modifier.testTag("CancelDeleteButton"),
                          onClick = { showConfirmationDialog = false }) {
                            Text("Cancel")
                          }
                    },
                )
              }
            }
      }
}
