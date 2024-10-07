package com.android.shelfLife.ui.recipes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.TopLevelDestination
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.android.shelfLife.ui.overview.FoodSearchBar

@Composable
fun RecipesScreen(
    navigationActions: NavigationActions
){
    Text("AAAAAAAAA")
//    Scaffold(
//        modifier = Modifier, // todo we will place a testTag
//        topBar = { TopNavigationBar()},
//        bottomBar = {
//            BottomNavigationMenu(
//                onTabSelect = {destination -> navigationActions.navigateTo(destination)},
//                tabList =  LIST_TOP_LEVEL_DESTINATION,
//                selectedItem = Route.RECIPES)
//        },
//        content = { paddingValues ->
//            Box(modifier = Modifier.padding(paddingValues)) {
//                Text("test")
//            }
//        }
//    )
}

//@Composable
//fun RecipesSearchBar(){
//
//}