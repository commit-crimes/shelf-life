package com.android.shelfLife.model.foodFacts

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class OpenFoodFactsRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var mockOpenFoodFactsRepository: OpenFoodFactsRepository
    private lateinit var openFoodFactsRepository: OpenFoodFactsRepository

    @Before
    fun setUp() {
        // Initialize Mockito
        MockitoAnnotations.openMocks(this)

        // Start the mock web server
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Use the mock web server's URL as the base URL for OkHttpClient
        val baseUrl = mockWebServer.url("/").toString()
        client = OkHttpClient()

        // Pass the base URL to the repository to ensure it uses MockWebServer for requests
        mockOpenFoodFactsRepository = OpenFoodFactsRepository(client, baseUrl)

        openFoodFactsRepository = OpenFoodFactsRepository(client)
    }


    @After
    fun tearDown() {
        // Shut down the mock web server after each test
        mockWebServer.shutdown()
    }

    @Test
    fun `searchFoodFacts should return list of FoodFacts on successful response`() {
        val mockResponseBody = """
        {
            "products": [
                {
                    "product_name": "Banana",
                    "code": "1234567890",
                    "nutriments": {
                        "energy-kcal_100g": 89,
                        "fat_100g": 0.3,
                        "saturated-fat_100g": 0.1,
                        "carbohydrates_100g": 23,
                        "sugars_100g": 12,
                        "proteins_100g": 1.1,
                        "salt_100g": 0.01
                    }
                }
            ]
        }
    """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(mockResponseBody).setResponseCode(200))

        val searchInput = FoodSearchInput.Query("banana")
        val latch = CountDownLatch(1)
        var result: List<FoodFacts>? = null

        mockOpenFoodFactsRepository.searchFoodFacts(
            searchInput,
            onSuccess = { foodFactsList ->
                result = foodFactsList
                latch.countDown()
            },
            onFailure = { fail("Expected success but got failure") }
        )

        latch.await(5, TimeUnit.SECONDS)

        assertTrue(result != null && result!!.isNotEmpty())

        val foodFacts = result!!.first()

        assertEquals("Banana", foodFacts.name)
        assertEquals("1234567890", foodFacts.barcode)
        assertEquals(89, foodFacts.nutritionFacts.energyKcal)
        assertEquals(0.3, foodFacts.nutritionFacts.fat, 0.0001)
        assertEquals(12.0, foodFacts.nutritionFacts.sugars, 0.0001)

    }

    @Test
    fun `searchFoodFacts should call onFailure on server error`() {
        // Given: Mock server returns a server error
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        // When: Performing the search with a barcode
        val searchInput = FoodSearchInput.Barcode(1234567890)
        val latch = CountDownLatch(1)
        var exceptionThrown: Exception? = null

        mockOpenFoodFactsRepository.searchFoodFacts(
            searchInput,
            onSuccess = { fail("Expected failure but got success") },
            onFailure = { exception ->
                exceptionThrown = exception
                latch.countDown()
            }
        )

        latch.await(5, TimeUnit.SECONDS)

        // Then: Verify that an error was correctly handled with the proper message
        assertTrue(exceptionThrown is IOException)
    }

    @Test
    fun `fetch real FoodFacts from OpenFoodFacts API and verify Nutella details`() {
        val barcodeInput = FoodSearchInput.Barcode(3017620425035) // Barcode for Nutella
        val latch = CountDownLatch(1)
        var result: List<FoodFacts>? = null

        openFoodFactsRepository.searchFoodFacts(
            barcodeInput,
            onSuccess = { foodFactsList ->
                result = foodFactsList
                latch.countDown()
            },
            onFailure = { exception ->
                println("Error fetching FoodFacts: ${exception.message}")
                latch.countDown()
            }
        )

        latch.await(10, TimeUnit.SECONDS) // Wait for a maximum of 10 seconds for the response

        // Then: Verify that the correct FoodFacts for Nutella is returned
        assertTrue(result != null && result!!.isNotEmpty())

        val foodFacts = result!!.first()

        assertEquals("Nutella", foodFacts.name)
        assertEquals("3017620425035", foodFacts.barcode)
        assertEquals(1.0, foodFacts.quantity.amount, 0.0001)
        assertEquals(FoodUnit.GRAM, foodFacts.quantity.unit)
        assertEquals(FoodCategory.OTHER, foodFacts.category)

        val nutritionFacts = foodFacts.nutritionFacts
        assertEquals(539, nutritionFacts.energyKcal)
        assertEquals(30.9, nutritionFacts.fat, 0.0001)
        assertEquals(10.6, nutritionFacts.saturatedFat, 0.0001)
        assertEquals(57.5, nutritionFacts.carbohydrates, 0.0001)
        assertEquals(56.3, nutritionFacts.sugars, 0.0001)
        assertEquals(6.3, nutritionFacts.proteins, 0.0001)
        assertEquals(0.107, nutritionFacts.salt, 0.0001)

        println("Verified FoodFacts for Nutella: $foodFacts")
    }

    @Test
    fun `fetch real FoodFacts from OpenFoodFacts API`() {
        val stringInput = FoodSearchInput.Query("banana")
        val barcodeInput = FoodSearchInput.Barcode(3017620425035)
        val latch = CountDownLatch(1)

        openFoodFactsRepository.searchFoodFacts(
            stringInput,
            onSuccess = { foodFactsList ->
                println("Received FoodFacts List 1:")
                foodFactsList.forEach { foodFact ->
                    println("Product Name: ${foodFact}")
                }
                latch.countDown()
            },
            onFailure = { exception ->
                println("Error fetching FoodFacts: ${exception.message}")
                latch.countDown()
            }
        )

        latch.await(10, TimeUnit.SECONDS) // Wait for a maximum of 10 seconds for the response
    }
}
