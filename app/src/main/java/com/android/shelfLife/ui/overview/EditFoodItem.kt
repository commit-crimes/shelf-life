package com.android.shelfLife.ui.overview

import android.widget.Toast
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.*
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the Edit Food Item screen.
 *
 * This screen allows the user to edit the details of a food item, including its amount, location,
 * expiry date, open date, and buy date. If the food item exists, the user can update the information,
 * and the food item can be deleted. The UI is structured with a `LazyColumn` for a smooth scrolling experience.
 *
 * @param navigationActions The navigation actions to be used in the screen.
 * @param paddingValues The padding values to be applied to the screen.
 * @param foodItemViewModel The [FoodItemViewModel] that handles the food item data and interactions.
 */
@Composable
fun EditFoodItemScreen(
    navigationActions: NavigationActions,
    paddingValues: PaddingValues = PaddingValues(16.dp),
    foodItemViewModel: FoodItemViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Ensure the food item is not null before displaying the screen
    if (foodItemViewModel.selectedFood != null) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CustomTopAppBar(
                    onClick = { navigationActions.goBack() },
                    title = stringResource(id = R.string.edit_food_item_title),
                    titleTestTag = "editFoodItemTitle",
                    actions = {
                        // Delete food item action
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    foodItemViewModel.deleteFoodItem()
                                    navigationActions.navigateTo(Route.OVERVIEW)
                                }
                            },
                            modifier = Modifier.testTag("deleteFoodItem")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                        }
                    })
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(innerPadding)
                    .testTag("editFoodItemScreen"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Food Name Field
                item(key = "foodName") {
                    FoodNameField(
                        foodName = foodItemViewModel.foodName,
                        onFoodNameChange = { newValue ->
                            foodItemViewModel.changeFoodName(newValue)
                        },
                        foodNameErrorResId = foodItemViewModel.foodNameErrorResId
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Amount and Unit Fields
                item(key = "amountAndUnit") {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AmountField(
                            amount = foodItemViewModel.amount,
                            onAmountChange = { newValue -> foodItemViewModel.changeAmount(newValue) },
                            amountErrorResId = foodItemViewModel.amountErrorResId,
                            modifier = Modifier.weight(1f),
                            testTag = "editFoodAmount"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Display unit as card (non-editable)
                        Card(
                            border = CardDefaults.outlinedCardBorder(),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.weight(1f).testTag("editFoodUnit")
                        ) {
                            Text(
                                text = foodItemViewModel.unit.name,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Location Field
                item(key = "location") {
                    NewLocationDropdownField(
                        location = foodItemViewModel.location,
                        onLocationChange = { foodItemViewModel.location = it },
                        locationExpanded = foodItemViewModel.locationExpanded,
                        onExpandedChange = { foodItemViewModel.locationExpanded = it },
                        testTag = "editFoodLocation"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Expiry Date Field
                item(key = "expireDate") {
                    DateField(
                        date = foodItemViewModel.expireDate,
                        onDateChange = { newValue -> foodItemViewModel.changeExpiryDate(newValue) },
                        dateErrorResId = foodItemViewModel.expireDateErrorResId,
                        labelResId = R.string.expire_date_hint,
                        testTag = "editFoodExpireDate"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Open Date Field
                item(key = "openDate") {
                    DateField(
                        date = foodItemViewModel.openDate,
                        onDateChange = { newValue -> foodItemViewModel.changeOpenDate(newValue) },
                        dateErrorResId = foodItemViewModel.openDateErrorResId,
                        labelResId = R.string.open_date_hint,
                        testTag = "editFoodOpenDate"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Buy Date Field
                item(key = "buyDate") {
                    DateField(
                        date = foodItemViewModel.buyDate,
                        onDateChange = { newValue -> foodItemViewModel.changeBuyDate(newValue) },
                        dateErrorResId = foodItemViewModel.buyDateErrorResId,
                        labelResId = R.string.buy_date_hint,
                        testTag = "editFoodBuyDate"
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Buttons for saving or cancelling the edits
                item(key = "buttons") {
                    CustomButtons(
                        button1OnClick = { navigationActions.goBack() },
                        button1TestTag = "cancelButton",
                        button1Text = stringResource(R.string.cancel_button),
                        button2OnClick = {
                            coroutineScope.launch {
                                val success = foodItemViewModel.submitFoodItem()
                                if (success) {
                                    navigationActions.goBack()
                                } else {
                                    Toast.makeText(
                                        context, R.string.submission_error_message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        button2TestTag = "foodSave",
                        button2Text = stringResource(R.string.submit_button_text)
                    )
                }
            }
        }
    } else {
        // If no food item is selected, navigate to Easter Egg screen
        navigationActions.navigateTo(Screen.EASTER_EGG)
    }
}