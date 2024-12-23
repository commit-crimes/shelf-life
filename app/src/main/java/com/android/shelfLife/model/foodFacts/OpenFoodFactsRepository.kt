package com.android.shelfLife.model.foodFacts

import android.util.Log
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

/** Represents the different states of a search operation. */
sealed class SearchStatus {
  /** Represents the idle state of the search. */
  data object Idle : SearchStatus()

  /** Represents the loading state of the search. */
  data object Loading : SearchStatus()

  /** Represents the successful completion of the search. */
  data object Success : SearchStatus()

  /** Represents the failure of the search. */
  data object Failure : SearchStatus()
}

/**
 * Repository class for interacting with the OpenFoodFacts API.
 *
 * @property client The OkHttpClient used for making network requests.
 * @property baseUrl The base URL of the OpenFoodFacts API.
 */
class OpenFoodFactsRepository(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://world.openfoodfacts.net"
) : FoodFactsRepository {

  companion object {
    private const val MAX_RESULTS = 7 // Adjust the number of results as needed
  }

  private val _searchStatus = MutableStateFlow<SearchStatus>(SearchStatus.Idle)
  override val searchStatus: StateFlow<SearchStatus> = _searchStatus

  private val _foodFactsSuggestions = MutableStateFlow<List<FoodFacts>>(emptyList())
  override val foodFactsSuggestions: StateFlow<List<FoodFacts>> = _foodFactsSuggestions

  /** Resets the search status to idle. */
  override fun resetSearchStatus() {
    _searchStatus.value = SearchStatus.Idle
  }

  /** Sets the search status to failure. */
  override fun setFailureStatus() {
    _searchStatus.value = SearchStatus.Failure
  }

  /**
   * Searches for food facts by barcode.
   *
   * @param barcode The barcode of the food item to search for.
   */
  override fun searchByBarcode(barcode: Long) {
    searchFoodFacts(
        searchInput = FoodSearchInput.Barcode(barcode),
        onSuccess = { foodFactsList -> _foodFactsSuggestions.value = foodFactsList },
        onFailure = {
          _searchStatus.value = SearchStatus.Failure
          _foodFactsSuggestions.value = emptyList()
        })
  }

  /**
   * Searches for food facts by query string.
   *
   * @param newQuery The query string to search for.
   */
  override fun searchByQuery(newQuery: String) {
    searchFoodFacts(
        FoodSearchInput.Query(newQuery),
        onSuccess = { foodFactsList ->
          // Filter out items without images
          val filteredList = foodFactsList.filter { it.imageUrl.isNotEmpty() }
          _foodFactsSuggestions.value = filteredList
        },
        onFailure = {
          _searchStatus.value = SearchStatus.Failure
          _foodFactsSuggestions.value = emptyList()
        })
  }

  /**
   * Builds a URL from the given path segments.
   *
   * @param paths The path segments to join into a URL.
   * @return The constructed URL string.
   */
  private fun buildUrl(vararg paths: String): String {
    return paths.joinToString("/") { it.trim('/') }
  }

  /**
   * Searches for food facts based on the given search input.
   *
   * @param searchInput The search input (barcode or query).
   * @param onSuccess Callback function to handle successful search results.
   * @param onFailure Callback function to handle search failures.
   */
  override fun searchFoodFacts(
      searchInput: FoodSearchInput,
      onSuccess: (List<FoodFacts>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    _searchStatus.value = SearchStatus.Loading
    val url =
        when (searchInput) {
          is FoodSearchInput.Barcode ->
              buildUrl(baseUrl, "api/v0/product/${searchInput.barcode}.json")
          is FoodSearchInput.Query ->
              buildUrl(
                  baseUrl,
                  "cgi/search.pl?search_terms=${searchInput.searchQuery}&page_size=$MAX_RESULTS&json=true")
        }

    val request = Request.Builder().url(url).build()

    client
        .newCall(request)
        .enqueue(
            object : okhttp3.Callback {
              override fun onFailure(call: okhttp3.Call, e: IOException) {
                _searchStatus.value = SearchStatus.Failure
                onFailure(e)
              }

              override fun onResponse(call: okhttp3.Call, response: Response) {
                if (!response.isSuccessful) {
                  _searchStatus.value = SearchStatus.Failure
                  onFailure(
                      IOException("Server error: HTTP ${response.code} - ${response.message}"))
                  return
                }

                val body = response.body?.string() ?: ""
                val foodFactsList = parseFoodFactsResponse(body, searchInput)

                Log.d("OpenFoodFactsRepository", "Food Facts: $foodFactsList")
                _searchStatus.value = SearchStatus.Success
                onSuccess(foodFactsList)
              }
            })
  }

  /**
   * Parses the JSON response from the OpenFoodFacts API into a list of FoodFacts objects. Handles
   * both barcode and query responses.
   *
   * @param responseBody The JSON response string from the API.
   * @param searchInput The original search input used to determine the type of parsing.
   * @return A list of FoodFacts objects parsed from the response.
   */
  fun parseFoodFactsResponse(responseBody: String, searchInput: FoodSearchInput): List<FoodFacts> {
    return when (searchInput) {
      is FoodSearchInput.Barcode -> parseBarcodeResponse(responseBody)
      is FoodSearchInput.Query -> parseQueryResponse(responseBody)
    }
  }

  /** Parses the response for a barcode search into a list containing a single FoodFacts object. */
  fun parseBarcodeResponse(responseBody: String): List<FoodFacts> {
    val jsonObject = JSONObject(responseBody)
    val productObject = jsonObject.optJSONObject("product")

    return if (productObject != null) {
      val foodFacts = extractFoodFactsFromJson(productObject)
      listOf(foodFacts)
    } else {
      emptyList()
    }
  }

  /** Parses the response for a query search into a list of FoodFacts objects. */
  fun parseQueryResponse(responseBody: String): List<FoodFacts> {
    val jsonObject = JSONObject(responseBody)
    val productsArray = jsonObject.optJSONArray("products") ?: JSONArray()
    val foodFactsList = mutableListOf<FoodFacts>()

    for (i in 0 until productsArray.length().coerceAtMost(MAX_RESULTS)) {
      val productObject = productsArray.getJSONObject(i)
      val foodFacts = extractFoodFactsFromJson(productObject)
      foodFactsList.add(foodFacts)
    }

    return foodFactsList
  }

  /**
   * Extracts FoodFacts details from a JSON object and maps them to the FoodFacts data class.
   *
   * @param productObject The JSON object representing a product.
   * @return The FoodFacts object extracted from the JSON object.
   */
  fun extractFoodFactsFromJson(productObject: JSONObject): FoodFacts {
    val name = productObject.optString("product_name", "Unknown Product")
    val barcode = productObject.optString("code", "")
    val imageUrl = productObject.optString("image_url", FoodFacts.DEFAULT_IMAGE_URL)

    val quantityString = productObject.optString("quantity", "1")
    Log.d("OpenFoodFactsRepository", "Quantity String: $quantityString")
    val quantity = parseOpenFoodFactsQuantity(quantityString)

    // Map nutrition facts
    val nutritionFacts =
        NutritionFacts(
            energyKcal = productObject.optJSONObject("nutriments")?.optInt("energy-kcal_100g") ?: 0,
            fat = productObject.optJSONObject("nutriments")?.optDouble("fat_100g") ?: 0.0,
            saturatedFat =
                productObject.optJSONObject("nutriments")?.optDouble("saturated-fat_100g") ?: 0.0,
            carbohydrates =
                productObject.optJSONObject("nutriments")?.optDouble("carbohydrates_100g") ?: 0.0,
            sugars = productObject.optJSONObject("nutriments")?.optDouble("sugars_100g") ?: 0.0,
            proteins = productObject.optJSONObject("nutriments")?.optDouble("proteins_100g") ?: 0.0,
            salt = productObject.optJSONObject("nutriments")?.optDouble("salt_100g") ?: 0.0)

    val foodCategory =
        FoodCategory.OTHER // You can refine the logic to determine the category if needed

    // Create and return the FoodFacts object
    return FoodFacts(
        name = name,
        barcode = barcode,
        quantity = quantity,
        category = foodCategory,
        nutritionFacts = nutritionFacts,
        imageUrl = imageUrl)
  }

  /**
   * Parses the quantity string from OpenFoodFacts and converts it to a Quantity object.
   *
   * @param quantityString The quantity string from OpenFoodFacts.
   * @return The parsed Quantity object.
   */
  fun parseOpenFoodFactsQuantity(quantityString: String): Quantity {
    // Normalize the string (remove leading/trailing spaces, and convert to lowercase)
    val normalizedString = quantityString.trim().lowercase()

    // Regex to match a number (integer or decimal) followed by an optional space and unit
    val regex = Regex("""(\d+(\.\d+)?)(\s*)?(g|kg|ml|l|dl|cl)""")

    val matchResult = regex.find(normalizedString)
    return if (matchResult != null) {
      val (amountString, _, _, unitString) = matchResult.destructured

      val amount = amountString.toDoubleOrNull() ?: 1.0 // Fallback to 1.0 if parsing fails
      val unit =
          when (unitString) {
            "g" -> FoodUnit.GRAM
            "kg" ->
                FoodUnit.GRAM.also {
                  return Quantity(amount * 1000, it)
                } // Convert kg -> g
            "ml" -> FoodUnit.ML
            "l" ->
                FoodUnit.ML.also {
                  return Quantity(amount * 1000, it)
                } // Convert l -> ml
            "dl" ->
                FoodUnit.ML.also {
                  return Quantity(amount * 100, it)
                } // Convert dl -> ml
            "cl" ->
                FoodUnit.ML.also {
                  return Quantity(amount * 10, it)
                } // Convert cl -> ml
            else -> FoodUnit.COUNT // Default fallback
          }

      Quantity(amount, unit)
    } else {
      // Default quantity in case of invalid input
      Quantity(1.0, FoodUnit.COUNT)
    }
  }
}
