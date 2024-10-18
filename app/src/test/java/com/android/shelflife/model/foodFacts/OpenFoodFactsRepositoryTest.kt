package com.android.shelflife.model.foodFacts

import com.android.shelfLife.model.foodFacts.FoodSearchInput
import com.android.shelfLife.model.foodFacts.OpenFoodFactsRepository
import java.io.IOException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class OpenFoodFactsRepositoryTest {

  private lateinit var mockWebServer: MockWebServer
  private lateinit var mockClient: OkHttpClient
  private lateinit var repository: OpenFoodFactsRepository

  @Before
  fun setup() {
    mockWebServer = MockWebServer()
    mockClient = OkHttpClient()
    repository = OpenFoodFactsRepository(mockClient, mockWebServer.url("/").toString())
  }

  @After
  fun teardown() {
    mockWebServer.shutdown()
  }

  @Test
  fun `test searchFoodFacts with Barcode returns success`() {
    // Mocking a successful response from the server
    val mockResponse =
        MockResponse()
            .setResponseCode(200)
            .setBody(
                """
                {
                  "product": {
                    "product_name": "Apple Juice",
                    "code": "123456789",
                    "nutriments": {
                      "energy-kcal_100g": 46,
                      "fat_100g": 0.1,
                      "saturated-fat_100g": 0.02,
                      "carbohydrates_100g": 11.0,
                      "sugars_100g": 10.0,
                      "proteins_100g": 0.5,
                      "salt_100g": 0.01
                    }
                  }
                }
            """
                    .trimIndent())
    mockWebServer.enqueue(mockResponse)

    val foodSearchInput = FoodSearchInput.Barcode(123456789L)
    var successCalled = true

    repository.searchFoodFacts(
        searchInput = foodSearchInput,
        onSuccess = { foodFactsList ->
          successCalled = true
          assertEquals(1, foodFactsList.size)
          assertEquals("Apple Juice", foodFactsList[0].name)
        },
        onFailure = { assertTrue("Failure should not be called in success case", false) })

    assertTrue("Success callback should be called", successCalled)
  }

  @Test
  fun `test searchFoodFacts with Query returns success`() {
    // Mocking a successful response for a query search
    val mockResponse =
        MockResponse()
            .setResponseCode(200)
            .setBody(
                """
                {
                  "products": [
                    {
                      "product_name": "Banana",
                      "code": "987654321",
                      "nutriments": {
                        "energy-kcal_100g": 89,
                        "fat_100g": 0.3,
                        "saturated-fat_100g": 0.1,
                        "carbohydrates_100g": 22.8,
                        "sugars_100g": 12.2,
                        "proteins_100g": 1.1,
                        "salt_100g": 0.0
                      }
                    },
                    {
                      "product_name": "Orange Juice",
                      "code": "192837465",
                      "nutriments": {
                        "energy-kcal_100g": 45,
                        "fat_100g": 0.2,
                        "saturated-fat_100g": 0.0,
                        "carbohydrates_100g": 11.3,
                        "sugars_100g": 10.2,
                        "proteins_100g": 0.7,
                        "salt_100g": 0.01
                      }
                    }
                  ]
                }
            """
                    .trimIndent())
    mockWebServer.enqueue(mockResponse)

    val foodSearchInput = FoodSearchInput.Query("fruit")
    var successCalled = true

    repository.searchFoodFacts(
        searchInput = foodSearchInput,
        onSuccess = { foodFactsList ->
          successCalled = true
          assertEquals(2, foodFactsList.size)
          assertEquals("Banana", foodFactsList[0].name)
          assertEquals("Orange Juice", foodFactsList[1].name)
        },
        onFailure = { assertTrue("Failure should not be called in success case", successCalled) })

    assertTrue("Success callback should be called", successCalled)
  }

  @Test
  fun `test searchFoodFacts returns HTTP error`() {
    // Mocking an HTTP error response
    val mockResponse = MockResponse().setResponseCode(500).setBody("Internal Server Error")
    mockWebServer.enqueue(mockResponse)

    val foodSearchInput = FoodSearchInput.Barcode(123456789L)
    var failureCalled = true

    repository.searchFoodFacts(
        searchInput = foodSearchInput,
        onSuccess = { assertTrue("Success should not be called in error case", true) },
        onFailure = { e ->
          failureCalled = true
          assertTrue(e is IOException)
          assertEquals("Server error: HTTP 500 - Internal Server Error", e.message)
        })

    assertTrue("Failure callback should be called", failureCalled)
  }

  @Test
  fun `test searchFoodFacts returns network failure`() {
    // Simulate network failure
    mockWebServer.shutdown() // Simulating no connection

    val foodSearchInput = FoodSearchInput.Barcode(123456789L)
    var failureCalled = true

    repository.searchFoodFacts(
        searchInput = foodSearchInput,
        onSuccess = { assertTrue("Success should not be called in network failure case", false) },
        onFailure = { e ->
          failureCalled = true
          assertTrue(e is IOException)
        })

    assertTrue("Failure callback should be called", failureCalled)
  }

  @Test
  fun `test extractFoodFactsFromJson parses correct data`() {
    // Mocking a JSONObject to represent a product
    val productJson =
        JSONObject(
            """
            {
                "product_name": "Orange Juice",
                "code": "123456789",
                "nutriments": {
                    "energy-kcal_100g": 45,
                    "fat_100g": 0.1,
                    "saturated-fat_100g": 0.02,
                    "carbohydrates_100g": 10.0,
                    "sugars_100g": 9.0,
                    "proteins_100g": 1.0,
                    "salt_100g": 0.01
                }
            }
        """
                .trimIndent())

    val repository = OpenFoodFactsRepository(OkHttpClient())
    val foodFacts = repository.extractFoodFactsFromJson(productJson)

    assertEquals("Orange Juice", foodFacts.name)
    assertEquals("123456789", foodFacts.barcode)
    assertEquals(45, foodFacts.nutritionFacts.energyKcal)
    assertEquals(0.1, foodFacts.nutritionFacts.fat)
    assertEquals(9.0, foodFacts.nutritionFacts.sugars)
    assertEquals(1.0, foodFacts.nutritionFacts.proteins)
    assertEquals(0.01, foodFacts.nutritionFacts.salt)
  }

  @Test
  fun `test parseQueryResponse parses multiple products`() {
    // Mocking a JSON response string for a query
    val responseBody =
        """
            {
              "products": [
                {
                  "product_name": "Banana",
                  "code": "111111111",
                  "nutriments": {
                    "energy-kcal_100g": 89,
                    "fat_100g": 0.3,
                    "sugars_100g": 12.2,
                    "proteins_100g": 1.1,
                    "salt_100g": 0.0
                  }
                },
                {
                  "product_name": "Apple",
                  "code": "222222222",
                  "nutriments": {
                    "energy-kcal_100g": 52,
                    "fat_100g": 0.2,
                    "sugars_100g": 10.4,
                    "proteins_100g": 0.3,
                    "salt_100g": 0.0
                  }
                }
              ]
            }
        """
            .trimIndent()

    val repository = OpenFoodFactsRepository(OkHttpClient())
    val foodFactsList = repository.parseQueryResponse(responseBody)

    assertEquals(2, foodFactsList.size)
    assertEquals("Banana", foodFactsList[0].name)
    assertEquals("Apple", foodFactsList[1].name)
    assertEquals(89, foodFactsList[0].nutritionFacts.energyKcal)
    assertEquals(52, foodFactsList[1].nutritionFacts.energyKcal)
  }

  @Test
  fun `test parseBarcodeResponse parses single product`() {
    // Mocking a JSON response string for a barcode
    val responseBody =
        """
            {
              "product": {
                "product_name": "Tomato Ketchup",
                "code": "333333333",
                "nutriments": {
                  "energy-kcal_100g": 100,
                  "fat_100g": 0.1,
                  "sugars_100g": 22.0,
                  "proteins_100g": 1.2,
                  "salt_100g": 2.0
                }
              }
            }
        """
            .trimIndent()

    val repository = OpenFoodFactsRepository(OkHttpClient())
    val foodFactsList = repository.parseBarcodeResponse(responseBody)

    assertEquals(1, foodFactsList.size)
    assertEquals("Tomato Ketchup", foodFactsList[0].name)
    assertEquals("333333333", foodFactsList[0].barcode)
    assertEquals(100, foodFactsList[0].nutritionFacts.energyKcal)
  }

  @Test
  fun `test parseBarcodeResponse returns empty list when no product is found`() {
    // Mocking an empty JSON response
    val responseBody =
        """
            {
              "product": null
            }
        """
            .trimIndent()

    val repository = OpenFoodFactsRepository(OkHttpClient())
    val foodFactsList = repository.parseBarcodeResponse(responseBody)

    assertEquals(0, foodFactsList.size)
  }

  @Test
  fun `test parseFoodFactsResponse for barcode input`() {
    val responseBody =
        """
            {
              "product": {
                "product_name": "Cereal",
                "code": "444444444",
                "nutriments": {
                  "energy-kcal_100g": 200,
                  "fat_100g": 1.5,
                  "sugars_100g": 15.0,
                  "proteins_100g": 5.0,
                  "salt_100g": 0.5
                }
              }
            }
        """
            .trimIndent()

    val searchInput = FoodSearchInput.Barcode(444444444L)
    val repository = OpenFoodFactsRepository(OkHttpClient())
    val foodFactsList = repository.parseFoodFactsResponse(responseBody, searchInput)

    assertEquals(1, foodFactsList.size)
    assertEquals("Cereal", foodFactsList[0].name)
  }

  @Test
  fun `test parseFoodFactsResponse for query input`() {
    val responseBody =
        """
            {
              "products": [
                {
                  "product_name": "Cookies",
                  "code": "555555555",
                  "nutriments": {
                    "energy-kcal_100g": 500,
                    "fat_100g": 25.0,
                    "sugars_100g": 40.0,
                    "proteins_100g": 5.0,
                    "salt_100g": 0.5
                  }
                }
              ]
            }
        """
            .trimIndent()

    val searchInput = FoodSearchInput.Query("snacks")
    val repository = OpenFoodFactsRepository(OkHttpClient())
    val foodFactsList = repository.parseFoodFactsResponse(responseBody, searchInput)

    assertEquals(1, foodFactsList.size)
    assertEquals("Cookies", foodFactsList[0].name)
  }
}
