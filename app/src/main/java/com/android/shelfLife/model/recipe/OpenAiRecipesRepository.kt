package com.android.shelfLife.model.recipe

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.core.Parameters
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.android.shelfLife.BuildConfig
import com.android.shelfLife.model.foodItem.FoodItem
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class OpenAiRecipesRepository(
    private val openai: OpenAI =
        OpenAI(token = BuildConfig.OPENAI_API_KEY, timeout = Timeout(socket = 60.seconds)),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecipesRepository {

    companion object {
        const val USE_SOON_TO_EXPIRE_SYSTEM_PROMPT =
            "You are an assistant who generates recipes using ingredients that are about to expire."
        const val USE_SOON_TO_EXPIRE_USER_PROMPT =
            "Create a recipe using the following ingredients that are about to expire: "

        const val USE_ONLY_HOUSEHOLD_ITEMS_SYSTEM_PROMPT =
            "You are an assistant who generates recipes using common household ingredients."
        const val USE_ONLY_HOUSEHOLD_ITEMS_USER_PROMPT =
            "Create a recipe using the following common household ingredients: "

        const val HIGH_PROTEIN_SYSTEM_PROMPT =
            "You are an assistant who generates high-protein recipes."
        const val HIGH_PROTEIN_USER_PROMPT =
            "Create a high-protein recipe using the following ingredients: "

        const val LOW_CALORIE_SYSTEM_PROMPT = "You are an assistant who generates low-calorie recipes."
        const val LOW_CALORIE_USER_PROMPT =
            "Create a low-calorie recipe using the following ingredients: "
    }

    // Define custom prompts for different recipe types
    private fun getPromptsForMode(
        listFoodItems: List<FoodItem>,
        searchRecipeType: RecipesRepository.SearchRecipeType
    ): Pair<String, String> {

        val foodItemsNames = listFoodItems.joinToString(", ") { it.toString() }

        // System and user prompts based on the recipe search type
        return when (searchRecipeType) {
            RecipesRepository.SearchRecipeType.USE_SOON_TO_EXPIRE -> {
                USE_SOON_TO_EXPIRE_SYSTEM_PROMPT to "$USE_SOON_TO_EXPIRE_USER_PROMPT$foodItemsNames."
            }
            RecipesRepository.SearchRecipeType.USE_ONLY_HOUSEHOLD_ITEMS -> {
                USE_ONLY_HOUSEHOLD_ITEMS_SYSTEM_PROMPT to
                        "$USE_ONLY_HOUSEHOLD_ITEMS_USER_PROMPT$foodItemsNames."
            }
            RecipesRepository.SearchRecipeType.HIGH_PROTEIN -> {
                HIGH_PROTEIN_SYSTEM_PROMPT to "$HIGH_PROTEIN_USER_PROMPT$foodItemsNames."
            }
            RecipesRepository.SearchRecipeType.LOW_CALORIE -> {
                LOW_CALORIE_SYSTEM_PROMPT to "$LOW_CALORIE_USER_PROMPT$foodItemsNames."
            }
        }
    }

    override fun generateRecipes(
        listFoodItems: List<FoodItem>,
        searchRecipeType: RecipesRepository.SearchRecipeType,
        onSuccess: (List<Recipe>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Get the custom system and user prompts based on the mode
        val (systemPrompt, userPrompt) = getPromptsForMode(listFoodItems, searchRecipeType)

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
                                putJsonObject("items") { put("type", "string") }
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
                                role = ChatRole.User, content = userPrompt // User prompt with food items
                            ))
                    tools {
                        function(
                            name = "_createRecipeFunction",
                            description =
                            "Generate a recipe with ingredients, step by step instructions, servings, and cooking time",
                            parameters = params)
                    }
                    toolChoice = ToolChoice.Auto // Automatically selects the tool
                }

                // Send the request to OpenAI and retrieve the tool call response
                val response = openai.chatCompletion(request)
                val message = response.choices.firstOrNull()?.message
                message?.toolCalls?.firstOrNull()?.let { toolCall ->
                    require(toolCall is ToolCall.Function) { "Tool call is not a function" }
                    val toolResponse = toolCall.execute(searchRecipeType)

                    // Convert the tool response into a recipe object
                    val generatedRecipe =
                        Recipe(
                            name = "Generated Recipe",
                            instructions = toolResponse.instructions,
                            servings = toolResponse.servings,
                            time = toolResponse.time,
                            ingredients = toolResponse.ingredients,
                            recipeType = searchRecipeType)

                    onSuccess(listOf(generatedRecipe)) // Return the generated recipe
                } ?: onFailure(Exception("No tool call generated"))
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    private val availableFunctions = mapOf("_createRecipeFunction" to ::_createRecipeFunction)

    private fun ToolCall.Function.execute(recipeType: RecipesRepository.SearchRecipeType): Recipe {
        val functionToCall =
            availableFunctions[function.name] ?: error("Function ${function.name} not found")
        val functionArgs = function.argumentsAsJson()

        // Extract ingredients, instructions, servings, and time from tool call arguments
        val ingredients =
            functionArgs["ingredients"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val instructions =
            functionArgs["instructions"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val servings = functionArgs["servings"]?.jsonPrimitive?.int ?: 2
        val time = functionArgs["time"]?.jsonPrimitive?.content?.toIntOrNull()?.minutes ?: 30.minutes

        return _createRecipeFunction(ingredients, instructions, servings, time, recipeType)
    }

    @JvmName("_createRecipeFunction") // allowing tests to access this function using reflection
    private fun _createRecipeFunction(
        ingredients: List<String>,
        instructions: List<String>,
        servings: Int,
        time: kotlin.time.Duration,
        recipeType: RecipesRepository.SearchRecipeType
    ): Recipe {

        // TODO: logic to find and create a new list of Ingredients
        return Recipe(
            name = "Generated Recipe", instructions = instructions, servings = servings, time = time, recipeType = recipeType)
    }
}
