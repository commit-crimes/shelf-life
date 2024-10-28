package com.android.shelfLife.ui.navigation

import android.util.Log
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
import com.android.shelfLife.ui.overview.AddHouseHoldPopUp
import com.android.shelfLife.ui.overview.EditHouseHoldPopUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HouseHoldSelectionDrawer(
    scope: CoroutineScope,
    drawerState: DrawerState,
    householdViewModel: HouseholdViewModel,
    content: @Composable () -> Unit
) {

  val userHouseholds = householdViewModel.households.collectAsState().value
  val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()

  var showDialog by remember { mutableStateOf(false) }
  var showEdit by remember { mutableStateOf(false) }

  // These two popups will be replaced in the future with a dedicated screen
  AddHouseHoldPopUp(
      showDialog = showDialog,
      onDismiss = { showDialog = false },
      householdViewModel = householdViewModel,
  )

  EditHouseHoldPopUp(
      showDialog = showEdit,
      onDismiss = { showEdit = false },
      householdViewModel = householdViewModel)

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
          userHouseholds.forEachIndexed { index, household ->
            Log.d("HouseHoldSelectionDrawer", "HouseHoldElement index: $index")
            selectedHousehold?.let {
              HouseHoldElement(
                  household = household,
                  selectedHousehold = it,
                  onHouseholdSelected = { household ->
                    if (household != selectedHousehold) {
                      householdViewModel.selectHousehold(household)
                    }
                    scope.launch { drawerState.close() }
                  },
                  modifier = Modifier.testTag("householdElement_$index"))
            }
          }
          HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
          Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center) {
                IconButton(
                    modifier = Modifier.testTag("addHouseholdIcon"),
                    onClick = { showDialog = true }) {
                      Icon(
                          imageVector = Icons.Default.Add,
                          contentDescription = "Add Household Icon",
                      )
                    }

                IconButton(
                    modifier = Modifier.testTag("editHouseholdIcon"),
                    onClick = { showEdit = true }) {
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
