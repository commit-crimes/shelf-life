package com.android.shelfLife.model.recipe

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.android.shelfLife.BuildConfig
import com.android.shelfLife.model.foodItem.FoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class OpenAiRecipesRepositoryIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var openAiRecipesRepository: OpenAiRecipesRepository

    @Before
    fun setUp() {
        // Initialize the mock web server (if needed, otherwise comment this out for real calls)
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Initialize OpenAI repository with real API key (or mock URL if using MockWebServer)
        val openai = OpenAI(
            token = BuildConfig.OPENAI_API_KEY,
            timeout = Timeout(socket = 60.seconds)
        )
        openAiRecipesRepository = OpenAiRecipesRepository(openai, dispatcher = Dispatchers.IO)
    }

    @After
    fun tearDown() {
        // Shut down the mock web server after each test (if needed)
        mockWebServer.shutdown()
    }

    @Test
    fun `test generateRecipes from OpenAI API and print results`(): Unit = runBlocking {
        val latch = CountDownLatch(1)

        // Example food items for the recipe generation

        // Call the repository to generate a recipe using OpenAI
        openAiRecipesRepository.generateRecipes(
            listFoodItems = listOf(),
            searchRecipeType = RecipesRepository.SearchRecipeType.LOW_CALORIE,
            onSuccess = { recipes ->
                // Print the recipes to the console
                recipes.forEach { recipe ->
                    println("Generated Recipe: ${recipe.name}")
                    println("Instructions: ${recipe.instructions}")
                    println("Servings: ${recipe.servings}")
                    println("Time: ${recipe.time}")
                }
                latch.countDown()
            },
            onFailure = { error ->
                println("Error generating recipes: ${error.message}")
                latch.countDown()
            }
        )

        latch.await(10, TimeUnit.SECONDS) // Wait for the recipe generation to complete
    }
}
