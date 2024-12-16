package com.android.shelfLife.viewmodel.overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.utils.*
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing the state and operations related to food items.
 *
 * This ViewModel handles food item creation, editing, deletion, validation, and updates
 * based on user interaction or scanned data.
 *
 * @param foodItemRepository Repository for managing food items in the household.
 * @param userRepository Repository for managing user data.
 */
@HiltViewModel
class FoodItemViewModel
@Inject
constructor(
    private val foodItemRepository: FoodItemRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // State for managing the selected food item
    var selectedFood by mutableStateOf<FoodItem?>(null)

    // Boolean flags for various states
    var isSelected by mutableStateOf(false)
    var isScanned by mutableStateOf(false)

    // User input fields
    var foodName by mutableStateOf("")
    var amount by mutableStateOf("")
    var unit by mutableStateOf(FoodUnit.GRAM)
    var category by mutableStateOf(FoodCategory.OTHER)
    var location by mutableStateOf(FoodStorageLocation.PANTRY)
    var expireDate by mutableStateOf("")
    var openDate by mutableStateOf("")
    var buyDate by mutableStateOf(formatTimestampToDate(Timestamp.now()))

    // Error messages for input validation
    var foodNameErrorResId by mutableStateOf<Int?>(null)
    var amountErrorResId by mutableStateOf<Int?>(null)
    var expireDateErrorResId by mutableStateOf<Int?>(null)
    var openDateErrorResId by mutableStateOf<Int?>(null)
    var buyDateErrorResId by mutableStateOf<Int?>(null)

    // Dropdown state management
    var unitExpanded by mutableStateOf(false)
    var categoryExpanded by mutableStateOf(false)
    var locationExpanded by mutableStateOf(false)
    var selectedImage by mutableStateOf<FoodFacts?>(null)

    init {
        selectedFood = foodItemRepository.selectedFoodItem.value
        selectedFood?.let { foodItem ->
            isSelected = true
            foodName = foodItem.foodFacts.name
            amount = foodItem.foodFacts.quantity.amount.toString()
            unit = foodItem.foodFacts.quantity.unit
            category = foodItem.foodFacts.category
            location = foodItem.location
            expireDate = foodItem.expiryDate?.let { formatTimestampToDate(it) } ?: ""
            openDate = foodItem.openDate?.let { formatTimestampToDate(it) } ?: ""
            buyDate = foodItem.buyDate?.let { formatTimestampToDate(it) } ?: ""
        }
    }

    /**
     * Marks the current item as scanned.
     */
    fun isScanned() {
        isScanned = true
    }

    /**
     * Validates all fields when the user submits the form.
     */
    fun validateAllFieldsWhenSubmitButton() {
        if (!isSelected && !isScanned) {
            foodNameErrorResId = validateString(foodName)
        }
        if (!isScanned) {
            amountErrorResId = validateNumber(amount)
        }
        buyDateErrorResId = validateBuyDate(buyDate)
        expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
        openDateErrorResId = validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
    }

    /**
     * Adds a new food item to the repository.
     *
     * @param foodItem The food item to be added.
     */
    suspend fun addFoodItem(foodItem: FoodItem) {
        userRepository.user.value?.selectedHouseholdUID?.let { householdId ->
            foodItemRepository.addFoodItem(householdId, foodItem)
        }
    }

    /**
     * Updates an existing food item in the repository.
     *
     * @param foodItem The food item to be updated.
     */
    suspend fun editFoodItem(foodItem: FoodItem) {
        userRepository.user.value?.selectedHouseholdUID?.let { householdId ->
            foodItemRepository.updateFoodItem(householdId, foodItem)
        }
    }

    /**
     * Deletes the selected food item from the repository.
     */
    suspend fun deleteFoodItem() {
        userRepository.user.value?.selectedHouseholdUID?.let { householdId ->
            selectedFood?.let { foodItem ->
                foodItemRepository.deleteFoodItem(householdId, foodItem.uid)
                foodItemRepository.selectFoodItem(null)
            }
        }
    }

    // Functions for updating input fields and their validation states

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
        openDateErrorResId = validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
    }

    fun changeOpenDate(newDate: String) {
        openDate = newDate.filter { it.isDigit() }
        openDateErrorResId = validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
    }

    fun changeBuyDate(newDate: String) {
        buyDate = newDate.filter { it.isDigit() }
        buyDateErrorResId = validateBuyDate(buyDate)
        expireDateErrorResId = validateExpireDate(expireDate, buyDate, buyDateErrorResId)
        openDateErrorResId = validateOpenDate(openDate, buyDate, buyDateErrorResId, expireDate, expireDateErrorResId)
    }

    /**
     * Submits the food item after validating all input fields.
     *
     * @param scannedFoodFacts Optional FoodFacts data for scanned food items.
     * @return True if the submission is successful, false otherwise.
     */
    suspend fun submitFoodItem(scannedFoodFacts: FoodFacts? = null): Boolean {
        validateAllFieldsWhenSubmitButton()

        val isValid = listOf(
            expireDateErrorResId == null && expireDate.isNotEmpty(),
            openDateErrorResId == null,
            buyDateErrorResId == null && buyDate.isNotEmpty(),
            foodNameErrorResId == null,
            amountErrorResId == null
        ).all { it }

        if (isValid) {
            val foodFacts = if (isScanned) {
                scannedFoodFacts!!
            } else {
                FoodFacts(
                    name = foodName,
                    barcode = selectedFood?.foodFacts?.barcode ?: selectedImage?.barcode.orEmpty(),
                    quantity = Quantity(amount.toDouble(), unit),
                    category = category,
                    nutritionFacts = selectedFood?.foodFacts?.nutritionFacts ?: selectedImage?.nutritionFacts ?: NutritionFacts(),
                    imageUrl = selectedFood?.foodFacts?.imageUrl ?: selectedImage?.imageUrl ?: FoodFacts.DEFAULT_IMAGE_URL
                )
            }

            val newFoodItem = FoodItem(
                uid = selectedFood?.uid ?: foodItemRepository.getNewUid(),
                foodFacts = foodFacts,
                location = location,
                expiryDate = formatDateToTimestamp(expireDate),
                openDate = if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null,
                buyDate = formatDateToTimestamp(buyDate),
                status = selectedFood?.status ?: FoodStatus.UNOPENED,
                owner = selectedFood?.owner ?: userRepository.user.value?.uid.orEmpty()
            )

            if (isSelected) {
                foodItemRepository.selectFoodItem(newFoodItem)
                editFoodItem(newFoodItem)
            } else {
                addFoodItem(newFoodItem)
            }
            return true
        }
        return false
    }

    /**
     * Resets the form fields for the scanner.
     */
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