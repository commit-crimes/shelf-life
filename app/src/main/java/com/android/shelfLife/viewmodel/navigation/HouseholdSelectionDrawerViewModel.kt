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

  fun selectHousehold(household: HouseHold?) {
    viewModelScope.launch {
      if (household != null) {
        houseHoldRepository.selectHousehold(household)
        userRepository.selectHousehold(household.uid)
        foodItemRepository.getFoodItems(household.uid)
      }
    }
  }

  fun selectHouseholdToEdit(household: HouseHold?) {
    viewModelScope.launch { houseHoldRepository.selectHouseholdToEdit(household) }
  }
}
