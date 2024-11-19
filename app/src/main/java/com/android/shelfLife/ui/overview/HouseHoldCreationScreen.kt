package com.android.shelfLife.ui.overview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.android.shelfLife.ui.utils.DeletionConfirmationPopUp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseHoldCreationScreen(
    navigationActions: NavigationActions,
    householdViewModel: HouseholdViewModel,
) {
  val coroutineScope = rememberCoroutineScope()
  val householdToEdit by householdViewModel.householdToEdit.collectAsState()
  val memberEmails by householdViewModel.memberEmails.collectAsState()

  var isError by remember { mutableStateOf(false) }
  var houseHoldName by rememberSaveable { mutableStateOf(householdToEdit?.name ?: "") }

  var showConfirmationDialog by remember { mutableStateOf(false) }

  // Mutable state list to hold member emails
  val memberEmailList = remember { mutableStateListOf<String>() }
  var emailInput by rememberSaveable { mutableStateOf("") }
  var showEmailTextField by remember { mutableStateOf(false) }

  val columnScrollState = rememberScrollState()

  // Initialize memberEmailList when memberEmails are fetched
  LaunchedEffect(memberEmails) {
    memberEmailList.clear()
    memberEmailList.addAll(memberEmails.values)
  }

  LaunchedEffect(showEmailTextField) {
    if (showEmailTextField) {
      coroutineScope.launch { columnScrollState.animateScrollTo(columnScrollState.maxValue) }
    }
  }

  // Fetch member emails when the screen is opened for editing
  LaunchedEffect(householdToEdit) {
    householdToEdit?.let { householdViewModel.selectHouseholdToEdit(it) }
  }

  // Function to add email card to the list and scroll to the bottom
  fun addEmailCard() {
    if (emailInput.isNotBlank()) {
      // TODO check if email is valid
      memberEmailList.add(emailInput.trim())
      emailInput = ""
    }
    showEmailTextField = false
    coroutineScope.launch { columnScrollState.scrollTo(columnScrollState.maxValue) }
  }

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
              Text(
                  "Household Members",
                  style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                  textAlign = TextAlign.Start,
                  modifier =
                      Modifier.fillMaxWidth().padding(top = 20.dp).testTag("HouseHoldMembersText"))

              // Display the list of member emails
              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(top = 20.dp)
                          .verticalScroll(columnScrollState)
                          .weight(1f),
              ) {
                memberEmailList.forEachIndexed { index, email ->
                  ElevatedCard(
                      elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(horizontal = 16.dp, vertical = 8.dp)
                              .background(MaterialTheme.colorScheme.background)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)) {
                              Text(
                                  text = email,
                                  style = TextStyle(fontSize = 16.sp),
                                  modifier = Modifier.weight(1f))
                              IconButton(
                                  onClick = { memberEmailList.removeAt(index) },
                                  modifier = Modifier.testTag("RemoveEmailButton")) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Email")
                                  }
                            }
                      }
                }

                if (showEmailTextField) {
                  // Email input field and add button
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier =
                          Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Friend's Email") },
                            placeholder = { Text("Enter email") },
                            singleLine = true,
                            keyboardActions = KeyboardActions(onDone = { addEmailCard() }),
                            modifier = Modifier.weight(1f).testTag("EmailInputField"))
                        IconButton(
                            onClick = { addEmailCard() },
                            modifier = Modifier.testTag("AddEmailButton")) {
                              Icon(
                                  imageVector = Icons.Default.Check,
                                  contentDescription = "Add Email")
                            }
                      }
                }

                FloatingActionButton(
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                    onClick = { showEmailTextField = !showEmailTextField },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                  Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = "Add Email",
                      tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
              }

              // Confirm and Cancel buttons
              Row(
                  modifier = Modifier.fillMaxWidth().padding(top = 25.dp, bottom = 60.dp),
                  verticalAlignment = Alignment.Bottom,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        modifier = Modifier.testTag("ConfirmButton"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        onClick = {
                          if (householdViewModel.checkIfHouseholdNameExists(houseHoldName) &&
                              (householdToEdit == null ||
                                  houseHoldName != householdToEdit!!.name)) {
                            isError = true
                          } else {
                            coroutineScope.launch {
                              householdViewModel.getUserIdsByEmails(memberEmailList) { emailToUid ->
                                val missingEmails =
                                    memberEmailList.filter { it !in emailToUid.keys }
                                if (missingEmails.isNotEmpty()) {
                                  Log.w(
                                      "HouseHoldCreationScreen", "Emails not found: $missingEmails")
                                }
                                val memberUids = emailToUid.values.toMutableList()

                                if (householdToEdit != null) {
                                  val updatedHouseHold =
                                      householdToEdit!!.copy(
                                          name = houseHoldName, members = memberUids)
                                  householdViewModel.updateHousehold(updatedHouseHold)
                                } else {
                                  householdViewModel.addNewHousehold(houseHoldName, memberEmailList)
                                }
                                navigationActions.goBack()
                              }
                            }
                          }
                        },
                    ) {
                      Text(
                          "Save",
                          style =
                              TextStyle(
                                  fontSize = 20.sp,
                                  textAlign = TextAlign.Center,
                                  color = MaterialTheme.colorScheme.onSecondaryContainer),
                          modifier = Modifier.padding(7.dp).width(70.dp))
                    }
                    Button(
                        modifier = Modifier.testTag("CancelButton"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        onClick = { navigationActions.goBack() },
                    ) {
                      Text(
                          "Cancel",
                          style =
                              TextStyle(
                                  fontSize = 20.sp,
                                  textAlign = TextAlign.Center,
                                  color = MaterialTheme.colorScheme.onSecondaryContainer),
                          modifier = Modifier.padding(7.dp).width(70.dp))
                    }
                  }

              // Confirmation Dialog for Deletion
              DeletionConfirmationPopUp(
                  showDeleteDialog = showConfirmationDialog,
                  onDismiss = { showConfirmationDialog = false },
                  onConfirm = {
                    navigationActions.goBack()
                    showConfirmationDialog = false
                  },
                  householdViewModel = householdViewModel)
            }
      }
}
