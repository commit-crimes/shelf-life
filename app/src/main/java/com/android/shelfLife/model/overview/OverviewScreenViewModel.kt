package com.android.shelfLife.model.overview

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OverviewScreenViewModel() : ViewModel() {
  private val _selectedFilters = MutableStateFlow<List<String>>(emptyList())
  val selectedFilters = _selectedFilters.asStateFlow()

  private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
  val drawerState = _drawerState.asStateFlow()

  val filters = listOf("Dairy", "Meat", "Fish", "Fruit", "Vegetables", "Bread", "Canned")

  /**
   * Toggles the filter on or off
   *
   * @param filter The filter to toggle
   */
  fun toggleFilter(filter: String) {
    if (_selectedFilters.value.contains(filter)) {
      _selectedFilters.update { it - filter }
    } else {
      _selectedFilters.update { it + filter }
    }
  }
}
