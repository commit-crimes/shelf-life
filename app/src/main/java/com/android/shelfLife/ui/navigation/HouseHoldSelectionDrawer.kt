package com.android.shelfLife.ui.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.utils.DeletionConfirmationPopUp
import com.android.shelfLife.viewmodel.navigation.HouseholdSelectionDrawerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Composable function for the household selection drawer.
 *
 * This function displays a navigation drawer where the user can select, edit, or delete their households.
 * It includes a list of the user's households and options to add a new household or toggle between
 * edit modes. A confirmation dialog is shown when the user attempts to delete a household.
 *
 * @param scope Coroutine scope used to launch coroutines for drawer state management.
 * @param drawerState The state of the navigation drawer, used to manage its open/close state.
 * @param navigationActions Actions for navigating to different screens.
 * @param householdSelectionDrawerViewModel ViewModel for managing the household selection and edit actions.
 * @param content Composable content to display inside the drawer's main area.
 */
@Composable
fun HouseHoldSelectionDrawer(
    scope: CoroutineScope,
    drawerState: DrawerState,
    navigationActions: NavigationActions,
    householdSelectionDrawerViewModel: HouseholdSelectionDrawerViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    // Collect the list of households and the currently selected household
    val userHouseholds by householdSelectionDrawerViewModel.households.collectAsState()
    val selectedHousehold by householdSelectionDrawerViewModel.selectedHousehold.collectAsState()

    // State for managing the edit mode of households
    var editMode by remember { mutableStateOf(false) }

    // State for showing the deletion confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Disable edit mode when the drawer is closed
    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed) {
            editMode = false
        }
    }

    // Handle back button press to close the drawer
    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

    // Main navigation drawer with ModalDrawerSheet for the drawer content
    ModalNavigationDrawer(
        modifier = Modifier.testTag("householdSelectionDrawer"),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                // Scrollable content inside the drawer
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    // Drawer header title
                    Text(
                        "Household selection",
                        modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
                        style = MaterialTheme.typography.labelMedium
                    )

                    // Iterate over the user's households and display them in the drawer
                    userHouseholds.forEachIndexed { index, household ->
                        selectedHousehold?.let {
                            HouseholdDrawerItem(
                                household = household,
                                selectedHousehold = it,
                                editMode = editMode,
                                onHouseholdSelected = { household ->
                                    if (household != selectedHousehold) {
                                        Log.d("HouseHoldSelectionDrawer", "Called selectHousehold")
                                        householdSelectionDrawerViewModel.selectHousehold(household)
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                onHouseholdEditSelected = { household ->
                                    Log.d("HouseHoldSelectionDrawer", "Called selectedHouseholdToEdit")
                                    householdSelectionDrawerViewModel.selectHouseholdToEdit(household)
                                    navigationActions.navigateTo(Screen.HOUSEHOLD_CREATION)
                                },
                                onHouseholdDeleteSelected = { household ->
                                    householdSelectionDrawerViewModel.selectHouseholdToEdit(household)
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.testTag("householdElement_$index"),
                            )
                        }
                    }

                    // Divider between the household list and buttons
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Row with buttons to add a new household or toggle edit mode
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Add Household button
                        IconButton(
                            modifier = Modifier.testTag("addHouseholdIcon"),
                            onClick = {
                                householdSelectionDrawerViewModel.selectHouseholdToEdit(null)
                                navigationActions.navigateTo(Screen.HOUSEHOLD_CREATION)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Household Icon")
                        }

                        // Toggle Edit Mode button
                        IconButton(
                            modifier = Modifier.testTag("editHouseholdIcon"),
                            onClick = { editMode = !editMode }
                        ) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit Household Icon")
                        }
                    }
                }
            }
        },
        content = content
    )

    // Confirmation Dialog for Deletion
    DeletionConfirmationPopUp(
        showDeleteDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = { showDeleteDialog = false }
    )
}