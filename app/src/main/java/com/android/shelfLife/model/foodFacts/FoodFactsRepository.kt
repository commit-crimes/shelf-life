package com.android.shelfLife.model.foodFacts

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents different types of input for searching food facts.
 *
 * This sealed class can represent either a search by barcode or a search by a query string.
 */
sealed class FoodSearchInput {

    /**
     * Represents a search input based on a barcode.
     *
     * @property barcode The barcode of the food item.
     */
    data class Barcode(val barcode: Long) : FoodSearchInput()

    /**
     * Represents a search input based on a query string.
     *
     * @property searchQuery The string query to search for food items.
     */
    data class Query(val searchQuery: String) : FoodSearchInput()
}

/**
 * Interface for a repository that handles food facts data.
 *
 * Provides methods to search for food facts and manage search state.
 */
interface FoodFactsRepository {

    /**
     * A [StateFlow] that represents the current status of the food facts search.
     */
    val searchStatus: StateFlow<SearchStatus>

    /**
     * Resets the search status to its default state.
     */
    fun resetSearchStatus()

    /**
     * A [StateFlow] that provides a list of food facts suggestions.
     */
    val foodFactsSuggestions: StateFlow<List<FoodFacts>>

    /**
     * Searches for food facts based on the provided search input.
     *
     * @param searchInput The search input to use for querying food facts (either barcode or query).
     * @param onSuccess A callback function invoked with a list of [FoodFacts] on successful search.
     * @param onFailure A callback function invoked with an [Exception] on search failure.
     */
    fun searchFoodFacts(
        searchInput: FoodSearchInput,
        onSuccess: (List<FoodFacts>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Searches for food facts using a specific barcode.
     *
     * @param barcode The barcode of the food item to search for.
     */
    fun searchByBarcode(barcode: Long)

    /**
     * Searches for food facts using a specific query string.
     *
     * @param query The query string to search for food items.
     */
    fun searchByQuery(query: String)
}