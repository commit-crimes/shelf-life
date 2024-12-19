package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.*
import com.android.shelfLife.ui.utils.AmountField
import com.android.shelfLife.ui.utils.CategoryDropdownField
import com.android.shelfLife.ui.utils.CustomButtons
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.ui.utils.DateField
import com.android.shelfLife.ui.utils.FoodNameField
import com.android.shelfLife.ui.utils.UnitDropdownField
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the Add Food Item screen.
 *
 * @param navigationActions The navigation actions to be used in the screen.
 * @param paddingValues The padding values to be applied to the screen.
 * @param foodItemViewModel The ViewModel for managing the state of the food item.
 */
@Composable
fun AddFoodItemScreen(
    navigationActions: NavigationActions,
    paddingValues: PaddingValues = PaddingValues(16.dp),
    foodItemViewModel: FoodItemViewModel = hiltViewModel()
) {

  val coroutineScope = rememberCoroutineScope()

  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        CustomTopAppBar(
            onClick = { navigationActions.goBack() },
            title = stringResource(id = R.string.add_food_item_title),
            titleTestTag = "addFoodItemTitle")
      }) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding)
                    .testTag("addFoodItemScreen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              item(key = "foodName") {
                FoodNameField(
                    foodName = foodItemViewModel.foodName,
                    onFoodNameChange = { newValue -> foodItemViewModel.changeFoodName(newValue) },
                    foodNameErrorResId = foodItemViewModel.foodNameErrorResId)
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "amountAndUnit") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      AmountField(
                          amount = foodItemViewModel.amount,
                          onAmountChange = { newValue -> foodItemViewModel.changeAmount(newValue) },
                          amountErrorResId = foodItemViewModel.amountErrorResId,
                          modifier = Modifier.weight(1f),
                          testTag = "inputFoodAmount")
                      Spacer(modifier = Modifier.width(8.dp))
                      UnitDropdownField(
                          unit = foodItemViewModel.unit,
                          onUnitChange = { foodItemViewModel.unit = it },
                          unitExpanded = foodItemViewModel.unitExpanded,
                          onUnitExpandedChange = { foodItemViewModel.unitExpanded = it },
                          modifier = Modifier.weight(1f),
                          testTag = "inputFoodUnit")
                    }
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "category") {
                CategoryDropdownField(
                    category = foodItemViewModel.category,
                    onCategoryChange = { foodItemViewModel.category = it },
                    categoryExpanded = foodItemViewModel.categoryExpanded,
                    onCategoryExpandedChange = { foodItemViewModel.categoryExpanded = it })
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "location") {
                LocationDropdownField(
                    location = foodItemViewModel.location,
                    onLocationChange = { foodItemViewModel.location = it },
                    locationExpanded = foodItemViewModel.locationExpanded,
                    onExpandedChange = { foodItemViewModel.locationExpanded = it },
                    testTag = "inputFoodLocation")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "expireDate") {
                DateField(
                    date = foodItemViewModel.expireDate,
                    onDateChange = { newValue -> foodItemViewModel.changeExpiryDate(newValue) },
                    dateErrorResId = foodItemViewModel.expireDateErrorResId,
                    labelResId = R.string.expire_date_hint,
                    testTag = "inputFoodExpireDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "openDate") {
                DateField(
                    date = foodItemViewModel.openDate,
                    onDateChange = { newValue -> foodItemViewModel.changeOpenDate(newValue) },
                    dateErrorResId = foodItemViewModel.openDateErrorResId,
                    labelResId = R.string.open_date_hint,
                    testTag = "inputFoodOpenDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "buyDate") {
                DateField(
                    date = foodItemViewModel.buyDate,
                    onDateChange = { newValue -> foodItemViewModel.changeBuyDate(newValue) },
                    dateErrorResId = foodItemViewModel.buyDateErrorResId,
                    labelResId = R.string.buy_date_hint,
                    testTag = "inputFoodBuyDate")
                Spacer(modifier = Modifier.height(32.dp))
              }

              item(key = "buttons") {
                CustomButtons(
                    button1OnClick = { navigationActions.goBack() },
                    button1TestTag = "cancelButton",
                    button1Text = stringResource(R.string.cancel_button),
                    button2OnClick = {
                      coroutineScope.launch {
                        val success = foodItemViewModel.submitFoodItem()
                        if (success) {
                          // foodFactsViewModel.clearFoodFactsSuggestions()
                          navigationActions.goBack()
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
