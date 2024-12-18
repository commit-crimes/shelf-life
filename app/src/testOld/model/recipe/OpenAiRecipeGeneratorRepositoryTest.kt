package com.android.shelfLife.model.newRecipe

import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.FunctionCall
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.chat.ToolId
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeGeneratorOpenAIRepository
import com.android.shelfLife.model.recipe.RecipePrompt
import com.android.shelfLife.model.recipe.RecipeType
import java.lang.reflect.Method
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

class OpenAiRecipeRepositoryTest {

  @Mock private lateinit var mockOpenAI: OpenAI
  private lateinit var openAiRecipesRepository: RecipeGeneratorOpenAIRepository

  @OptIn(ExperimentalCoroutinesApi::class) private val testDispatcher = UnconfinedTestDispatcher()

  private val testFoodItems =
      listOf(
          FoodItem(
              uid = "1",
              foodFacts =
                  FoodFacts(name = "Ingredient 1", quantity = Quantity(1.0, FoodUnit.COUNT)),
              owner = "test_owner"),
          FoodItem(
              uid = "2",
              foodFacts =
                  FoodFacts(name = "Ingredient 2", quantity = Quantity(1.0, FoodUnit.COUNT)),
              owner = "test_owner"))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    openAiRecipesRepository =
        RecipeGeneratorOpenAIRepository(openai = mockOpenAI, dispatcher = testDispatcher)
  }

  /**
   * Test for every recipe type enum value, ensuring that the correct system and user prompts are
   * generated.
   */
  @Test
  fun `getPromptsForMode should return correct prompts for all recipe types`() {
    val method: Method =
        RecipeGeneratorOpenAIRepository::class
            .java
            .getDeclaredMethod("getPromptsForMode", List::class.java, RecipeType::class.java)
    method.isAccessible = true // Make the method accessible

    // Define expected system and user prompts for each search type
    val ingredientsString = testFoodItems.joinToString(", ") { it.toString() }
    val expectedPrompts =
        mapOf(
            RecipeType.USE_SOON_TO_EXPIRE to
                (RecipeGeneratorOpenAIRepository.USE_SOON_TO_EXPIRE_SYSTEM_PROMPT to
                    "${RecipeGeneratorOpenAIRepository.USE_SOON_TO_EXPIRE_USER_PROMPT}${ingredientsString}."),
            RecipeType.USE_ONLY_HOUSEHOLD_ITEMS to
                (RecipeGeneratorOpenAIRepository.USE_ONLY_HOUSEHOLD_ITEMS_SYSTEM_PROMPT to
                    "${RecipeGeneratorOpenAIRepository.USE_ONLY_HOUSEHOLD_ITEMS_USER_PROMPT}${ingredientsString}."),
            RecipeType.HIGH_PROTEIN to
                (RecipeGeneratorOpenAIRepository.HIGH_PROTEIN_SYSTEM_PROMPT to
                    "${RecipeGeneratorOpenAIRepository.HIGH_PROTEIN_USER_PROMPT}${ingredientsString}."),
            RecipeType.LOW_CALORIE to
                (RecipeGeneratorOpenAIRepository.LOW_CALORIE_SYSTEM_PROMPT to
                    "${RecipeGeneratorOpenAIRepository.LOW_CALORIE_USER_PROMPT}${ingredientsString}."))

    // Iterate over each search type and test the generated prompts
    for ((searchType, expectedPromptPair) in expectedPrompts) {
      val (expectedSystemPrompt, expectedUserPrompt) = expectedPromptPair

      val (systemPrompt, userPrompt) =
          method.invoke(openAiRecipesRepository, testFoodItems, searchType) as Pair<String, String>

      assertEquals(
          "System prompt for $searchType did not match.", expectedSystemPrompt, systemPrompt)
      assertEquals("User prompt for $searchType did not match.", expectedUserPrompt, userPrompt)
    }
  }

  @Test
  fun `generateRecipe should call OpenAI with appropriate request`() =
      runTest(testDispatcher) {
        val mockResponse = mockRecipeResponse()
        whenever(mockOpenAI.chatCompletion(any(), anyOrNull())).thenReturn(mockResponse)

        var recipeResult: Recipe? = null
        openAiRecipesRepository.generateRecipe(
            recipePrompt =
                RecipePrompt(
                    "Generated Recipe", RecipeType.USE_SOON_TO_EXPIRE, ingredients = testFoodItems),
            onSuccess = { recipe ->
              println("Success: Recipes generated successfully $recipe")
              recipeResult = recipe
            },
            onFailure = { e ->
              println("FAILURE: $e")
              fail("Expected success callback")
            })

        // No need to advance time with UnconfinedTestDispatcher

        assertNotNull(recipeResult)
        assertEquals("Generated Recipe", recipeResult?.name)
      }

  @Test
  fun `generateRecipe should call onFailure on exception`() =
      runTest(testDispatcher) {
        whenever(mockOpenAI.chatCompletion(any(), anyOrNull()))
            .thenThrow(RuntimeException("API error"))

        var errorMessage: String? = null
        openAiRecipesRepository.generateRecipe(
            recipePrompt =
                RecipePrompt(
                    "Generated Recipe", RecipeType.USE_SOON_TO_EXPIRE, ingredients = testFoodItems),
            onSuccess = { fail("Expected failure callback") },
            onFailure = { error ->
              println("FAILURE: $error")
              errorMessage = error.message
            })

        assertEquals("API error", errorMessage)
      }

  /**
   * Uses "hacky" reflection to test private method (Prof. Candea's suggestion:
   * https://edstem.org/eu/courses/1567/discussion/131808)
   */
  @Test
  fun `test _createRecipeFunction using reflection`() {
    val method =
        RecipeGeneratorOpenAIRepository::class.java.declaredMethods.firstOrNull {
          it.name == "_createRecipeFunction"
        } ?: throw NoSuchMethodException("Method _createRecipeFunction not found")

    method.isAccessible = true

    val ingredients =
        listOf(
            mapOf("name" to "Chicken", "quantity" to 2, "unit" to "pieces"),
            mapOf("name" to "Rice", "quantity" to 1, "unit" to "cup"),
            mapOf("name" to "Broccoli", "quantity" to 1, "unit" to "head"))
    val instructions = listOf("Cook rice", "Steam broccoli", "Grill chicken")
    val servings = 5
    val time = 100.seconds // Original Duration value

    // Convert time to milliseconds (Long) for reflection (it compiled Duration into a Long)
    val timeInMillis = time.inWholeMilliseconds

    // Invoke the private method
    val recipe =
        method.invoke(openAiRecipesRepository, ingredients, instructions, servings, timeInMillis)
            as Map<*, *>

    // Assertions for the returned Recipe
    assertNotNull(recipe)
    assertEquals("Generated Recipe", recipe["name"])
    assertEquals(instructions, recipe["instructions"])
    assertEquals(servings, recipe["servings"])
  }

  // Mock method for generating a recipe response
  private fun mockRecipeResponse(): ChatCompletion {
    // Mock the tool response as a JSON object to simulate recipe generation
    val mockToolResponse = buildJsonObject {
      put(
          "ingredients",
          buildJsonArray {
            add(
                buildJsonObject {
                  put("name", "Chicken")
                  put("quantity", 2)
                  put("unit", "pieces")
                })
            add(
                buildJsonObject {
                  put("name", "Rice")
                  put("quantity", 1)
                  put("unit", "cup")
                })
            add(
                buildJsonObject {
                  put("name", "Broccoli")
                  put("quantity", 1)
                  put("unit", "head")
                })
          })
      put("servings", 2)
      put("time", "30 minutes")
      putJsonArray("instructions") {
        add("Cook rice")
        add("Steam broccoli")
        add("Grill chicken")
      }
    }

    // Convert the mockToolResponse to a JSON string for argumentsOrNull
    val mockToolResponseString = Json.encodeToString(mockToolResponse)

    // Define the FunctionCall with the function name and arguments
    val functionCall =
        FunctionCall(nameOrNull = "_createRecipeFunction", argumentsOrNull = mockToolResponseString)

    // Assuming `ToolCall` has an associated method or factory for instantiation
    val mockToolCall =
        ToolCall.Function(id = ToolId("_createRecipeFunction"), function = functionCall)

    // Creating the mock message with tool calls
    val mockMessage =
        ChatMessage(
            role = ChatRole.Assistant,
            content = "Generated Recipe",
            toolCalls =
                listOf(mockToolCall) // Assuming toolCalls is the list of tool calls required
            )

    // Creating the choice with the missing index value added
    val mockChoice =
        ChatChoice(
            index = 0, // Add index for ChatChoice
            message = mockMessage)

    // Returning a mock ChatCompletion response
    return ChatCompletion(
        id = "test_id",
        created = System.currentTimeMillis() / 1000, // Mock Unix timestamp
        model = ModelId("test-model"),
        choices = listOf(mockChoice))
  }
}
