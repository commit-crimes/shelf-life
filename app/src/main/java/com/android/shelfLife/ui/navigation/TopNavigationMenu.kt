package com.android.shelfLife.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    onFilterClick: () -> Unit = {}
) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    var selectedHousehold by remember { mutableStateOf("Household 1") }
    // HouseHold variables
    val householdName = "Household 1"
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
                    text = householdName,
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
            })
    }
}

@Composable
fun HouseHoldSelector(onClose: () -> Unit, selectedHousehold: String,
                      onHouseholdSelected: (String) -> Unit,){
    val drawerState = rememberDrawerState(DrawerValue.Open)
    LaunchedEffect(Unit) { drawerState.open()}
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
                HouseHoldElement("Household 1", selectedHousehold, onHouseholdSelected)
                HouseHoldElement("Household 2", selectedHousehold, onHouseholdSelected)
                HouseHoldElement("Household 3", selectedHousehold, onHouseholdSelected)
                HouseHoldElement("Household 4", selectedHousehold, onHouseholdSelected)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }, content = {
            drawerState.apply{
                if (isClosed) onClose()
            }
        }
    )
}

@Composable
fun HouseHoldElement(householdName : String, selectedHousehold: String, onHouseholdSelected: (String) -> Unit){
    NavigationDrawerItem(
        label = {
            Text(
                text = householdName,
                fontWeight = if (householdName == selectedHousehold) FontWeight.Bold else FontWeight.Normal,
                color = if (householdName == selectedHousehold) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
        },
        selected = selectedHousehold == householdName,
        onClick = {
            onHouseholdSelected(householdName)
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Preview(showBackground = true)
@Composable
fun CustomTopAppBarPreview() {
    TopNavigationBar()
}