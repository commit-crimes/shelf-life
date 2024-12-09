package com.android.shelfLife.ui.newoverview

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.android.shelfLife.R
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.*
import com.android.shelfLife.viewmodel.FoodItemViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the Edit Food Item screen.
 *
 * @param navigationActions The navigation actions to be used in the screen.
 * @param foodItemRepository The food item model.
 * @param userRepository The user model.
 * @param paddingValues The padding values to be applied to the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodItemScreen(
    navigationActions: NavigationActions,
    foodItemRepository: FoodItemRepository,
    userRepository: UserRepository,
    paddingValues: PaddingValues = PaddingValues(16.dp)
) {

  val context = LocalContext.current
  val foodItemViewModel = FoodItemViewModel(foodItemRepository, userRepository)
  val coroutineScope = rememberCoroutineScope()

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        CustomTopAppBar(
            onClick = { navigationActions.goBack() },
            title = stringResource(id = if (foodItemViewModel.isSelected) R.string.edit_food_item_title else R.string.finish_food_item_title),
            titleTestTag = "editFoodItemTitle",
            actions = {
              IconButton(
                  onClick = {
                    coroutineScope.launch {
                      foodItemViewModel.deleteFoodItem()
                      navigationActions.navigateTo(Route.OVERVIEW)
                    }
                  },
                  modifier = Modifier.testTag("deleteFoodItem")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                  }
            })
      }) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding)
                    .testTag("editFoodItemScreen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              item(key = "amountAndUnit") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      AmountField(
                          amount = foodItemViewModel.amount,
                          onAmountChange = { newValue -> foodItemViewModel.changeAmount(newValue) },
                          amountErrorResId = foodItemViewModel.amountErrorResId,
                          modifier = Modifier.weight(1f),
                          testTag = "editFoodAmount")
                      Spacer(modifier = Modifier.width(8.dp))
                      // Display unit as card (non-editable)
                      Card(
                          border = CardDefaults.outlinedCardBorder(),
                          shape = MaterialTheme.shapes.large,
                          modifier = Modifier.weight(1f).testTag("editFoodUnit")) {
                            Text(
                                text = foodItemViewModel.unit.name,
                                modifier = Modifier.padding(12.dp))
                          }
                    }
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "location") {
                NewLocationDropdownField(
                    location = foodItemViewModel.location,
                    onLocationChange = { foodItemViewModel.location = it },
                    locationExpanded = foodItemViewModel.locationExpanded,
                    onExpandedChange = { foodItemViewModel.locationExpanded = it },
                    testTag = "editFoodLocation")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "expireDate") {
                DateField(
                    date = foodItemViewModel.expireDate,
                    onDateChange = { newValue -> foodItemViewModel.changeExpiryDate(newValue) },
                    dateErrorResId = foodItemViewModel.expireDateErrorResId,
                    labelResId = R.string.expire_date_hint,
                    testTag = "editFoodExpireDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "openDate") {
                DateField(
                    date = foodItemViewModel.openDate,
                    onDateChange = { newValue -> foodItemViewModel.changeOpenDate(newValue) },
                    dateErrorResId = foodItemViewModel.openDateErrorResId,
                    labelResId = R.string.open_date_hint,
                    testTag = "editFoodOpenDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "buyDate") {
                DateField(
                    date = foodItemViewModel.buyDate,
                    onDateChange = { newValue -> foodItemViewModel.changeBuyDate(newValue) },
                    dateErrorResId = foodItemViewModel.buyDateErrorResId,
                    labelResId = R.string.buy_date_hint,
                    testTag = "editFoodBuyDate")
                Spacer(modifier = Modifier.height(32.dp))
              }

                //Only if its in Edit Food Item and not in Add Food Item
                if (foodItemViewModel.isSelected){
                    item{
                        foodItemViewModel.selectedImage ?.let {
                            Text(
                                stringResource(id = R.string.selected_image_label),
                                modifier = Modifier.testTag("selectedImageText")
                            )
                            Image(
                                painter = rememberAsyncImagePainter(it.imageUrl),
                                contentDescription = null,
                                modifier = Modifier.size(150.dp).padding(8.dp).testTag("selectedImage")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    item(key = "ChangePic"){
                        Button(
                            onClick = { navigationActions.navigateTo(Screen.CHOOSE_FOOD_ITEM) },
                            modifier = Modifier.testTag("navigateToChooseFoodItemButton")
                        ) {
                            Text("Go to Choose Food Item")
                        }
                    }
                }

              item(key = "buttons") {
                CustomButtons(
                    button1OnClick = { navigationActions.navigateTo(Route.OVERVIEW) },
                    button1TestTag = "cancelButton",
                    button1Text = stringResource(R.string.cancel_button),
                    button2OnClick = {
                      coroutineScope.launch {
                        val success = foodItemViewModel.submitFoodItem()
                        if (success) {
                            navigationActions.navigateTo(Route.OVERVIEW)
                        } else {
                          Toast.makeText(
                                  context, R.string.submission_error_message, Toast.LENGTH_SHORT)
                              .show()
                        }
                      }
                    },
                    button2TestTag = "foodSave",
                    button2Text = stringResource(R.string.submit_button_text))
              }
            }
      }
}
