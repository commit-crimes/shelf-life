package com.android.shelfLife.ui.overview

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.ui.utils.FoodItemDetails
import com.android.shelfLife.viewmodel.overview.IndividualFoodItemViewModel
import kotlinx.coroutines.launch

@Composable
fun IndividualFoodItemScreen(
    navigationActions: NavigationActions,
    individualFoodItemViewModel: IndividualFoodItemViewModel = hiltViewModel()
) {
  val coroutineScope = rememberCoroutineScope()

  if (individualFoodItemViewModel.selectedFood != null) {
      individualFoodItemViewModel.setIsGenerated(false)
    Scaffold(
        modifier = Modifier.testTag("IndividualFoodItemScreen"),
        topBar = {
          CustomTopAppBar(
              onClick = {
                  individualFoodItemViewModel.unselectFoodItem()
                  navigationActions.goBack() },
              title =
                  if (individualFoodItemViewModel.selectedFood != null)
                      individualFoodItemViewModel.selectedFood!!.foodFacts.name
                  else "",
              titleTestTag = "IndividualFoodItemName",
              actions = {
                IconButton(
                    onClick = {
                      individualFoodItemViewModel.selectedFood?.let {
                        coroutineScope.launch {
                          individualFoodItemViewModel.deleteFoodItem()
                          navigationActions.goBack()
                        }
                      }
                    },
                    modifier = Modifier.testTag("deleteFoodItem")) {
                      Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                    }
              })
        },
        // Floating Action Button to edit the food item
        floatingActionButton = {
          FloatingActionButton(
              onClick = { navigationActions.navigateTo(Screen.EDIT_FOOD) },
              content = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
              modifier = Modifier.testTag("editFoodFab"),
              containerColor = MaterialTheme.colorScheme.secondaryContainer)
        },
        bottomBar = {
          BottomNavigationMenu(
              onTabSelect = { destination -> navigationActions.navigateTo(destination) },
              tabList = LIST_TOP_LEVEL_DESTINATION,
              selectedItem = Route.OVERVIEW)
        }) { paddingValues ->
          LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
              if (individualFoodItemViewModel.selectedFood != null) {
                AsyncImage(
                    model = individualFoodItemViewModel.selectedFood!!.foodFacts.imageUrl,
                    contentDescription = "Food Image",
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .testTag("IndividualFoodItemImage"),
                    contentScale = ContentScale.Crop)

                FoodItemDetails(foodItem = individualFoodItemViewModel.selectedFood!!)
              } else {
                CircularProgressIndicator(modifier = Modifier.testTag("CircularProgressIndicator"))
              }
            }
          }
        }
  } else {
    navigationActions.navigateTo(Screen.EASTER_EGG)
  }
}
