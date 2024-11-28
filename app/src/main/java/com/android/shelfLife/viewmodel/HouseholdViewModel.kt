package com.android.shelfLife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HouseholdViewModel(private val repository: HouseHoldRepository) : ViewModel() {

  // StateFlow to hold the list of households
  private val _households = MutableStateFlow<List<HouseHold>>(emptyList())
  val households: StateFlow<List<HouseHold>> = _households

  // StateFlow to hold the selected household
  private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
  val selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold

  // StateFlow to hold the members of the selected household
  private val _householdMembers = MutableStateFlow<List<String>>(emptyList())
  val householdMembers: StateFlow<List<String>> = _householdMembers

  // Function to load all households
  fun loadHouseholds() {
    viewModelScope.launch {
      // Logic to load households from the repository
    }
  }

  // Function to select a household
  fun selectHousehold(household: HouseHold?) {
    _selectedHousehold.value = household
    loadHouseholdMembers(household?.uid)
  }

  // Function to load members for the selected household
  private fun loadHouseholdMembers(householdId: String?) {
    if (householdId == null) {
      _householdMembers.value = emptyList()
      return
    }

    viewModelScope.launch {
      val members = repository.getHouseholdMembers(householdId)
      _householdMembers.value = members
    }
  }

  // Function to add a new household
  fun addHousehold(name: String, members: List<String>) {
    viewModelScope.launch {
      // Logic to add a new household
    }
  }

  // Function to update an existing household
  fun updateHousehold(household: HouseHold) {
    viewModelScope.launch {
      // Logic to update the household
    }
  }

  // Function to delete a household
  fun deleteHousehold(householdId: String) {
    viewModelScope.launch {
      // Logic to delete the household
    }
  }
}
