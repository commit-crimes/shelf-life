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
 * ViewModel responsible for managing household deletion and updating related user and food item data.
 *
 * @property houseHoldRepository Repository for household data management.
 * @property userRepository Repository for user-related data management.
 * @property foodItemRepository Repository for food item data management.
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

  /** The household currently selected by the user. */
  val selectedHousehold = houseHoldRepository.selectedHousehold

  /** The list of all households associated with the user. */
  val households = houseHoldRepository.households

  /**
   * Deletes a household by its unique ID and refreshes the household list.
   *
   * If the deleted household is the currently selected one, another household will be selected,
   * or the selection will be cleared if no households remain.
   *
   * @param householdId The unique ID of the household to delete.
   */
  fun deleteHouseholdById(householdId: String) {
    viewModelScope.launch {
      // Remove the household from the repository
      houseHoldRepository.deleteHouseholdById(householdId)

      // Find the position of the deleted household in the list of households
      var currentPosition = 0
      households.value.forEachIndexed { index, household ->
        if (household.uid == householdId) {
          currentPosition = index
          return@forEachIndexed
        }
      }

      // Remove the household ID from the user's list of associated households
      userRepository.deleteHouseholdUID(householdId)

      // Handle the case where the deleted household was selected
      if (selectedHousehold.value == null || householdId == selectedHousehold.value!!.uid) {
        // Select another household if possible, or clear the selection
        houseHoldRepository.selectHousehold(
          if (households.value.isEmpty()) {
            null
          } else {
            if (currentPosition < households.value.size) {
              households.value[currentPosition]
            } else {
              households.value[households.value.size - 1]
            }
          }
        )
        userRepository.selectHousehold(houseHoldRepository.selectedHousehold.value?.uid)

        // Load food items for the newly selected household
        selectedHousehold.value?.uid?.let { foodItemRepository.getFoodItems(it) }
      }
    }
  }
}