package com.android.shelfLife.ui.newoverview

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.ui.utils.FoodNameField
import com.android.shelfLife.viewmodel.FoodItemViewModel
import kotlinx.coroutines.launch

@Composable
fun FirstFoodItem(
    navigationActions: NavigationActions,
    foodItemViewModel: FoodItemViewModel = hiltViewModel(),
) {

  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  Scaffold(
      topBar = {
        CustomTopAppBar(
            onClick = { navigationActions.goBack() },
            title = stringResource(id = R.string.first_food_item_title),
            titleTestTag = "firstFoodItemTitle")
      },
      content = { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              item {
                Text(
                    text = stringResource(R.string.food_item_details_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp).testTag("firstFoodItemTitle"))
              }
              item {
                FoodNameField(
                    foodItemViewModel.foodName,
                    onFoodNameChange = { newValue -> foodItemViewModel.changeFoodName(newValue) },
                    foodNameErrorResId = foodItemViewModel.foodNameErrorResId,
                    modifier = Modifier.padding(bottom = 16.dp).testTag("inputFoodName"))
              }
              item {
                CustomButtons(
                    button1OnClick = {
                      foodItemViewModel.foodName = ""
                      navigationActions.navigateTo(Route.OVERVIEW)
                    },
                    button1TestTag = "cancelButton",
                    button1Text = stringResource(R.string.cancel_button),
                    button2OnClick = {
                      coroutineScope.launch {
                        val success = foodItemViewModel.submbitFoodName()
                        if (success) {
                            foodItemViewModel.searchByQuery(foodItemViewModel.foodName)
                            Log.e("FirstFoodItem", "foodName: ${foodItemViewModel.foodName}")
                          navigationActions.navigateTo(Screen.CHOOSE_FOOD_ITEM)
                        } else {
                          Toast.makeText(
                                  context, R.string.submission_error_message, Toast.LENGTH_SHORT)
                              .show()
                        }
                      }
                    },
                    button2TestTag = "submitButton",
                    button2Text = stringResource(R.string.submit_button_text))
              }
            }
      })
}
