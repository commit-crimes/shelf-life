package com.android.shelfLife.viewmodel.utils

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

      // Find the position of the deleted household in the list
      var currentPosition = 0
      households.value.forEachIndexed { index, household ->
        if (household.uid == householdId) {
          currentPosition = index
          return@forEachIndexed
        }
      }

      userRepository.deleteHouseholdUID(householdId)
      if (selectedHousehold.value == null || householdId == selectedHousehold.value!!.uid) {
        // If the deleted household was selected, deselect it
        userRepository.selectHousehold(
            if (households.value.isEmpty()) {
              null
            } else {
              if (currentPosition < households.value.size) {
                households.value[currentPosition]
              } else {
                households.value[households.value.size - 1]
              }
            })
      }
    }
  }
}
