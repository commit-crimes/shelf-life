package com.android.shelfLife.viewmodel.overview

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodFacts.SearchStatus
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

/**
 * ViewModel for managing food items.
 *
 * @property foodItemRepository Repository for accessing food item data.
 * @property userRepository Repository for accessing user data.
 * @property foodFactsRepository Repository for accessing food facts data.
 */
@HiltViewModel
class FoodItemViewModel
@Inject
constructor(
    private val foodItemRepository: FoodItemRepository,
    private val userRepository: UserRepository,
    private val foodFactsRepository: FoodFactsRepository
) : ViewModel() {

  var selectedFood by mutableStateOf<FoodItem?>(null)
  var isSelected by mutableStateOf(false)
  var isScanned by mutableStateOf(false)
  var isQuickAdd by mutableStateOf(false)
  val searchStatus: StateFlow<SearchStatus> = foodFactsRepository.searchStatus
  val foodFactsSuggestions: StateFlow<List<FoodFacts>> = foodFactsRepository.foodFactsSuggestions
  var isLoading by mutableStateOf(false)
  var capturedImageUri by mutableStateOf<Uri?>(null)

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
      selectedImage = selectedFood!!.foodFacts
    } else {
      isSelected = false
    }
  }

  /** Marks the food item as scanned. */
  fun isScanned() {
    isScanned = true
  }

  /** Validates all fields when the submit button is clicked. */
  fun validateAllFieldsWhenSubmitButton() {
    if (!isSelected && !isScanned) {
      foodNameErrorResId = validateString(foodName)
    }
    if (!isScanned) {
      amountErrorResId = validateNumber(amount)
    }
    buyDateErrorResId = validateBuyDate(buyDate)
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  /**
   * Adds a new food item to the repository.
   *
   * @param foodItem The food item to add.
   */
  suspend fun addFoodItem(foodItem: FoodItem) {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null) {
      foodItemRepository.addFoodItem(householdId, foodItem)
    }
  }

  /**
   * Edits an existing food item in the repository.
   *
   * @param foodItem The food item to edit.
   */
  suspend fun editFoodItem(foodItem: FoodItem) {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null) {
      foodItemRepository.updateFoodItem(householdId, foodItem)
    }
  }

  /** Deletes the selected food item from the repository. */
  suspend fun deleteFoodItem() {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null) {
      foodItemRepository.deleteFoodItem(householdId, selectedFood!!.uid)
      foodItemRepository.selectFoodItem(null)
    }
  }

  /**
   * Changes the food name and validates it.
   *
   * @param newFoodName The new food name.
   */
  fun changeFoodName(newFoodName: String) {
    foodName = newFoodName
    foodNameErrorResId = validateString(foodName)
  }

  /**
   * Changes the amount and validates it.
   *
   * @param newAmount The new amount.
   */
  fun changeAmount(newAmount: String) {
    amount = newAmount
    amountErrorResId = validateNumber(amount)
  }

  /**
   * Changes the expiry date and validates it.
   *
   * @param newDate The new expiry date.
   */
  fun changeExpiryDate(newDate: String) {
    expireDate = newDate.filter { it.isDigit() }
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    // Re-validate Open Date since it depends on Expire Date
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  /**
   * Changes the open date and validates it.
   *
   * @param newDate The new open date.
   */
  fun changeOpenDate(newDate: String) {
    openDate = newDate.filter { it.isDigit() }
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  /**
   * Changes the buy date and validates it.
   *
   * @param newDate The new buy date.
   */
  fun changeBuyDate(newDate: String) {
    buyDate = newDate.filter { it.isDigit() }
    buyDateErrorResId = validateBuyDate(buyDate)
    // Re-validate Expire Date and Open Date since they depend on Buy Date
    expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
    openDateErrorResId =
        validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
  }

  /**
   * Submits the food name after validation.
   *
   * @return Boolean indicating if the food name is valid.
   */
  suspend fun submbitFoodName(): Boolean {
    foodNameErrorResId = validateString(foodName)
    val isFoodNameValid = foodNameErrorResId == null
    if (isFoodNameValid) {
      return true
    } else {
      return false
    }
  }

  /**
   * Submits the food item after validation.
   *
   * @param scannedFoodFacts Optional scanned food facts.
   * @return Boolean indicating if the food item is valid.
   */
  suspend fun submitFoodItem(scannedFoodFacts: FoodFacts? = null): Boolean {
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
          if (isScanned) {
            scannedFoodFacts!!
          } else if (!isQuickAdd) {
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
          } else {
            selectedImage
          }
      val newFoodItem =
          FoodItem(
              uid =
                  if (isSelected && !foodItemRepository.isQuickAdd.first()) selectedFood!!.uid
                  else foodItemRepository.getNewUid(),
              foodFacts = foodFacts!!,
              location = location,
              expiryDate = expiryTimestamp,
              openDate = openTimestamp,
              buyDate = buyTimestamp,
              status =
                  if (isSelected && !foodItemRepository.isQuickAdd.first()) selectedFood!!.status
                  else FoodStatus.UNOPENED,
              owner =
                  if (isSelected && !foodItemRepository.isQuickAdd.first()) selectedFood!!.owner
                  else userRepository.user.value?.uid ?: "")

      resetSearchStatus()
      if (isSelected) {
        if (foodItemRepository.isQuickAdd.first()) {
          foodItemRepository.selectFoodItem(null)
          addFoodItem(newFoodItem)
        } else {
          foodItemRepository.selectFoodItem(null)
          foodItemRepository.setisQuickAdd(false)
          editFoodItem(newFoodItem)
        }
      } else {
        addFoodItem(newFoodItem)
      }
      return true
    } else {
      return false
    }
  }

  /** Resets the search status. */
  fun resetSearchStatus() {
    foodFactsRepository.resetSearchStatus()
  }

  /**
   * Searches for food facts by query.
   *
   * @param query The search query.
   */
  fun searchByQuery(query: String) {
    foodFactsRepository.searchByQuery(query)
  }

  /** Resets the selected food item. */
  fun resetSelectFoodItem() {
    foodItemRepository.selectFoodItem(
        FoodItem(
            uid = "",
            foodFacts = FoodFacts(name = "", quantity = Quantity(0.0, FoodUnit.GRAM)),
            owner = ""))
  }

  /**
   * Sets the quick add flag.
   *
   * @param isQuickAdd Boolean indicating if quick add is enabled.
   */
  fun setIsQuickAdd(isQuickAdd: Boolean) {
    foodItemRepository.setisQuickAdd(isQuickAdd)
  }

  /**
   * Sets the selected food item.
   *
   * @param foodItem The food item to select.
   */
  fun setFoodItem(foodItem: FoodItem?) {
    foodItemRepository.selectFoodItem(foodItem)
  }

  /**
   * Gets the quick add flag.
   *
   * @return Boolean indicating if quick add is enabled.
   */
  fun getIsQuickAdd(): Boolean {
    return foodItemRepository.isQuickAdd.value ?: false
  }

  /**
   * Uploads an image to Firebase Storage.
   *
   * @param uri The URI of the image.
   * @param context The context.
   * @return The URL of the uploaded image.
   */
  suspend fun uploadImageToFirebaseStorage(uri: Uri, context: Context): String? {
    return foodItemRepository.uploadImageToFirebaseStorage(uri, context)
  }

  /** Resets the fields for the scanner. */
  fun resetForScanner() {
    location = FoodStorageLocation.PANTRY
    expireDate = ""
    openDate = ""
    buyDate = formatTimestampToDate(Timestamp.now())
    expireDateErrorResId = null
    openDateErrorResId = null
    buyDateErrorResId = null
    locationExpanded = false
  }
}
