package com.android.shelfLife.model.foodFacts

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class OpenFoodFactsRepository(private val client: OkHttpClient, private val baseUrl: String = "https://world.openfoodfacts.net") : FoodFactsRepository {

    private val MAX_RESULTS = 7  // Adjust the number of results as needed

    override fun searchFoodFacts(
        searchInput: FoodSearchInput,
        onSuccess: (List<FoodFacts>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val url = when (searchInput) {
            is FoodSearchInput.Barcode -> "$baseUrl/api/v0/product/${searchInput.barcode}.json"
            is FoodSearchInput.Query -> "$baseUrl/cgi/search.pl?search_terms=${searchInput.searchQuery}&page_size=$MAX_RESULTS&json=true"
        }

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onFailure(e)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (!response.isSuccessful) {
                    onFailure(IOException("Server error: HTTP ${response.code} - ${response.message}"))
                    return
                }

                val body = response.body?.string() ?: ""
                val foodFactsList = parseFoodFactsResponse(body, searchInput)
                Log.d("OpenFoodFactsRepository", "Food Facts: $foodFactsList")

                onSuccess(foodFactsList)
            }
        })
    }

    /**
     * Parses the JSON response from the OpenFoodFacts API into a list of FoodFacts objects.
     * Handles both barcode and query responses.
     * @param responseBody The JSON response string from the API.
     * @param searchInput The original search input used to determine the type of parsing.
     * @return A list of FoodFacts objects parsed from the response.
     */
    private fun parseFoodFactsResponse(responseBody: String, searchInput: FoodSearchInput): List<FoodFacts> {
        return when (searchInput) {
            is FoodSearchInput.Barcode -> parseBarcodeResponse(responseBody)
            is FoodSearchInput.Query -> parseQueryResponse(responseBody)
        }
    }

    /**
     * Parses the response for a barcode search into a list containing a single FoodFacts object.
     */
    private fun parseBarcodeResponse(responseBody: String): List<FoodFacts> {
        val jsonObject = JSONObject(responseBody)
        val productObject = jsonObject.optJSONObject("product")

        return if (productObject != null) {
            val foodFacts = extractFoodFactsFromJson(productObject)
            listOf(foodFacts)
        } else {
            emptyList()
        }
    }

    /**
     * Parses the response for a query search into a list of FoodFacts objects.
     */
    private fun parseQueryResponse(responseBody: String): List<FoodFacts> {
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
     */
    private fun extractFoodFactsFromJson(productObject: JSONObject): FoodFacts {
        val name = productObject.optString("product_name", "Unknown Product")
        val barcode = productObject.optString("code", "")
        val quantity = Quantity(amount = 1.0)  // Assuming default quantity, adjust as needed

        // Map nutrition facts
        val nutritionFacts = NutritionFacts(
            energyKcal = productObject.optJSONObject("nutriments")?.optInt("energy-kcal_100g") ?: 0,
            fat = productObject.optJSONObject("nutriments")?.optDouble("fat_100g") ?: 0.0,
            saturatedFat = productObject.optJSONObject("nutriments")?.optDouble("saturated-fat_100g") ?: 0.0,
            carbohydrates = productObject.optJSONObject("nutriments")?.optDouble("carbohydrates_100g") ?: 0.0,
            sugars = productObject.optJSONObject("nutriments")?.optDouble("sugars_100g") ?: 0.0,
            proteins = productObject.optJSONObject("nutriments")?.optDouble("proteins_100g") ?: 0.0,
            salt = productObject.optJSONObject("nutriments")?.optDouble("salt_100g") ?: 0.0
        )

        val foodCategory = FoodCategory.OTHER  // You can refine the logic to determine the category if needed

        // Create and return the FoodFacts object
        return FoodFacts(
            name = name,
            barcode = barcode,
            quantity = quantity,
            category = foodCategory,
            nutritionFacts = nutritionFacts
        )
    }
}
