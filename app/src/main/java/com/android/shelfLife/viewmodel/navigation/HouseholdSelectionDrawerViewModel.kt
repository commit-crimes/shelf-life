package com.android.shelfLife.viewmodel.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HouseholdSelectionDrawerViewModel
@Inject
constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val userRepository: UserRepository,
    private val foodItemRepository: FoodItemRepository
) : ViewModel() {
  val households =
      houseHoldRepository.households.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
  val selectedHousehold =
      userRepository.selectedHousehold.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  fun selectHousehold(household: HouseHold?) {
    viewModelScope.launch {
      userRepository.selectHousehold(household)
      if (household != null) {
        foodItemRepository.getFoodItems(household.uid)
      }
    }
  }

  fun selectHouseholdToEdit(household: HouseHold?) {
    viewModelScope.launch { houseHoldRepository.selectHouseholdToEdit(household) }
  }
}
