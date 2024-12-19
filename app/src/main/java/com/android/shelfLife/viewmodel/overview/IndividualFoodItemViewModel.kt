package com.android.shelfLife.viewmodel.overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing individual food items.
 *
 * @property foodItemRepository Repository for accessing food item data.
 * @property userRepository Repository for accessing user data.
 */
@HiltViewModel
class IndividualFoodItemViewModel
@Inject
constructor(
  private val foodItemRepository: FoodItemRepository,
  private val userRepository: UserRepository
) : ViewModel() {

  /** The currently selected food item. */
  var selectedFood by mutableStateOf<FoodItem?>(null)

  init {
    selectedFood = foodItemRepository.selectedFoodItem.value
  }

  /** Unselects the current food item. */
  fun unselectFoodItem() {
    foodItemRepository.selectFoodItem(null)
  }

  /**
   * Sets the quick add flag.
   *
   * @param isQuickAdd Boolean indicating if quick add is enabled.
   */
  fun setIsQuickAdd(isQuickAdd: Boolean) {
    foodItemRepository.setisQuickAdd(isQuickAdd)
  }

  /** Deletes the selected food item from the repository. */
  suspend fun deleteFoodItem() {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null && selectedFood != null) {
      foodItemRepository.deleteFoodItem(householdId, selectedFood!!.uid)
      foodItemRepository.selectFoodItem(null)
    }
  }
}