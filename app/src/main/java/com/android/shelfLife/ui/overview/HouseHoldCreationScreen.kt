package com.android.shelfLife.ui.overview

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.DeletionConfirmationPopUp
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.viewmodel.overview.HouseholdCreationScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseHoldCreationScreen(
    navigationActions: NavigationActions,
    houseHoldRepository: HouseHoldRepository,
    invitationRepository: InvitationRepository,
    userRepository: UserRepository
) {
  val householdCreationScreenViewModel: HouseholdCreationScreenViewModel = viewModel {
    HouseholdCreationScreenViewModel(
        houseHoldRepository = houseHoldRepository,
        invitationRepository = invitationRepository,
        userRepository = userRepository)
  }
  val householdToEdit by householdCreationScreenViewModel.householdToEdit.collectAsState()

  var isError by rememberSaveable { mutableStateOf(false) }
  var houseHoldName by rememberSaveable { mutableStateOf(householdToEdit?.name ?: "") }

  var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

  // Mutable state list to hold member emails
  val memberEmailList by householdCreationScreenViewModel.emailList.collectAsState()
  var emailInput by rememberSaveable { mutableStateOf("") }
  var showEmailTextField by rememberSaveable { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val columnScrollState = rememberScrollState()

  val focusRequester = remember { FocusRequester() }

  // Scroll to the bottom and focus on the email input field when the email input field is shown
  LaunchedEffect(showEmailTextField) {
    if (showEmailTextField) {
      coroutineScope.launch { columnScrollState.animateScrollTo(columnScrollState.maxValue) }
      focusRequester.requestFocus()
    }
  }

  // Function to add email card to the list and scroll to the bottom
  fun addEmailCard() {
    if (emailInput.isNotBlank() && emailInput.trim() !in memberEmailList) {
      householdCreationScreenViewModel.addEmail(emailInput.trim())
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
                          text = "Household name already exists or is empty",
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
                memberEmailList.forEach { email ->
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
                              if (email != FirebaseAuth.getInstance().currentUser?.email ||
                                  householdToEdit != null) {
                                IconButton(
                                    onClick = {
                                      householdCreationScreenViewModel.removeEmail(email)
                                    },
                                    modifier = Modifier.testTag("RemoveEmailButton")) {
                                      Icon(
                                          imageVector = Icons.Default.Delete,
                                          contentDescription = "Remove Email")
                                    }
                              }
                            }
                      }
                }

                if (showEmailTextField) {
                  // Email input field and add button
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(horizontal = 16.dp, vertical = 8.dp)
                              .focusRequester(focusRequester)) {
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
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp).testTag("AddEmailFab"),
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
              CustomButtons(
                  button1OnClick = { navigationActions.goBack() },
                  button1TestTag = "CancelButton",
                  button1Text = stringResource(R.string.cancel_button),
                  button2OnClick = {
                    if (householdCreationScreenViewModel.newHouseholdNameIsInvalid(houseHoldName)) {
                      isError = true
                    } else {
                      if (householdToEdit != null) {
                        val updatedHouseHold = householdToEdit!!.copy(name = houseHoldName)
                        householdCreationScreenViewModel.getUserIdsByEmails(
                            memberEmailList,
                            callback = { emailToUserIds ->
                              if (emailToUserIds.isNotEmpty()) {
                                val oldUidList = updatedHouseHold.members
                                val uidList = memberEmailList.map { emailToUserIds[it]!! }
                                if (oldUidList.size < uidList.size) {
                                  householdCreationScreenViewModel.updateHousehold(
                                      householdToEdit!!.copy(
                                          name = houseHoldName, members = uidList),
                                      false)
                                } else if (oldUidList.size > uidList.size) {
                                  householdCreationScreenViewModel.updateHousehold(
                                      householdToEdit!!.copy(
                                          name = houseHoldName, members = uidList),
                                      true)
                                }
                                householdCreationScreenViewModel.updateHousehold(updatedHouseHold)
                              }
                            })
                      } else {
                        householdCreationScreenViewModel.addNewHousehold(
                            houseHoldName, memberEmailList)
                        Log.d("HouseHoldCreationScreen", "Added new household")
                      }
                      navigationActions.navigateTo(Screen.OVERVIEW)
                    }
                  },
                  button2TestTag = "ConfirmButton",
                  button2Text = stringResource(R.string.save_button))

              // Confirmation Dialog for Deletion
              DeletionConfirmationPopUp(
                  showDeleteDialog = showConfirmationDialog,
                  onDismiss = { showConfirmationDialog = false },
                  onConfirm = {
                    navigationActions.goBack()
                    showConfirmationDialog = false
                  },
                  houseHoldRepository = houseHoldRepository,
                  userRepository = userRepository)
            }
      }
}
