package com.android.shelfLife.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.utils.formatDateToTimestamp
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.ui.utils.validateBuyDate
import com.android.shelfLife.ui.utils.validateExpireDate
import com.android.shelfLife.ui.utils.validateNumber
import com.android.shelfLife.ui.utils.validateOpenDate
import com.android.shelfLife.ui.utils.validateString
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FoodItemViewModel
@Inject
constructor(
    private val foodItemRepository: FoodItemRepository,
    private val userRepository: UserRepository
) : ViewModel() {

  var selectedFood by mutableStateOf<FoodItem?>(null)

  var isSelected by mutableStateOf(false)

  var foodName by mutableStateOf("")
  var amount by mutableStateOf("")
  var unit by mutableStateOf(FoodUnit.GRAM)
  var category by mutableStateOf(FoodCategory.OTHER)
  var location by mutableStateOf(FoodStorageLocation.PANTRY)
  var expireDate by mutableStateOf("")
  var openDate by mutableStateOf("")
  var buyDate by mutableStateOf(formatTimestampToDate(Timestamp.now()))

  var foodNameErrorResId by mutableStateOf<Int?>(null)
  var amountErrorResId by mutableStateOf<Int?>(null)
  var expireDateErrorResId by mutableStateOf<Int?>(null)
  var openDateErrorResId by mutableStateOf<Int?>(null)
  var buyDateErrorResId by mutableStateOf<Int?>(null)

  var unitExpanded by mutableStateOf(false)
  var categoryExpanded by mutableStateOf(false)
  var locationExpanded by mutableStateOf(false)
  var selectedImage by mutableStateOf<FoodFacts?>(null)

  init {
    selectedFood = foodItemRepository.selectedFoodItem.value
    if (selectedFood != null) {
      isSelected = true
      foodName = selectedFood!!.foodFacts.name
      amount = selectedFood!!.foodFacts.quantity.amount.toString()
      unit = selectedFood!!.foodFacts.quantity.unit
      category = selectedFood!!.foodFacts.category
      location = selectedFood!!.location
      expireDate = selectedFood!!.expiryDate?.let { formatTimestampToDate(it) } ?: ""
      openDate = selectedFood!!.openDate?.let { formatTimestampToDate(it) } ?: ""
      buyDate = selectedFood!!.buyDate?.let { formatTimestampToDate(it) } ?: ""
    } else {
      isSelected = false
    }
  }

  /** Validates all fields when the submit button is clicked. */
  fun validateAllFieldsWhenSubmitButton() {
    if (!isSelected) {
      foodNameErrorResId = validateString(foodName)
    }
    amountErrorResId = validateNumber(amount)
    buyDateErrorResId = validateBuyDate(buyDate)
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  suspend fun addFoodItem(foodItem: FoodItem) {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null) {
      foodItemRepository.addFoodItem(householdId, foodItem)
    }
  }

  suspend fun editFoodItem(foodItem: FoodItem) {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null) {
      foodItemRepository.updateFoodItem(householdId, foodItem)
    }
  }

  suspend fun deleteFoodItem() {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null) {
      foodItemRepository.deleteFoodItem(householdId, selectedFood!!.uid)
      foodItemRepository.selectFoodItem(null)
    }
  }

  fun changeFoodName(newFoodName: String) {
    foodName = newFoodName
    foodNameErrorResId = validateString(foodName)
  }

  fun changeAmount(newAmount: String) {
    amount = newAmount
    amountErrorResId = validateNumber(amount)
  }

  fun changeExpiryDate(newDate: String) {
    expireDate = newDate.filter { it.isDigit() }
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    // Re-validate Open Date since it depends on Expire Date
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  fun changeOpenDate(newDate: String) {
    openDate = newDate.filter { it.isDigit() }
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  fun changeBuyDate(newDate: String) {
    buyDate = newDate.filter { it.isDigit() }
    buyDateErrorResId = validateBuyDate(buyDate)
    // Re-validate Expire Date and Open Date since they depend on Buy Date
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  suspend fun submitFoodItem(): Boolean {
    validateAllFieldsWhenSubmitButton()
    val isExpireDateValid = expireDateErrorResId == null && expireDate.isNotEmpty()
    val isOpenDateValid = openDateErrorResId == null
    val isBuyDateValid = buyDateErrorResId == null && buyDate.isNotEmpty()
    val isFoodNameValid = foodNameErrorResId == null
    val isAmountValid = amountErrorResId == null

    val expiryTimestamp = formatDateToTimestamp(expireDate)
    val openTimestamp = if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
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
              barcode =
                  if (isSelected) selectedFood!!.foodFacts.barcode
                  else selectedImage?.barcode ?: "",
              quantity = Quantity(amount.toDouble(), unit),
              category = category,
              nutritionFacts =
                  if (isSelected) selectedFood!!.foodFacts.nutritionFacts
                  else selectedImage?.nutritionFacts ?: NutritionFacts(),
              imageUrl =
                  if (isSelected) selectedFood!!.foodFacts.imageUrl
                  else selectedImage?.imageUrl ?: FoodFacts.DEFAULT_IMAGE_URL)
      val newFoodItem =
          FoodItem(
              uid = if (isSelected) selectedFood!!.uid else foodItemRepository.getNewUid(),
              foodFacts = foodFacts,
              location = location,
              expiryDate = expiryTimestamp,
              openDate = openTimestamp,
              buyDate = buyTimestamp,
              status = if (isSelected) selectedFood!!.status else FoodStatus.UNOPENED,
              owner =
                  if (isSelected) selectedFood!!.owner else userRepository.user.value?.uid ?: "")
      if (isSelected) {
        editFoodItem(newFoodItem)
      } else {
        addFoodItem(newFoodItem)
      }
      return true
    } else {
      return false
    }
  }
}
