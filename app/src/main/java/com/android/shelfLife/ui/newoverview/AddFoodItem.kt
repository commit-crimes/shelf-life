package com.android.shelfLife.ui.newoverview

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
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.*
import com.android.shelfLife.viewmodel.foodItem.FoodItemViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the Add Food Item screen.
 *
 * @param navigationActions The navigation actions to be used in the screen.
 * @param houseHoldViewModel The ViewModel for the household.
 * @param foodItemViewModel The ViewModel for the food items.
 * @param paddingValues The padding values to be applied to the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodItemScreen(
    navigationActions: NavigationActions,
    foodItemViewModel: FoodItemViewModel,
    paddingValues: PaddingValues = PaddingValues(16.dp)
) {
  // val foodFacts by foodFactsViewModel.foodFactsSuggestions.collectAsState()

  val coroutineScope = rememberCoroutineScope()

  val context = LocalContext.current

  LaunchedEffect(Unit) {
    if (!foodItemViewModel.isSelected) {
      foodItemViewModel.reset()
    }
  }

  // DisposableEffect(Unit) { onDispose { foodFactsViewModel.clearFoodFactsSuggestions() } }

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
                    onFoodNameChange = { newValue ->
                      foodItemViewModel.changeFoodName(newValue)
                      // foodFactsViewModel.searchByQuery(foodName) // TODO ask kevin
                    },
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
                NewLocationDropdownField(
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

              //              if (foodFacts.isNotEmpty()) {
              //                item(key = "selectImage") {
              //                  Text(
              //                      text = stringResource(id = R.string.select_image_label),
              //                      modifier = Modifier.testTag("selectImage"))
              //                  Spacer(modifier = Modifier.height(8.dp))
              //
              //                  LazyRow(
              //                      modifier = Modifier.fillMaxWidth(),
              //                      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              //                        items(foodFacts.take(10)) { foodFact ->
              //                          Box(
              //                              modifier =
              //                                  Modifier.fillMaxWidth(0.3f)
              //                                      .aspectRatio(1f)
              //                                      .border(
              //                                          width = if (selectedImage == foodFact)
              // 2.dp else 1.dp,
              //                                          color = MaterialTheme.colorScheme.primary,
              //                                          shape = RoundedCornerShape(8.dp))
              //                                      .clickable { selectedImage = foodFact }
              //                                      .testTag("foodImage")) {
              //                                Image(
              //                                    painter =
              // rememberAsyncImagePainter(foodFact.imageUrl),
              //                                    contentDescription = foodFact.name,
              //                                    modifier = Modifier.fillMaxSize())
              //                              }
              //                        }
              //                      }
              //                  Spacer(modifier = Modifier.height(16.dp))
              //                }
              //              }

              // Add a "No Image" option
              //              item("noImage") {
              //                Box(
              //                    modifier =
              //                        Modifier.size(100.dp)
              //                            .border(
              //                                width = if (selectedImage == null) 4.dp else 1.dp,
              //                                color = MaterialTheme.colorScheme.primary,
              //                                shape = RoundedCornerShape(8.dp))
              //                            .clickable {
              //                              selectedImage = null // Indicate no image selected
              //                            }
              //                            .testTag("noImage"),
              //                    contentAlignment = Alignment.Center) {
              //                      Text(
              //                          stringResource(id = R.string.no_image_option),
              //                          modifier = Modifier.testTag("noImageText"))
              //                    }
              //                Spacer(modifier = Modifier.height(16.dp))
              //              }

              // Display Selected Image
              //              selectedImage?.let {
              //                item {
              //                  Text(
              //                      stringResource(id = R.string.selected_image_label),
              //                      modifier = Modifier.testTag("selectedImageText"))
              //                  Image(
              //                      painter = rememberAsyncImagePainter(it.imageUrl),
              //                      contentDescription = null,
              //                      modifier =
              // Modifier.size(150.dp).padding(8.dp).testTag("selectedImage"))
              //                  Spacer(modifier = Modifier.height(16.dp))
              //                }
              //              }
              //                  ?: item {
              //                    Text(
              //                        stringResource(id = R.string.default_image_label),
              //                        modifier = Modifier.testTag("defaultImageText"))
              //                    Image(
              //                        painter =
              // rememberAsyncImagePainter(FoodFacts.DEFAULT_IMAGE_URL),
              //                        contentDescription = null,
              //                        modifier =
              // Modifier.size(150.dp).padding(8.dp).testTag("defaultImage"))
              //                    Spacer(modifier = Modifier.height(16.dp))
              //                  }

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
