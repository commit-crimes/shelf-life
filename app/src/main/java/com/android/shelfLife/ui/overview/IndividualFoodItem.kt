package com.android.shelfLife.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.android.shelfLife.R
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.shelfLife.ui.navigation.TopNavigationBar
import com.android.shelfLife.ui.utils.FoodItemDetails
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@Composable
fun IndividualFoodItemScreen(
    foodItemId: String?,
    navigationActions: NavigationActions,
    householdViewModel: HouseholdViewModel
) {
    val foodItem = foodItemId?.let { householdViewModel.getFoodItemById(it).collectAsState(initial = null) }

    Scaffold(
        modifier = Modifier.testTag("IndividualFoodItemScreen"),
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { destination -> navigationActions.navigateTo(destination) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.OVERVIEW
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                val selectedFoodItem = foodItem?.value
                if (selectedFoodItem != null) {
                    Text(
                        text = selectedFoodItem.foodFacts.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.minecraft_steak),
                        contentDescription = "Image of ${selectedFoodItem.foodFacts.name}",
                        modifier = Modifier
                            .size(360.dp, 360.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    FoodItemDetails(foodItem = selectedFoodItem)
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}