package com.android.shelfLife.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.utils.FoodItemDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualFoodItemScreen(
    navigationActions: NavigationActions,
    foodItemViewModel: ListFoodItemsViewModel
) {
  val foodItem by foodItemViewModel.selectedFoodItem.collectAsState()
  Scaffold(
      modifier = Modifier.testTag("IndividualFoodItemScreen"),
      topBar = {
        TopAppBar(
            title = {},
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            modifier = Modifier.testTag("topBar"),
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("IndividualTestScreenGoBack")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back Icon")
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { destination -> navigationActions.navigateTo(destination) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.OVERVIEW)
      }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
          item {
            if (foodItem != null) {
              Text(
                  text = foodItem!!.foodFacts.name,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(16.dp).testTag("IndividualFoodItemName"))
              AsyncImage(
                  model = foodItem!!.foodFacts.imageUrl,
                  contentDescription = "Food Image",
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(horizontal = 16.dp, vertical = 8.dp)
                          .aspectRatio(1f)
                          .clip(RoundedCornerShape(8.dp))
                          .testTag("IndividualFoodItemImage"),
                  contentScale = ContentScale.Crop)

              FoodItemDetails(foodItem = foodItem!!)
            } else {
              CircularProgressIndicator(modifier = Modifier.testTag("CircularProgressIndicator"))
            }
          }
        }
      }
}
