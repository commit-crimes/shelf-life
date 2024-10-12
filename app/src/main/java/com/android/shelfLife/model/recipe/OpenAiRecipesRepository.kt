package com.android.shelfLife.model.recipe

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.core.Parameters
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.android.shelfLife.BuildConfig
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class OpenAiRecipesRepository(
    private val openai: OpenAI = OpenAI(
        token = BuildConfig.OPENAI_API_KEY,
        timeout = Timeout(socket = 60.seconds)
    ),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecipesRepository {

    override fun generateRecipes(
        listFoodItems: List<FoodItem>,
        searchRecipeType: RecipesRepository.SearchRecipeType,
        onSuccess: (List<Recipe>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val foodItemsNames = "kisses, love, chicken"//listFoodItems.joinToString { it.name }  // Convert the food items into a comma-separated string
        val prompt = "Create a recipe using the following ingredients: $foodItemsNames."

        // Launch a coroutine
        CoroutineScope(dispatcher).launch {
            try {
                // Define the parameters for the recipe generation tool
                val params = Parameters.buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("ingredients") {
                            put("type", "array")
                            putJsonObject("items") {
                                put("type", "string")
                            }
                            put("description", "List of ingredients for the recipe")
                        }
                        putJsonObject("servings") {
                            put("type", "integer")
                            put("description", "Number of servings")
                        }
                        putJsonObject("time") {
                            put("type", "string")
                            put("description", "Estimated cooking time in minutes")
                        }
                        putJsonObject("instructions") {
                            put("type", "array")
                            putJsonObject("items") {
                                put("type", "string")
                            }
                            put("description", "Step by step instructions for the recipe")
                        }
                    }
                    putJsonArray("required") {
                        add("ingredients")
                        add("servings")
                        add("time")
                        add("instructions") // Make sure instructions are required
                    }
                }


                // Create the chat completion request with tool call
                val request = chatCompletionRequest {
                    model = ModelId("gpt-4o-mini")
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = prompt
                        )
                    )
                    tools {
                        function(
                            name = "_createRecipeFunction",
                            description = "Generate a recipe with ingredients, step by step instructions, servings, and cooking time",
                            parameters = params
                        )
                    }
                    toolChoice = ToolChoice.Auto // Automatically selects the tool
                }

                // Send the request to OpenAI and retrieve the tool call response
                val response = openai.chatCompletion(request)
                val message = response.choices.firstOrNull()?.message
                message?.toolCalls?.firstOrNull()?.let { toolCall ->
                    require(toolCall is ToolCall.Function) { "Tool call is not a function" }
                    val toolResponse = toolCall.execute()

                    // Convert the tool response into a recipe object
                    val generatedRecipe = Recipe(
                        name = "Generated Recipe",
                        instructions = toolResponse.instructions,
                        servings = toolResponse.servings,
                        time = toolResponse.time
                    )

                    onSuccess(listOf(generatedRecipe))  // Return the generated recipe
                } ?: onFailure(Exception("No tool call generated"))
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    /**
     * A map that associates function names with their corresponding functions.
     */
    private val availableFunctions = mapOf("_createRecipeFunction" to ::_createRecipeFunction)

    /**
     * Executes a function call and returns its result.
     */
    private fun ToolCall.Function.execute(): Recipe {
        val functionToCall =
            availableFunctions[function.name] ?: error("Function ${function.name} not found")
        val functionArgs = function.argumentsAsJson()

        // Extract ingredients, instructions, servings, and time from tool call arguments
        val ingredients = functionArgs["ingredients"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val instructions = functionArgs["instructions"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val servings = functionArgs["servings"]?.jsonPrimitive?.int ?: 2
        val time = functionArgs["time"]?.jsonPrimitive?.content?.toIntOrNull()?.minutes ?: 30.minutes

        return _createRecipeFunction(ingredients, instructions, servings, time)
    }

    private fun _createRecipeFunction(ingredients: List<String>, instructions: List<String>, servings: Int, time: Duration): Recipe {
        return Recipe(
            name = "Generated Recipe",
            instructions = instructions.mapIndexed { i, instruction -> "${i + 1}. $instruction" }.joinToString("\n"),
            servings = servings,
            time = time
        )
    }

}