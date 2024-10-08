package com.android.shelfLife.ui.navigation

import HouseholdViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.shelfLife.model.household.HouseHold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    houseHold: HouseHold,
    onFilterClick: () -> Unit = {},
    onHamburgerClick: () -> Unit = {},
    householdViewModel: HouseholdViewModel
) {

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                onHamburgerClick()
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
                    text = houseHold.name,
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
}
