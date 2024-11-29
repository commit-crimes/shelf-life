package com.android.shelfLife.ui.overview

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.*
import com.google.firebase.Timestamp

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
    houseHoldViewModel: HouseholdViewModel,
    foodItemViewModel: ListFoodItemsViewModel,
    foodFactsViewModel: FoodFactsViewModel,
    paddingValues: PaddingValues = PaddingValues(16.dp)
) {

  var foodName by remember { mutableStateOf("") }
  var amount by remember { mutableStateOf("") }
  var unit by remember { mutableStateOf(FoodUnit.GRAM) }
  var category by remember { mutableStateOf(FoodCategory.OTHER) }
  var location by remember { mutableStateOf(FoodStorageLocation.PANTRY) }
  var expireDate by remember { mutableStateOf("") }
  var openDate by remember { mutableStateOf("") }
  var buyDate by remember { mutableStateOf(formatTimestampToDate(Timestamp.now())) }

  var foodNameErrorResId by remember { mutableStateOf<Int?>(null) }
  var amountErrorResId by remember { mutableStateOf<Int?>(null) }
  var expireDateErrorResId by remember { mutableStateOf<Int?>(null) }
  var openDateErrorResId by remember { mutableStateOf<Int?>(null) }
  var buyDateErrorResId by remember { mutableStateOf<Int?>(null) }

  var unitExpanded by remember { mutableStateOf(false) }
  var categoryExpanded by remember { mutableStateOf(false) }
  var locationExpanded by remember { mutableStateOf(false) }
  var selectedImage by remember { mutableStateOf<FoodFacts?>(null) }
  val foodFacts by foodFactsViewModel.foodFactsSuggestions.collectAsState()

  val context = LocalContext.current

  DisposableEffect(Unit) { onDispose { foodFactsViewModel.clearFoodFactsSuggestions() } }

  /** Validates all fields when the submit button is clicked. */
  fun validateAllFieldsWhenSubmitButton() {
    foodNameErrorResId = validateFoodName(foodName)
    amountErrorResId = validateAmount(amount)
    buyDateErrorResId = validateBuyDate(buyDate)
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  modifier = Modifier.testTag("addFoodItemTitle"),
                  text = stringResource(id = R.string.add_food_item_title))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription =
                            stringResource(id = R.string.go_back_button_description))
                  }
            })
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
                    foodName = foodName,
                    onFoodNameChange = { newValue ->
                      foodName = newValue
                      foodNameErrorResId = validateFoodName(foodName)
                      foodFactsViewModel.searchByQuery(foodName)
                    },
                    foodNameErrorResId = foodNameErrorResId)
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "amountAndUnit") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      AmountField(
                          amount = amount,
                          onAmountChange = { newValue ->
                            amount = newValue
                            amountErrorResId = validateAmount(amount)
                          },
                          amountErrorResId = amountErrorResId,
                          modifier = Modifier.weight(1f),
                          testTag = "inputFoodAmount")
                      Spacer(modifier = Modifier.width(8.dp))
                      UnitDropdownField(
                          unit = unit,
                          onUnitChange = { unit = it },
                          unitExpanded = unitExpanded,
                          onUnitExpandedChange = { unitExpanded = it },
                          modifier = Modifier.weight(1f),
                          testTag = "inputFoodUnit")
                    }
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "category") {
                CategoryDropdownField(
                    category = category,
                    onCategoryChange = { category = it },
                    categoryExpanded = categoryExpanded,
                    onCategoryExpandedChange = { categoryExpanded = it })
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "location") {
                LocationDropdownField(
                    location = location,
                    onLocationChange = { location = it },
                    locationExpanded = locationExpanded,
                    onExpandedChange = { locationExpanded = it },
                    testTag = "inputFoodLocation")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "expireDate") {
                DateField(
                    date = expireDate,
                    onDateChange = { newValue ->
                      expireDate = newValue.filter { it.isDigit() }
                      expireDateErrorResId =
                          validateExpireDate(expireDate, buyDate, buyDateErrorResId)
                      // Re-validate Open Date since it depends on Expire Date
                      openDateErrorResId =
                          validateOpenDate(
                              openDate,
                              buyDate,
                              buyDateErrorResId,
                              expireDate,
                              expireDateErrorResId)
                    },
                    dateErrorResId = expireDateErrorResId,
                    labelResId = R.string.expire_date_hint,
                    testTag = "inputFoodExpireDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "openDate") {
                DateField(
                    date = openDate,
                    onDateChange = { newValue ->
                      openDate = newValue.filter { it.isDigit() }
                      openDateErrorResId =
                          validateOpenDate(
                              openDate,
                              buyDate,
                              buyDateErrorResId,
                              expireDate,
                              expireDateErrorResId)
                    },
                    dateErrorResId = openDateErrorResId,
                    labelResId = R.string.open_date_hint,
                    testTag = "inputFoodOpenDate")
                Spacer(modifier = Modifier.height(16.dp))
              }

              item(key = "buyDate") {
                DateField(
                    date = buyDate,
                    onDateChange = { newValue ->
                      buyDate = newValue.filter { it.isDigit() }
                      buyDateErrorResId = validateBuyDate(buyDate)
                      // Re-validate Expire Date and Open Date since they depend on Buy Date
                      expireDateErrorResId =
                          validateExpireDate(expireDate, buyDate, buyDateErrorResId)
                      openDateErrorResId =
                          validateOpenDate(
                              openDate,
                              buyDate,
                              buyDateErrorResId,
                              expireDate,
                              expireDateErrorResId)
                    },
                    dateErrorResId = buyDateErrorResId,
                    labelResId = R.string.buy_date_hint,
                    testTag = "inputFoodBuyDate")
                Spacer(modifier = Modifier.height(32.dp))
              }

              if (foodFacts.isNotEmpty()) {
                item(key = "selectImage") {
                  Text(
                      text = stringResource(id = R.string.select_image_label),
                      modifier = Modifier.testTag("selectImage"))
                  Spacer(modifier = Modifier.height(8.dp))

                  LazyRow(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(foodFacts.take(10)) { foodFact ->
                          Box(
                              modifier =
                                  Modifier.fillMaxWidth(0.3f)
                                      .aspectRatio(1f)
                                      .border(
                                          width = if (selectedImage == foodFact) 2.dp else 1.dp,
                                          color = MaterialTheme.colorScheme.primary,
                                          shape = RoundedCornerShape(8.dp))
                                      .clickable { selectedImage = foodFact }
                                      .testTag("foodImage")) {
                                Image(
                                    painter = rememberAsyncImagePainter(foodFact.imageUrl),
                                    contentDescription = foodFact.name,
                                    modifier = Modifier.fillMaxSize())
                              }
                        }
                      }
                  Spacer(modifier = Modifier.height(16.dp))
                }
              }

              // Add a "No Image" option
              item("noImage") {
                Box(
                    modifier =
                        Modifier.size(100.dp)
                            .border(
                                width = if (selectedImage == null) 4.dp else 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp))
                            .clickable {
                              selectedImage = null // Indicate no image selected
                            }
                            .testTag("noImage"),
                    contentAlignment = Alignment.Center) {
                      Text(
                          stringResource(id = R.string.no_image_option),
                          modifier = Modifier.testTag("noImageText"))
                    }
                Spacer(modifier = Modifier.height(16.dp))
              }

              // Display Selected Image
              selectedImage?.let {
                item {
                  Text(
                      stringResource(id = R.string.selected_image_label),
                      modifier = Modifier.testTag("selectedImageText"))
                  Image(
                      painter = rememberAsyncImagePainter(it.imageUrl),
                      contentDescription = null,
                      modifier = Modifier.size(150.dp).padding(8.dp).testTag("selectedImage"))
                  Spacer(modifier = Modifier.height(16.dp))
                }
              }
                  ?: item {
                    Text(
                        stringResource(id = R.string.default_image_label),
                        modifier = Modifier.testTag("defaultImageText"))
                    Image(
                        painter = rememberAsyncImagePainter(FoodFacts.DEFAULT_IMAGE_URL),
                        contentDescription = null,
                        modifier = Modifier.size(150.dp).padding(8.dp).testTag("defaultImage"))
                    Spacer(modifier = Modifier.height(16.dp))
                  }

              item(key = "submitButton") {
                // Submit Button
                Button(
                    onClick = {
                      validateAllFieldsWhenSubmitButton()
                      val isExpireDateValid =
                          expireDateErrorResId == null && expireDate.isNotEmpty()
                      val isOpenDateValid = openDateErrorResId == null
                      val isBuyDateValid = buyDateErrorResId == null && buyDate.isNotEmpty()
                      val isFoodNameValid = foodNameErrorResId == null
                      val isAmountValid = amountErrorResId == null

                      val expiryTimestamp = formatDateToTimestamp(expireDate)
                      val openTimestamp =
                          if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
                      val buyTimestamp = formatDateToTimestamp(buyDate)

                      if (isExpireDateValid &&
                          isOpenDateValid &&
                          isBuyDateValid &&
                          isFoodNameValid &&
                          isAmountValid &&
                          expiryTimestamp != null &&
                          buyTimestamp != null) {
                        val foodFacts =
                            FoodFacts(
                                name = foodName,
                                barcode = selectedImage?.barcode ?: "",
                                quantity = Quantity(amount.toDouble(), unit),
                                category = category,
                                nutritionFacts = selectedImage?.nutritionFacts ?: NutritionFacts(),
                                imageUrl = selectedImage?.imageUrl ?: FoodFacts.DEFAULT_IMAGE_URL)
                        val newFoodItem =
                            FoodItem(
                                uid = foodItemViewModel.getUID(),
                                foodFacts = foodFacts,
                                location = location,
                                expiryDate = expiryTimestamp,
                                openDate = openTimestamp,
                                buyDate = buyTimestamp,
                                status = FoodStatus.CLOSED)
                        houseHoldViewModel.addFoodItem(newFoodItem)
                        foodFactsViewModel.clearFoodFactsSuggestions()
                        navigationActions.goBack()
                      } else {
                        Toast.makeText(
                                context, R.string.submission_error_message, Toast.LENGTH_SHORT)
                            .show()
                      }
                    },
                    modifier = Modifier.testTag("foodSave").fillMaxWidth().height(50.dp)) {
                      Text(
                          text = stringResource(id = R.string.submit_button_text), fontSize = 18.sp)
                    }
              }
            }
      }
}
