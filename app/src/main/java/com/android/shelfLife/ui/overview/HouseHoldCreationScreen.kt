package com.android.shelfLife.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.DeletionConfirmationPopUp
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.viewmodel.overview.HouseholdCreationScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * Composable function to handle the creation and editing of a household.
         *
         * This screen allows users to create a new household or edit an existing one. Users can provide
         * the household name, add or remove household members by email, and delete the household.
         * It includes a scrollable list of members and actions to confirm or cancel the changes.
         *
         * @param navigationActions The navigation actions to be used for screen transitions.
         * @param viewModel The [HouseholdCreationScreenViewModel] that handles data and actions related to household creation and modification.
         */
fun HouseHoldCreationScreen(
    navigationActions: NavigationActions,
    viewModel: HouseholdCreationScreenViewModel = hiltViewModel()
) {
    val householdToEdit by viewModel.householdToEdit.collectAsState() // Get the household data to edit

    // State variables for handling errors, email input, and showing confirmation dialog
    var isError by rememberSaveable { mutableStateOf(false) }
    var houseHoldName by rememberSaveable { mutableStateOf(householdToEdit?.name ?: "") }

    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    val memberEmailList by viewModel.emailList.collectAsState() // Get the list of email addresses of household members
    var emailInput by rememberSaveable { mutableStateOf("") } // State for the email input field
    var showEmailTextField by rememberSaveable { mutableStateOf(false) } // State to show/hide the email input field

    val coroutineScope = rememberCoroutineScope() // Coroutine scope for handling asynchronous tasks
    val columnScrollState = rememberScrollState() // State for the scroll position of the column
    val focusRequester = remember { FocusRequester() } // FocusRequester for managing focus on the email input field

    // Automatically scroll and request focus when the email input field is shown
    LaunchedEffect(showEmailTextField) {
        if (showEmailTextField) {
            coroutineScope.launch { columnScrollState.animateScrollTo(columnScrollState.maxValue) }
            focusRequester.requestFocus()
        }
    }

    // Function to add an email card to the list of members
    fun addEmailCard() {
        val added = viewModel.tryAddEmailCard(emailInput)
        if (added) {
            emailInput = "" // Clear the input field after adding
        }
        showEmailTextField = false // Hide the input field
        coroutineScope.launch { columnScrollState.scrollTo(columnScrollState.maxValue) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag("CloseButton"),
                        onClick = { navigationActions.goBack() }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    // Show delete icon only if editing an existing household
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
                }
            )
        },
        modifier = Modifier.testTag("HouseHoldCreationScreen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(30.dp)
                .padding(top = 50.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Household Name input field
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
                modifier = Modifier
                    .padding(30.dp)
                    .fillMaxWidth()
                    .testTag("HouseHoldNameTextField")
            )

            // Label for household members
            Text(
                "Household Members",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp).testTag("HouseHoldMembersText")
            )

            // List of members with email input field
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .verticalScroll(columnScrollState)
                    .weight(1f)
            ) {
                // Render email cards for each member
                memberEmailList.forEach { email ->
                    ElevatedCard(
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = email,
                                style = TextStyle(fontSize = 16.sp),
                                modifier = Modifier.weight(1f)
                            )
                            // Remove email icon, unless it's the current user's email
                            if (email != FirebaseAuth.getInstance().currentUser?.email || householdToEdit != null) {
                                IconButton(
                                    onClick = { viewModel.removeEmail(email) },
                                    modifier = Modifier.testTag("RemoveEmailButton")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Email"
                                    )
                                }
                            }
                        }
                    }
                }

                // Show email input field for adding new members
                if (showEmailTextField) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .focusRequester(focusRequester)
                    ) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Friend's Email") },
                            placeholder = { Text("Enter email") },
                            singleLine = true,
                            keyboardActions = KeyboardActions(onDone = { addEmailCard() }),
                            modifier = Modifier.weight(1f).testTag("EmailInputField")
                        )
                        IconButton(
                            onClick = { addEmailCard() },
                            modifier = Modifier.testTag("AddEmailButton")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Add Email"
                            )
                        }
                    }
                }

                // Button to toggle email input visibility
                FloatingActionButton(
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp)
                        .testTag("AddEmailFab"),
                    onClick = { showEmailTextField = !showEmailTextField },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Email",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Buttons for canceling or saving the household
            CustomButtons(
                button1OnClick = { navigationActions.goBack() },
                button1TestTag = "CancelButton",
                button1Text = stringResource(R.string.cancel_button),
                button2OnClick = {
                    coroutineScope.launch {
                        val success = viewModel.confirmHouseholdActions(houseHoldName)
                        if (!success) {
                            isError = true
                        } else {
                            navigationActions.navigateTo(Screen.OVERVIEW)
                        }
                    }
                },
                button2TestTag = "ConfirmButton",
                button2Text = stringResource(R.string.save_button)
            )

            // Deletion confirmation dialog
            DeletionConfirmationPopUp(
                showDeleteDialog = showConfirmationDialog,
                onDismiss = { showConfirmationDialog = false },
                onConfirm = {
                    navigationActions.goBack()
                    showConfirmationDialog = false
                }
            )
        }
    }
}