package com.android.shelfLife.model.deletionConfirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import kotlinx.coroutines.launch

class DeletionConfirmationViewModel(
    private val houseHoldRepository: HouseHoldRepository,
    private val userRepository: UserRepository
) : ViewModel() {

  val householdToEdit = houseHoldRepository.householdToEdit
  val selectedHousehold = userRepository.selectedHousehold
  val households = houseHoldRepository.households

  /**
   * Deletes a household by its unique ID and refreshes the household list.
   *
   * @param householdId - The unique ID of the household to delete.
   */
  fun deleteHouseholdById(householdId: String) {
    viewModelScope.launch {
      houseHoldRepository.deleteHouseholdById(householdId)
      // Remove the household UID from the user's list of household UIDs
      userRepository.deleteHouseholdUID(householdId)
      if (selectedHousehold.value == null || householdId == selectedHousehold.value!!.uid) {
        // If the deleted household was selected, deselect it
        userRepository.selectHousehold(households.value.firstOrNull())
      }
    }
  }
}
