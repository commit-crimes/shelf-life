package com.android.shelfLife.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.utils.FilterBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    houseHold: HouseHold,
    onHouseholdChange: (HouseHold) -> Unit,
    onHamburgerClick: () -> Unit = {},
    userHouseholds: List<HouseHold>,
    householdViewModel: HouseholdViewModel,
    filters : List<String>
) {
  var showFilterBar by remember { mutableStateOf(false) }
  Column {
    TopAppBar(
        navigationIcon = {
          IconButton(onClick = { onHamburgerClick() }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu Icon",
                tint = Color.White)
          }
        },
        title = {
          Row(modifier = Modifier.padding(end = 8.dp)) {
            Text(
                text = houseHold.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White)
          }
        },
        actions = {
            if(filters.isNotEmpty()){
                IconButton(onClick = { showFilterBar = !showFilterBar }) { // Toggle filter bar visibility
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Icon",
                        tint = Color.White)
                }
            }
        },
        colors =
            TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                titleContentColor = Color.White))

      if(filters.isNotEmpty()){
          AnimatedVisibility(
              visible = showFilterBar,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically()) {
              FilterBar(filters)
          }
      }
  }
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
            color =
                if (household == selectedHousehold) MaterialTheme.colorScheme.primary
                else Color.Unspecified)
      },
      selected = household == selectedHousehold,
      onClick = { onHouseholdSelected(household) },
      modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
}
