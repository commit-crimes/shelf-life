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
 * ViewModel for managing the state and actions related to an individual food item.
 *
 * This ViewModel is responsible for:
 * - Tracking the currently selected food item.
 * - Performing operations such as deleting the selected food item.
 *
 * @param foodItemRepository Repository for managing food item data.
 * @param userRepository Repository for managing user-related data.
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
    // Initialize the selected food item from the repository.
    selectedFood = foodItemRepository.selectedFoodItem.value
  }

  /**
   * Deletes the currently selected food item from the repository.
   *
   * This function removes the food item from the repository and clears the selected food item state.
   * It ensures that the user is associated with a household before attempting to delete the item.
   *
   * @throws NullPointerException if no food item is currently selected.
   */
  suspend fun deleteFoodItem() {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null) {
      // Delete the selected food item and reset the selection in the repository.
      foodItemRepository.deleteFoodItem(householdId, selectedFood!!.uid)
      foodItemRepository.selectFoodItem(null)
    }
  }
}