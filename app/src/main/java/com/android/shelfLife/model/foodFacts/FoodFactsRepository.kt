package com.android.shelfLife.model.foodFacts

// Sealed class to represent different types of search inputs
sealed class FoodSearchInput {
    data class Barcode(val barcode: Long) : FoodSearchInput()
    data class Query(val searchQuery: String) : FoodSearchInput()
}

interface FoodFactsRepository {
    /**
     * Searches for food facts based on the provided search input.
     *
     * @param searchInput The search input to use for querying food facts.
     * @param onSuccess Callback function to handle successful search results.
     * @param onFailure Callback function to handle search failures.
     */
    fun searchFoodFacts(searchInput: FoodSearchInput, onSuccess: (List<FoodFacts>) -> Unit, onFailure: (Exception) -> Unit)
}
