package com.android.shelfLife.viewmodel.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * ViewModel for managing household selection in the navigation drawer.
 *
 * @property houseHoldRepository Repository for accessing household data.
 * @property userRepository Repository for accessing user data.
 * @property foodItemRepository Repository for accessing food item data.
 */
@HiltViewModel
class HouseholdSelectionDrawerViewModel
@Inject
constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val userRepository: UserRepository,
    private val foodItemRepository: FoodItemRepository
) : ViewModel() {
  val households = houseHoldRepository.households
  val selectedHousehold = houseHoldRepository.selectedHousehold

  /**
   * Selects a household and updates the repositories with the selected household's data.
   *
   * @param household The household to select.
   */
  fun selectHousehold(household: HouseHold?) {
    viewModelScope.launch {
      if (household != null) {
        houseHoldRepository.selectHousehold(household)
        userRepository.selectHousehold(household.uid)
        foodItemRepository.getFoodItems(household.uid)
      }
    }
  }

  /**
   * Selects a household to edit.
   *
   * @param household The household to edit.
   */
  fun selectHouseholdToEdit(household: HouseHold?) {
    viewModelScope.launch { houseHoldRepository.selectHouseholdToEdit(household) }
  }
}
