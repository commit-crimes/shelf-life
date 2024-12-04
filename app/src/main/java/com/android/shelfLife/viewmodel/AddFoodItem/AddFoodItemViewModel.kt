package com.android.shelfLife.viewmodel.AddFoodItem

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.newFoodItem.FoodStatus
import com.android.shelfLife.model.newFoodItem.FoodStorageLocation
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.utils.formatDateToTimestamp
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.ui.utils.validateAmount
import com.android.shelfLife.ui.utils.validateBuyDate
import com.android.shelfLife.ui.utils.validateExpireDate
import com.android.shelfLife.ui.utils.validateFoodName
import com.android.shelfLife.ui.utils.validateOpenDate
import com.google.firebase.Timestamp

class AddFoodItemViewModel(
    private val foodItemRepository: FoodItemRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var foodName = ""
    var amount = ""
    var unit = FoodUnit.GRAM
    var category = FoodCategory.OTHER
    var location = FoodStorageLocation.PANTRY
    var expireDate = ""
    var openDate = ""
    var buyDate = formatTimestampToDate(Timestamp.now())

    var foodNameErrorResId : Int? = null
    var amountErrorResId : Int? = null
    var expireDateErrorResId : Int? = null
    var openDateErrorResId : Int? = null
    var buyDateErrorResId : Int? = null

    var unitExpanded = false
    var categoryExpanded = false
    var locationExpanded = false
    var selectedImage : FoodFacts? = null

    /** Validates all fields when the submit button is clicked. */
    fun validateAllFieldsWhenSubmitButton() {
        foodNameErrorResId = validateFoodName(foodName)
        amountErrorResId = validateAmount(amount)
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

    fun changeFoodName(newFoodName: String) {
        foodName = newFoodName
        foodNameErrorResId = validateFoodName(foodName)
    }

    fun changeAmount(newAmount: String) {
        amount = newAmount
        amountErrorResId = validateAmount(amount)
    }

    fun changeExpiryDate(newDate: String) {
        expireDate = newDate.filter { it.isDigit() }
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
    }

    fun changeOpenDate(newDate: String) {
        openDate = newDate.filter { it.isDigit() }
        openDateErrorResId =
            validateOpenDate(
                openDate,
                buyDate,
                buyDateErrorResId,
                expireDate,
                expireDateErrorResId)
    }

    fun changeBuyDate(newDate: String) {
        buyDate = newDate.filter { it.isDigit() }
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
    }

    suspend fun submitFoodItem() : Boolean{
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
                    uid = foodItemRepository.getNewUid(),
                    foodFacts = foodFacts,
                    location = location,
                    expiryDate = expiryTimestamp,
                    openDate = openTimestamp,
                    buyDate = buyTimestamp,
                    status = FoodStatus.UNOPENED,
                    owner = userRepository.user.value?.uid ?: ""
                )
            addFoodItem(newFoodItem)
            return true
        } else {
            return false
        }
    }
}