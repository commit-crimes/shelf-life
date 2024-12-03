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
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.ui.utils.validateAmount
import com.android.shelfLife.ui.utils.validateBuyDate
import com.android.shelfLife.ui.utils.validateExpireDate
import com.android.shelfLife.ui.utils.validateFoodName
import com.android.shelfLife.ui.utils.validateOpenDate
import com.google.firebase.Timestamp

class AddFoodItemViewModel(
    private val foodItemRepository: FoodItemRepository,
    private val householdRepository: HouseHoldRepository
) : ViewModel() {
    var foodName = ""
    var amount = ""
    var unit = FoodUnit.GRAM
    var category = FoodCategory.OTHER
    var location = FoodStorageLocation.PANTRY
    var expireDate = ""
    var openDate = ""
    var buyDate = Timestamp.now()

    var foodNameErrorResId : Int? = null
    var amountErrorResId : Int? = null
    var expireDateErrorResId : Int? = null
    var openDateErrorResId : Int? = null
    var buyDateErrorResId : Int? = null

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


    suspend fun addFoodItem(foodItem: FoodItem) {
            val householdId = householdRepository.selectedHousehold.value?.uid
            if (householdId != null) {
                foodItemRepository.addFoodItem(householdId, foodItem)
            }
        }

}