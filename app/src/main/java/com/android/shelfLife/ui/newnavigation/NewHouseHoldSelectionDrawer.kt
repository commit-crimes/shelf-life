package com.android.shelfLife.ui.newnavigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.overview.HouseholdSelectionDrawerViewModel
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.newutils.DeletionConfirmationPopUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HouseHoldSelectionDrawer(
    scope: CoroutineScope,
    drawerState: DrawerState,
    navigationActions: NavigationActions,
    houseHoldRepository: HouseHoldRepository,
    userRepository: UserRepository,
    content: @Composable () -> Unit
) {
  val householdSelectionDrawerViewModel = viewModel {
    HouseholdSelectionDrawerViewModel(houseHoldRepository, userRepository)
  }
  val userHouseholds by householdSelectionDrawerViewModel.households.collectAsState()
  val selectedHousehold by householdSelectionDrawerViewModel.selectedHousehold.collectAsState()
  var editMode by remember { mutableStateOf(false) }

  // State for confirmation dialog
  var showDeleteDialog by remember { mutableStateOf(false) }

  // Disable edit mode when the drawer is closed
  LaunchedEffect(drawerState.isClosed) {
    if (drawerState.isClosed) {
      editMode = false
    }
  }

  // Close drawer on back button press if it's open
  BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

  ModalNavigationDrawer(
      modifier = Modifier.testTag("householdSelectionDrawer"),
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet(
            drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
          // Remember the scroll state
          val scrollState = rememberScrollState()
          // Wrap content in a Column with verticalScroll
          Column(modifier = Modifier.verticalScroll(scrollState)) {
            Text(
                "Household selection",
                modifier =
                    Modifier.padding(vertical = 18.dp, horizontal = 16.dp)
                        .padding(horizontal = 12.dp),
                style = MaterialTheme.typography.labelMedium)
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                  IconButton(
                      modifier = Modifier.testTag("addHouseholdIcon"),
                      onClick = {
                        householdSelectionDrawerViewModel.selectHouseholdToEdit(null)
                        navigationActions.navigateTo(Screen.HOUSEHOLD_CREATION)
                      }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Household Icon",
                        )
                      }

                  IconButton(
                      modifier = Modifier.testTag("editHouseholdIcon"),
                      onClick = { editMode = !editMode }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Household Icon",
                        )
                      }
                }
          }
        }
      },
      content = content)

  // Confirmation Dialog for Deletion
  DeletionConfirmationPopUp(
      showDeleteDialog = showDeleteDialog,
      onDismiss = { showDeleteDialog = false },
      onConfirm = { showDeleteDialog = false },
      houseHoldRepository = houseHoldRepository,
      userRepository = userRepository)
}
