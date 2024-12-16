package com.android.shelfLife.viewmodel.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing household selection and interactions within the navigation drawer.
 *
 * This ViewModel handles the selection of households, updates the repositories with the selected
 * household, and retrieves associated food items.
 *
 * @param houseHoldRepository Repository for managing household-related data.
 * @param userRepository Repository for managing user-related data and preferences.
 * @param foodItemRepository Repository for managing food items related to households.
 */
@HiltViewModel
class HouseholdSelectionDrawerViewModel
@Inject
constructor(
  private val houseHoldRepository: HouseHoldRepository,
  private val userRepository: UserRepository,
  private val foodItemRepository: FoodItemRepository
) : ViewModel() {

  /**
   * A flow that observes the list of all households available to the user.
   */
  val households = houseHoldRepository.households

  /**
   * A flow that observes the currently selected household.
   */
  val selectedHousehold = houseHoldRepository.selectedHousehold

  /**
   * Selects a household, updates the selected household in the repositories, and fetches food items.
   *
   * @param household The household to select. If null, no action is taken.
   */
  fun selectHousehold(household: HouseHold?) {
    viewModelScope.launch {
      if (household != null) {
        // Update the selected household in the repositories
        houseHoldRepository.selectHousehold(household)
        userRepository.selectHousehold(household.uid)

        // Fetch the food items associated with the selected household
        foodItemRepository.getFoodItems(household.uid)
      }
    }
  }

  /**
   * Selects a household for editing purposes.
   *
   * @param household The household to set as editable. If null, no action is taken.
   */
  fun selectHouseholdToEdit(household: HouseHold?) {
    viewModelScope.launch {
      houseHoldRepository.selectHouseholdToEdit(household)
    }
  }
}