package com.android.shelfLife.model.foodItem

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing a list of FoodItems.
 *
 * @property repository The repository for managing FoodItems.
 */
open class ListFoodItemsViewModel(private val repository: FoodItemRepository) : ViewModel() {
  private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
  val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

  // Selected food item for detail view
  private val _selectedFoodItem = MutableStateFlow<FoodItem?>(null)
  val selectedFoodItem: StateFlow<FoodItem?> = _selectedFoodItem.asStateFlow()

  /**
   * Initializes the ListFoodItemsViewModel by loading the list of FoodItems from the repository.
   */
  init {
    repository.init(
        onSuccess = {
          // getFoodItems()
        })
  }

  /**
   * Handles a failure in fetching FoodItems.
   *
   * @param exception The exception that occurred.
   */
  private fun _onFail(exception: Exception) {
    // TODO: proper error Handling (use a global Error PopUp?)
    Log.e("ListFoodItemsViewModel", "Error fetching FoodItems: $exception")
  }

  fun getUID(): String {
    return repository.getNewUid()
  }

  /** Gets all FoodItem documents */
  fun getAllFoodItems() {
    repository.getFoodItems(onSuccess = { _foodItems.value = it }, onFailure = ::_onFail)
  }

  /** Directly updates the list of FoodItems */
  fun setFoodItems(foodItems: List<FoodItem>) {
    _foodItems.value = foodItems
  }

  /** Adds a FoodItem document */
  fun addFoodItem(foodItem: FoodItem) {
    repository.addFoodItem(
        foodItem = foodItem, onSuccess = { getAllFoodItems() }, onFailure = ::_onFail)
  }

  /** Updates a FoodItem document */
  fun updateFoodItem(foodItem: FoodItem) {
    repository.updateFoodItem(
        foodItem = foodItem, onSuccess = { getAllFoodItems() }, onFailure = ::_onFail)
  }

  /** Deletes a FoodItem document by ID */
  fun deleteFoodItemById(id: String) {
    repository.deleteFoodItemById(id = id, onSuccess = { getAllFoodItems() }, onFailure = ::_onFail)
  }

  /** Selects a FoodItem document */
  fun selectFoodItem(foodItem: FoodItem) {
    _selectedFoodItem.value = foodItem
  }

  // create factory
  companion object {
    fun Factory(repository: FoodItemRepository): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ListFoodItemsViewModel::class.java)) {
              return ListFoodItemsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }
  }
}
