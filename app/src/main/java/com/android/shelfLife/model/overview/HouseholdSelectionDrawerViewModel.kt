package com.android.shelfLife.model.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import kotlinx.coroutines.launch

class HouseholdSelectionDrawerViewModel(private val houseHoldRepository: HouseHoldRepository) :
    ViewModel() {
  val households = houseHoldRepository.households
  val selectedHousehold = houseHoldRepository.selectedHousehold

  fun selectHousehold(household: HouseHold?) {
    viewModelScope.launch { houseHoldRepository.selectHousehold(household) }
  }

  fun selectHouseholdToEdit(household: HouseHold?) {
    viewModelScope.launch { houseHoldRepository.selectHouseholdToEdit(household) }
  }
}
