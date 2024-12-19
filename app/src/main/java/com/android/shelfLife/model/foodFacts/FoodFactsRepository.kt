package com.android.shelfLife.model.foodFacts

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a food facts object.
 *
 * Query class contains a search string (ex. name) for the food item.
 *
 * Barcode class contains the barcode of the food item.
 */
sealed class FoodSearchInput {
    // string search query
    data class Barcode(val barcode: Long) : FoodSearchInput()

    data class Query(val searchQuery: String) : FoodSearchInput()
}

interface FoodFactsRepository {

    /**
     * StateFlow representing the current search status.
     */
    val searchStatus: StateFlow<SearchStatus>

    /**
     * Resets the search status to its initial state.
     */
    fun resetSearchStatus()

    /**
     * Sets the search status to a failure state.
     */
    fun setFailureStatus()

    /**
     * StateFlow representing the list of food facts suggestions.
     */
    val foodFactsSuggestions: StateFlow<List<FoodFacts>>

    /**
     * Searches for food facts based on the provided search input.
     *
     * @param searchInput The search input to use for querying food facts.
     * @param onSuccess Callback function to handle successful search results.
     * @param onFailure Callback function to handle search failures.
     */
    fun searchFoodFacts(
        searchInput: FoodSearchInput,
        onSuccess: (List<FoodFacts>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Searches for food facts based on the provided barcode.
     *
     * @param barcode The barcode to use for querying food facts.
     */
    fun searchByBarcode(barcode: Long)

    /**
     * Searches for food facts based on the provided query string.
     *
     * @param query The query string to use for querying food facts.
     */
    fun searchByQuery(query: String)
}