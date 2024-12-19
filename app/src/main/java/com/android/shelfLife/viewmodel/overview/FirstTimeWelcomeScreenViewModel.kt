package com.android.shelfLife.viewmodel.overview

import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing the first-time welcome screen.
 *
 * @property houseHoldRepository Repository for accessing household data.
 */
@HiltViewModel
class FirstTimeWelcomeScreenViewModel
@Inject
constructor(private val houseHoldRepository: HouseHoldRepository) : ViewModel() {

  /**
   * Selects a household to edit.
   *
   * @param household The household to edit.
   */
  fun selectHouseholdToEdit(household: HouseHold?) {
    houseHoldRepository.selectHouseholdToEdit(household)
  }
}