package com.android.shelfLife.ui.navigation

import HouseholdViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.ui.component.AddHouseholdDialogContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    onFilterClick: () -> Unit = {},
    houseHold: HouseHold,
    onHouseholdChange: (HouseHold) -> Unit,
    userHouseholds: List<HouseHold>,
    householdViewModel: HouseholdViewModel
) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    var selectedHousehold by remember { mutableStateOf(houseHold) }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                isDrawerOpen = true
            }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Icon",
                    tint = Color.White
                )
            }
        },
        title = {
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = selectedHousehold.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = { onFilterClick() }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Icon",
                        tint = Color.White
                    )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,  // Custom light green background to match the app
            titleContentColor = Color.White
        )
    )
    if(isDrawerOpen){
        HouseHoldSelector(
            onClose = { isDrawerOpen = false },
            selectedHousehold = selectedHousehold,
            onHouseholdSelected = { household ->
                selectedHousehold = household
                isDrawerOpen = false // Close the drawer after selecting
                onHouseholdChange(household)
            },
            userHouseholds = userHouseholds,
            householdViewModel = householdViewModel)
    }
}

@Composable
fun HouseHoldSelector(onClose: () -> Unit, selectedHousehold: HouseHold,
                      onHouseholdSelected: (HouseHold) -> Unit,
                      userHouseholds: List<HouseHold>,
                      householdViewModel: HouseholdViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Open)
    LaunchedEffect(Unit) { drawerState.open()}
    var showDialog by remember { mutableStateOf(false) }
    AddHouseHoldDialogue(showDialog, householdViewModel)
    // List of HouseHolds needs to be fetched from the server
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Household selection",
                    modifier = Modifier
                        .padding(vertical = 18.dp, horizontal = 16.dp)
                        .padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelMedium
                )
                userHouseholds.forEach { household ->
                    HouseHoldElement(
                        household = household,
                        selectedHousehold = selectedHousehold,
                        onHouseholdSelected = onHouseholdSelected
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Household Icon",
                            modifier = Modifier.testTag("addHouseholdIcon")
                        )
                    }
                }
            }
        }, content = {
            drawerState.apply{
                if (isClosed) onClose()
            }
        }
    )
}

@Composable
fun AddHouseHoldDialogue(showDialog: Boolean, householdViewModel: HouseholdViewModel) {
    AddHouseholdDialogContent(
        showDialog = showDialog,
        onDismiss = { /* Logic to dismiss the dialog, handled externally */ },
        onAddHousehold = { householdName ->
            householdViewModel.addNewHousehold(householdName)
        }
    )
}

@Composable
fun HouseHoldElement(
    household: HouseHold,
    selectedHousehold: HouseHold,
    onHouseholdSelected: (HouseHold) -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = household.name,
                fontWeight = if (household == selectedHousehold) FontWeight.Bold else FontWeight.Normal,
                color = if (household == selectedHousehold) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
        },
        selected = household == selectedHousehold,
        onClick = { onHouseholdSelected(household) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
