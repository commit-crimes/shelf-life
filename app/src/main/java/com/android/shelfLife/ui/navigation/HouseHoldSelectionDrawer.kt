package com.android.shelfLife.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.android.shelfLife.model.household.HouseholdViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HouseHoldSelectionDrawer(
    scope: CoroutineScope,
    drawerState: DrawerState,
    navigationActions: NavigationActions,
    householdViewModel: HouseholdViewModel,
    content: @Composable () -> Unit
) {

  val userHouseholds = householdViewModel.households.collectAsState().value
  val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
  var editMode by remember { mutableStateOf(false) }

  // Disable edit mode when the drawer is closed
  LaunchedEffect(drawerState.isClosed) {
    if (drawerState.isClosed) {
      editMode = false
    }
  }

  ModalNavigationDrawer(
      modifier = Modifier.testTag("householdSelectionDrawer"),
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet(
            drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
          Text(
              "Household selection",
              modifier =
                  Modifier.padding(vertical = 18.dp, horizontal = 16.dp)
                      .padding(horizontal = 12.dp),
              style = MaterialTheme.typography.labelMedium)
          userHouseholds.forEach { household ->
            selectedHousehold?.let {
              HouseholdDrawerItem(
                  household = household,
                  selectedHousehold = it,
                  editMode = editMode,
                  onHouseholdSelected = { household ->
                    if (household != selectedHousehold) {
                      householdViewModel.selectHousehold(household)
                    }
                    scope.launch { drawerState.close() }
                  },
                  onHouseholdEditSelected = { household ->
                    householdViewModel.selectHouseholdToEdit(household)
                    navigationActions.navigateTo(Screen.HOUSEHOLD_CREATION)
                  })
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
                      householdViewModel.selectHouseholdToEdit(null)
                      navigationActions.navigateTo(Screen.HOUSEHOLD_CREATION)
                    }) {
                      Icon(
                          imageVector = Icons.Default.Add,
                          contentDescription = "Add Household Icon",
                      )
                    }

                IconButton(
                    modifier = Modifier.testTag("editHouseholdIcon"),
                    onClick = { editMode = true }) {
                      Icon(
                          imageVector = Icons.Outlined.Edit,
                          contentDescription = "Edit Household Icon",
                      )
                    }
              }
        }
      },
      content = content)
}
