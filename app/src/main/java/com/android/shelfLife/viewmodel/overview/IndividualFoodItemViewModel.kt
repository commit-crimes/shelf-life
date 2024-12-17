package com.android.shelfLife.viewmodel.overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IndividualFoodItemViewModel
@Inject
constructor(
    private val foodItemRepository: FoodItemRepository,
    private val userRepository: UserRepository
) : ViewModel() {
  var selectedFood by mutableStateOf<FoodItem?>(null)

  init {
    selectedFood = foodItemRepository.selectedFoodItem.value
  }

  fun unselectFoodItem() {
    foodItemRepository.selectFoodItem(null)
  }

  suspend fun deleteFoodItem() {
    val householdId = userRepository.user.value?.selectedHouseholdUID
    if (householdId != null) {
      foodItemRepository.deleteFoodItem(householdId, selectedFood!!.uid)
      foodItemRepository.selectFoodItem(null)
    }
  }
}
