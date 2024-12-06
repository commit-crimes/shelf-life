package com.android.shelfLife.model.foodFacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SearchStatus {
  data object Idle : SearchStatus()

  data object Loading : SearchStatus()

  data object Success : SearchStatus()

  data object Failure : SearchStatus()
}

class FoodFactsViewModel(private val repository: FoodFactsRepository) : ViewModel() {

  private val _searchStatus = MutableStateFlow<SearchStatus>(SearchStatus.Idle)
  val searchStatus: StateFlow<SearchStatus> = _searchStatus

  // Existing properties
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  private val _foodFactsSuggestions = MutableStateFlow<List<FoodFacts>>(emptyList())
  val foodFactsSuggestions: StateFlow<List<FoodFacts>> = _foodFactsSuggestions

  // Modified search function
  fun searchByBarcode(barcode: Long) {
    viewModelScope.launch {
      _searchStatus.value = SearchStatus.Loading
      repository.searchFoodFacts(
          searchInput = FoodSearchInput.Barcode(barcode),
          onSuccess = { foodFactsList ->
            _foodFactsSuggestions.value = foodFactsList
            _searchStatus.value = SearchStatus.Success
          },
          onFailure = {
            _foodFactsSuggestions.value = emptyList()
            _searchStatus.value = SearchStatus.Failure
          })
    }
  }

  fun resetSearchStatus() {
    _searchStatus.value = SearchStatus.Idle
  }

  fun clearFoodFactsSuggestions() {
    _foodFactsSuggestions.value = emptyList()
  }

  // Function to set a new query and trigger a search using a query string
  fun searchByQuery(newQuery: String) {
    _query.update { newQuery }
    viewModelScope.launch {
        _searchStatus.value = SearchStatus.Loading
      repository.searchFoodFacts(
          FoodSearchInput.Query(newQuery),
          onSuccess = { foodFactsList ->
            // Filter out items without images
            val filteredList = foodFactsList.filter { it.imageUrl.isNotEmpty() }
            _foodFactsSuggestions.value = foodFactsList
              _searchStatus.value = SearchStatus.Success
          },
          onFailure = { _foodFactsSuggestions.value = emptyList()
              _searchStatus.value = SearchStatus.Failure
          })
    }
  }

  // Private function to search FoodFacts using the repository with either Query or Barcode input
  private fun searchFoodFacts(searchInput: FoodSearchInput) {
    viewModelScope.launch {
      repository.searchFoodFacts(
          searchInput = searchInput,
          onSuccess = { foodFactsList -> _foodFactsSuggestions.update { foodFactsList } },
          onFailure = { _foodFactsSuggestions.update { emptyList() } })
    }
  }

  companion object {
    fun Factory(repository: FoodFactsRepository): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FoodFactsViewModel::class.java)) {
              return FoodFactsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }
  }
}
