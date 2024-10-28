package com.android.shelflife.model.recipe

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.android.shelfLife.BuildConfig
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.recipe.OpenAiRecipesRepository
import com.android.shelfLife.model.recipe.RecipesRepository
import com.google.firebase.Timestamp
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class OpenAiRecipesRepositoryIntegrationTest {

  /**
   * Integration test for generating recipes using OpenAI API (this test queries the openai api).
   * Set RUN_INTEGRATION_TEST to true to run the test and print out results of a recipe. WARNING!!:
   * DO NOT PUSH TO CI WITH INTEGRATION TEST ENABLED. THIS TEST DOES NOT MOCK, IT PROPERLY CALLS THE
   * API. ONLY FOR DEBUGGING LOCALLY. (note: This test requires a valid OpenAI API key to run.)
   */
  companion object {
    private val _NOT_ON_CI: Boolean by lazy {
      (System.getenv("CI")?.toBoolean() != true) &&
          (System.getenv("GITHUB_ACTION")?.toBoolean() != true)
    }
    private const val RUN_INTEGRATION_TEST: Boolean = false // or false based on user input
  }

  private val runIntegrationTest = RUN_INTEGRATION_TEST && _NOT_ON_CI

  private lateinit var mockWebServer: MockWebServer
  private lateinit var openAiRecipesRepository: OpenAiRecipesRepository

  val oneDaySeconds = 24 * 60 * 60 // 1 day in milliseconds

  val foodItems =
      listOf(
          FoodItem(
              uid = "1",
              foodFacts =
                  FoodFacts(
                      name = "Apple",
                      barcode = "123456789012",
                      quantity = Quantity(4.0, FoodUnit.COUNT),
                      category = FoodCategory.FRUIT),
              location = FoodStorageLocation.PANTRY,
              expiryDate = Timestamp.now(), // Just purchased, no expiry date provided
              status = FoodStatus.CLOSED),
          FoodItem(
              uid = "2",
              foodFacts =
                  FoodFacts(
                      name = "Milk",
                      barcode = "987654321098",
                      quantity = Quantity(1000.0, FoodUnit.ML),
                      category = FoodCategory.DAIRY,
                      nutritionFacts =
                          NutritionFacts(energyKcal = 50, fat = 3.0, proteins = 3.4, sugars = 5.0)),
              location = FoodStorageLocation.FRIDGE,
              expiryDate =
                  Timestamp(Timestamp.now().seconds + 1 * oneDaySeconds, 0), // Expires in 5 days
              status = FoodStatus.CLOSED),
          FoodItem(
              uid = "3",
              foodFacts =
                  FoodFacts(
                      name = "Chicken Breast",
                      barcode = "564738291234",
                      quantity = Quantity(500.0, FoodUnit.GRAM),
                      category = FoodCategory.MEAT),
              location = FoodStorageLocation.FREEZER,
              expiryDate =
                  Timestamp(Timestamp.now().seconds + 5 * oneDaySeconds, 0), // Expires in 5 days
              status = FoodStatus.CLOSED),
          FoodItem(
              uid = "4",
              foodFacts =
                  FoodFacts(
                      name = "Orange Juice",
                      barcode = "349857621098",
                      quantity = Quantity(1500.0, FoodUnit.ML),
                      category = FoodCategory.BEVERAGE),
              location = FoodStorageLocation.FRIDGE,
              expiryDate =
                  Timestamp(Timestamp.now().seconds + 3 * oneDaySeconds, 0), // Expires in 5 days
              status = FoodStatus.OPEN),
          FoodItem(
              uid = "5",
              foodFacts =
                  FoodFacts(
                      name = "Pasta",
                      barcode = "238974561234",
                      quantity = Quantity(500.0, FoodUnit.GRAM),
                      category = FoodCategory.GRAIN),
              location = FoodStorageLocation.PANTRY,
              expiryDate = null, // No expiry date provided
              status = FoodStatus.CLOSED),
          FoodItem(
              uid = "5",
              foodFacts =
                  FoodFacts(
                      name = "Pasta",
                      barcode = "238974561234",
                      quantity = Quantity(500.0, FoodUnit.GRAM),
                      category = FoodCategory.GRAIN),
              location = FoodStorageLocation.PANTRY,
              expiryDate = null, // No expiry date provided
              status = FoodStatus.CLOSED))

  @Before
  fun setUp() {
    // Initialize the mock web server (if needed, otherwise comment this out for real calls)
    mockWebServer = MockWebServer()
    mockWebServer.start()

    if (!runIntegrationTest)
        return System.out.println(
            "OPENAI_RECIPES_INTEGRATION_TEST: Skipping integration tests! Is on CI: ${!_NOT_ON_CI}, Integration test disabled: ${!RUN_INTEGRATION_TEST}")

    // Initialize OpenAI repository with real API key (or mock URL if using MockWebServer)
    val openai = OpenAI(token = BuildConfig.OPENAI_API_KEY, timeout = Timeout(socket = 60.seconds))
    openAiRecipesRepository = OpenAiRecipesRepository(openai, dispatcher = Dispatchers.IO)
  }

  @After
  fun tearDown() {
    // Shut down the mock web server after each test (if needed)
    mockWebServer.shutdown()
  }

  @Test
  fun `test generateRecipes from OpenAI API and print results`(): Unit = runBlocking {
    if (!runIntegrationTest) return@runBlocking

    val latch = CountDownLatch(1)
    // Example food items for the recipe generation

    // Call the repository to generate a recipe using OpenAI
    openAiRecipesRepository.generateRecipes(
        listFoodItems = foodItems,
        searchRecipeType = RecipesRepository.SearchRecipeType.LOW_CALORIE,
        onSuccess = { recipes ->
          // Print the recipes to the console
          recipes.forEach { recipe ->
            println("Generated Recipe: ${recipe.name}")
            println("Ingredients: ${recipe.ingredients}")
            println(
                "Instructions: ${recipe.instructions.mapIndexed { index, instruction -> "${index + 1}. $instruction" }.joinToString(", ")}")
            println("Servings: ${recipe.servings}")
            println("Time: ${recipe.time}")
          }
          latch.countDown()
        },
        onFailure = { error ->
          println("Error generating recipes: ${error.message}")
          latch.countDown()
        })

    latch.await(10, TimeUnit.SECONDS) // Wait for the recipe generation to complete
  }
}
