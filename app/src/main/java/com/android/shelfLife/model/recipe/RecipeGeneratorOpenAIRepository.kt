package com.android.shelfLife.model.recipe

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.core.Parameters
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.android.shelfLife.di.IoDispatcher
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.newFoodItem.FoodItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

@Singleton
class RecipeGeneratorOpenAIRepository
@Inject
constructor(private val openai: OpenAI, @IoDispatcher private val dispatcher: CoroutineDispatcher) :
    RecipeGeneratorRepository {

  companion object {
    val QUICK_RECIPE_DEF = "quick, 15minutes"
    val LONG_RECIPE_DEF = "long, over 30minutes"

    // Base prompts
    val BASE_SYSTEM_PROMPT =
      { recipeDescription: String -> "You are an assistant who generates  $recipeDescription recipes according to user's needs." }

    val BASE_USER_PROMPT: (Float, String, String, Boolean, String, String) -> String =
      { servings, recipeDescription, ingredients, onlyHousehold, recipeName, specialInstructions -> "Create a $recipeDescription recipe with exactly $servings servings. " +
        "The user named the recipe \"$recipeName\" and specified the following special instructions: \"$specialInstructions\". Use the following ingredients: (${ingredients}) ${
        if (onlyHousehold) "To ensure a valid recipe, stick strictly to the provided ingredients. Do not add any other ingredients."
        else "To ensure a great tasting recipe, you can add other ingredients you deem relevant and necessary."
      } Call _createRecipeFunction with a list of ingredients (name + quantity), step-by-step instructions, servings, and cooking time in seconds." }

  }

  private fun getPromptsForMode(recipePrompt: RecipePrompt): Pair<String, String> {
    val servings = recipePrompt.servings

    // Helper to filter and sort food items
    val foodItems = recipePrompt.ingredients
      .let { ingredients ->
        when (recipePrompt.recipeType) {
          RecipeType.USE_SOON_TO_EXPIRE -> ingredients.sortedBy { it.expiryDate }
          else -> ingredients
        }
      }

    val foodItemNames = foodItems.joinToString(", ") { it.foodFacts.name }

    val recipeTypeDescription = when (recipePrompt.recipeType) {
      RecipeType.USE_SOON_TO_EXPIRE -> ""
      RecipeType.USE_ONLY_HOUSEHOLD_ITEMS -> ""
      RecipeType.HIGH_PROTEIN -> "high protein"
      RecipeType.LOW_CALORIE -> "low calorie"
      RecipeType.PERSONAL -> throw IllegalArgumentException(
        "RecipeType.PERSONAL is not supported for recipe generation as it is reserved for user-created recipes."
      )
    }
    val recipeDescriptionFinal = "$recipeTypeDescription, ${if (recipePrompt.shortDuration) QUICK_RECIPE_DEF else LONG_RECIPE_DEF}"

    val finalUserPrompt = BASE_USER_PROMPT(servings, recipeDescriptionFinal, foodItemNames, recipePrompt.recipeType == RecipeType.USE_ONLY_HOUSEHOLD_ITEMS, recipePrompt.name, recipePrompt.specialInstruction)

    return BASE_SYSTEM_PROMPT(recipeDescriptionFinal) to finalUserPrompt
  }


  override fun generateRecipe(
      recipePrompt: RecipePrompt,
      onSuccess: (Recipe) -> Unit,
      onFailure: (Exception) -> Unit
  ) {

    // Get the custom system and user prompts based on the mode
    val (systemPrompt, userPrompt) =
        getPromptsForMode(recipePrompt)
    // Launch a coroutine
    CoroutineScope(dispatcher).launch {
      try {
        // Define the parameters for the recipe generation tool
        val params =
            Parameters.buildJsonObject {
              put("type", "object")
              putJsonObject("properties") {
                putJsonObject("ingredients") {
                  put("type", "array")
                  putJsonObject("items") {
                    put("type", "object")
                    putJsonObject("properties") {
                      putJsonObject("name") {
                        put("type", "string")
                        put("description", "Name of the ingredient")
                      }
                      putJsonObject("quantity") {
                        put("type", "number")
                        put("description", "Quantity value of the ingredient")
                      }
                      putJsonObject("unit") {
                        put("type", "string")
                        putJsonArray("enum") {
                          add("ML")
                          add("GRAM")
                        }
                        put("description", "Unit of measurement (ml or gram)")
                      }
                    }
                    putJsonArray("required") {
                      add("name")
                      add("quantity")
                      add("unit")
                    }
                    put("description", "Ingredient with name, quantity, and unit")
                  }
                  put("description", "List of ingredients for the recipe")
                }
                putJsonObject("servings") {
                  put("type", "integer")
                  put("description", "Number of servings")
                }
                putJsonObject("time") {
                  put("type", "integer")
                  put("description", "Estimated cooking time in seconds")
                }
                putJsonObject("instructions") {
                  put("type", "array")
                  putJsonObject("items") { put("type", "string") }
                  put("description", "Step-by-step instructions for the recipe")
                }
              }
              putJsonArray("required") {
                add("ingredients")
                add("servings")
                add("time")
                add("instructions")
              }
            }

        // Create the chat completion request with the system and user prompts
        val request = chatCompletionRequest {
          model = ModelId("gpt-4o-mini")
          messages =
              listOf(
                  ChatMessage(
                      role = ChatRole.System, content = systemPrompt // System prompt
                      ),
                  ChatMessage(
                      role = ChatRole.User,
                      content =
                          userPrompt // User prompt with food items
                      ))
          tools {
            function(
                name = "_createRecipeFunction",
                description =
                    "Generate a recipe with a list of ingredients, step by step instructions, servings, and cooking time in seconds",
                parameters = params)
          }
          toolChoice = ToolChoice.Auto // Automatically selects the tool
        }

        // Send the request to OpenAI and retrieve the tool call response
        val response = openai.chatCompletion(request)
        val message = response.choices.firstOrNull()?.message
        message?.toolCalls?.firstOrNull()?.let { toolCall ->
          require(toolCall is ToolCall.Function) { "Tool call is not a function" }
          val toolResponse = toolCall.execute() as Map<String, Any>
          // Construct the final Recipe object from the tool response
          val generatedRecipe =
              Recipe(
                  uid = "0", // Placeholder UID
                  name = recipePrompt.name,
                  instructions = toolResponse["instructions"] as List<String>,
                  servings = (toolResponse["servings"] as Int).toFloat(),
                  time = (toolResponse["time"] as Long).minutes,
                  workInProgress = true,
                  ingredients =
                      (toolResponse["ingredients"] as List<Map<String, Any>>).map { ingredient ->
                        Ingredient(
                            name = ingredient["name"] as String, // Access the map value for "name"
                            quantity =
                                Quantity(
                                    amount =
                                        ingredient["quantity"]
                                            as Double, // Access the map value for "quantity"
                                    unit =
                                        ingredient["unit"]
                                            as FoodUnit // Access the map value for "unit"
                                    ),
                            macros = NutritionFacts() // Default macros
                            )
                      },
                  recipeType = recipePrompt.recipeType)
          onSuccess(generatedRecipe) // Return the generated recipe
        } ?: onFailure(Exception("No tool call generated"))
      } catch (e: Exception) {
        onFailure(e)
      }
    }
  }

  private val availableFunctions = mapOf("_createRecipeFunction" to ::_createRecipeFunction)

  private fun ToolCall.Function.execute(): Map<String, Any> {
    val functionToCall =
        availableFunctions[function.name] ?: error("Function ${function.name} not found")
    val functionArgs = function.argumentsAsJson()

    // Safely extract arguments with defaults
    val ingredients =
        try {
          functionArgs["ingredients"]?.jsonArray?.map { ingredient ->
            ingredient.jsonObject.let {
              mapOf(
                  "name" to it["name"]?.jsonPrimitive?.content.orEmpty(),
                  "quantity" to (it["quantity"]?.jsonPrimitive?.doubleOrNull ?: 0.0),
                  "unit" to
                      (it["unit"]?.jsonPrimitive?.content?.let { unit ->
                        when (unit) {
                          "ML" -> FoodUnit.ML
                          "GRAM" -> FoodUnit.GRAM
                          else -> null
                        }
                      } ?: FoodUnit.GRAM))
            }
          } ?: emptyList()
        } catch (e: Exception) {
          emptyList()
        }

    val instructions =
        try {
          functionArgs["instructions"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        } catch (e: Exception) {
          emptyList()
        }

    val servings = functionArgs["servings"]?.jsonPrimitive?.int ?: 2
    val time = functionArgs["time"]?.jsonPrimitive?.content?.toIntOrNull()?.seconds ?: 30.seconds

    return functionToCall(ingredients, instructions, servings, time)
  }

  @JvmName("_createRecipeFunction") // allowing tests to access this function using reflection
  private fun _createRecipeFunction(
      ingredients: List<Map<String, Any>>, // List of ingredient maps
      instructions: List<String>,
      servings: Int,
      time: Duration
  ): Map<String, Any> {
    return mapOf(
        "name" to "Generated Recipe",
        "ingredients" to ingredients, // Return the updated ingredients structure
        "instructions" to instructions,
        "servings" to servings,
        "time" to time.inWholeSeconds // Return time in seconds for consistency
        )
  }
}
