package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
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

  return Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("") },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(Icons.Outlined.Close, contentDescription = "Close")
              }
            },
            actions = {
              if (householdToEdit != null) {
                IconButton(
                    onClick = {
                      // TODO add are you sure dialog
                      householdViewModel.deleteHouseholdById(householdToEdit.uid)
                      navigationActions.goBack()
                    }) {
                      Icon(
                          Icons.Outlined.Delete,
                          contentDescription = "Delete",
                          tint = MaterialTheme.colorScheme.error)
                    }
              }
            })
      },
  ) { paddingValues ->
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
              modifier = Modifier.padding(30.dp).fillMaxWidth())
          Text(
              "Household members",
              style = TextStyle(fontSize = 30.sp),
              textAlign = TextAlign.Start,
              modifier = Modifier.fillMaxWidth().padding(top = 20.dp))
          // TODO add member list here, maybe make it a composable for reuse
        }
    Row(
        modifier =
            Modifier.fillMaxSize().padding(top = 25.dp, bottom = 60.dp, start = 45.dp, end = 45.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween) {
          Button(
              onClick = {
                // TODO change logic to include multiple members

                if (householdViewModel.checkIfHouseholdNameExists(houseHoldName) &&
                    (householdToEdit == null || houseHoldName != householdToEdit.name)) {
                  isError = true
                } else {
                  if (householdToEdit != null) {
                    val updatedHouseHold = householdToEdit.copy(name = houseHoldName)
                    householdViewModel.updateHousehold(updatedHouseHold)
                  } else {
                    householdViewModel.addNewHousehold(houseHoldName)
                  }
                  navigationActions.goBack()
                }
              },
          ) {
            Text("Confirm", style = TextStyle(fontSize = 22.sp), modifier = Modifier.padding(10.dp))
          }
          Button(
              onClick = { navigationActions.goBack() },
          ) {
            Text("Cancel", style = TextStyle(fontSize = 22.sp), modifier = Modifier.padding(10.dp))
          }
        }
  }
}
