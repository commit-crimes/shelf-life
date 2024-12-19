package com.android.shelfLife.viewmodel.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * ViewModel for handling deletion confirmation logic.
 *
 * @property houseHoldRepository Repository for household data.
 * @property userRepository Repository for user data.
 * @property foodItemRepository Repository for food item data.
 */
@HiltViewModel
class DeletionConfirmationViewModel
@Inject
constructor(
  private val houseHoldRepository: HouseHoldRepository,
  private val userRepository: UserRepository,
  private val foodItemRepository: FoodItemRepository
) : ViewModel() {

  /** The household currently being edited. */
  val householdToEdit = houseHoldRepository.householdToEdit

  /** The currently selected household. */
  val selectedHousehold = houseHoldRepository.selectedHousehold

  /** List of all households. */
  val households = houseHoldRepository.households


  /**
   * Deletes a household by its unique ID and refreshes the household list.
   *
   * @param householdId - The unique ID of the household to delete.
   */
  fun deleteHouseholdById(householdId: String) {
    val household = households.value.find { it.uid == householdId }
    if (household != null) {
      val houseHoldIndex = households.value.indexOf(household)
      if (household.members.size > 1) {
        houseHoldRepository.updateHousehold(
            household.copy(members = household.members - userRepository.user.value!!.uid)) {
                householdUID ->
              userRepository.deleteHouseholdUID(householdUID)
            }
        houseHoldRepository.deleteHouseholdFromLocalList(household.uid)
      } else {
        houseHoldRepository.deleteHouseholdById(householdId) { householdUID ->
          userRepository.deleteHouseholdUID(householdUID)
          foodItemRepository.deleteHouseholdDocument(householdId)
        }
      }
      if (selectedHousehold.value?.uid == householdId) {
        val householdToSelect =
            households.value.getOrElse(houseHoldIndex) { households.value.lastOrNull() }
        houseHoldRepository.selectHousehold(householdToSelect)
        userRepository.selectHousehold(householdToSelect?.uid)
        if (householdToSelect != null) {
          viewModelScope.launch { foodItemRepository.getFoodItems(householdToSelect.uid) }
        }
      }
    }
  }
}
