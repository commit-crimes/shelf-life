package com.android.shelfLife.model.foodFacts

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
}
