package com.android.shelfLife.model.foodItem

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ListFoodItemsViewModel(private val repository: FoodItemRepository) : ViewModel() {
    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    // Selected food item for detail view
    private val _selectedFoodItem = MutableStateFlow<FoodItem?>(null)
    val selectedFoodItem: StateFlow<FoodItem?> = _selectedFoodItem.asStateFlow()

    init {
        repository.init(onSuccess = { getFoodItems() })
    }

    // Error handling function
    private fun _onFail(exception: Exception) {
        Log.e("ListFoodItemsViewModel", "Error fetching FoodItems: $exception")
    }

    /** Gets all FoodItem documents */
    fun getFoodItems() {
        repository.getFoodItems(onSuccess = { _foodItems.value = it }, onFailure = ::_onFail)
    }

    /** Adds a FoodItem document */
    fun addFoodItem(foodItem: FoodItem) {
        repository.addFoodItem(
            foodItem = foodItem,
            onSuccess = { getFoodItems() },
            onFailure = ::_onFail
        )
    }

    /** Updates a FoodItem document */
    fun updateFoodItem(foodItem: FoodItem) {
        repository.updateFoodItem(
            foodItem = foodItem,
            onSuccess = { getFoodItems() },
            onFailure = ::_onFail
        )
    }

    /** Deletes a FoodItem document by ID */
    fun deleteFoodItemById(id: String) {
        repository.deleteFoodItemById(
            id = id,
            onSuccess = { getFoodItems() },
            onFailure = ::_onFail
        )
    }

    /** Selects a FoodItem document */
    fun selectFoodItem(foodItem: FoodItem) {
        _selectedFoodItem.value = foodItem
    }

    // create factory
    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ListFoodItemsViewModel(FoodItemRepositoryFirestore(Firebase.firestore)) as T
                }
            }
    }
}
