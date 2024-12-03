package com.android.shelfLife.model.deletionConfirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import kotlinx.coroutines.launch

class DeletionConfirmationViewModel(private val houseHoldRepository: HouseHoldRepository) :
    ViewModel() {

  val householdToEdit = houseHoldRepository.householdToEdit

  /**
   * Deletes a household by its unique ID and refreshes the household list.
   *
   * @param householdId - The unique ID of the household to delete.
   */
  fun deleteHouseholdById(householdId: String) {
    viewModelScope.launch { houseHoldRepository.deleteHouseholdById(householdId) }
  }
}
