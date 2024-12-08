package com.android.shelfLife.viewmodel.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
