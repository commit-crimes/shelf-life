package com.android.shelfLife.model.foodFacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoodFactsViewModel(private val repository: FoodFactsRepository) : ViewModel() {

    // StateFlow to store the search query entered by the user
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    // StateFlow to store the list of FoodFacts suggestions from the repository
    private val _foodFactsSuggestions = MutableStateFlow<List<FoodFacts>>(emptyList())
    val foodFactsSuggestions: StateFlow<List<FoodFacts>> = _foodFactsSuggestions

    // Function to set a new query and trigger a search using a query string
    fun searchByQuery(newQuery: String) {
        _query.update { newQuery }
        searchFoodFacts(FoodSearchInput.Query(newQuery))
    }

    // Function to search using a barcode input
    fun searchByBarcode(barcode: Long) {
        searchFoodFacts(FoodSearchInput.Barcode(barcode))
    }

    // Private function to search FoodFacts using the repository with either Query or Barcode input
    private fun searchFoodFacts(searchInput: FoodSearchInput) {
        viewModelScope.launch {
            repository.searchFoodFacts(
                searchInput = searchInput,
                onSuccess = { foodFactsList -> _foodFactsSuggestions.update { foodFactsList } },
                onFailure = { _foodFactsSuggestions.update { emptyList() } }
            )
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
