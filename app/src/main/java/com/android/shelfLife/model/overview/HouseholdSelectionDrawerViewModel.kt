package com.android.shelfLife.model.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import kotlinx.coroutines.launch

class HouseholdSelectionDrawerViewModel(
    private val houseHoldRepository: HouseHoldRepository,
    private val userRepository: UserRepository
) : ViewModel() {
  val households = houseHoldRepository.households
  val selectedHousehold = userRepository.selectedHousehold

  fun selectHousehold(household: HouseHold?) {
    viewModelScope.launch { userRepository.selectHousehold(household) }
  }

  fun selectHouseholdToEdit(household: HouseHold?) {
    viewModelScope.launch { houseHoldRepository.selectHouseholdToEdit(household) }
  }
}
