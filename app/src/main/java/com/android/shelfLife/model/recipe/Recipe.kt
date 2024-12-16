package com.android.shelfLife.model.recipe

import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import kotlin.time.Duration

/**
 * Data class representing a recipe object.
 *
 * @property uid Unique identifier for the recipe.
 * @property name Name of the recipe.
 * @property instructions Step-by-step cooking instructions for the recipe.
 * @property servings Total number of servings the recipe makes.
 * @property time Estimated cooking time for the recipe.
 * @property ingredients List of ingredients required for the recipe.
 * @property recipeType Type of the recipe, such as BASIC or PERSONAL.
 * @property workInProgress Indicates whether the recipe is still being worked on.
 */
data class Recipe(
    val uid: String, // Unique identifier for the recipe
    val name: String, // Recipe name
    val instructions: List<String>, // Step-by-step cooking instructions
    val servings: Float, // Total number of servings
    val time: Duration, // Estimated cooking time
    val ingredients: List<Ingredient> = listOf(), // Ingredients for the recipe
    val recipeType: RecipeType = RecipeType.PERSONAL, // Type of recipe
    val workInProgress: Boolean = false // Whether the recipe is still a draft
) {
    companion object {
        /** The maximum number of servings a recipe can have. */
        const val MAX_SERVINGS: Float = 20.0f
    }
}

/**
 * Data class representing a recipe prompt used to query the recipe generation model.
 *
 * @property name Name of the recipe.
 * @property recipeType Type of the recipe to generate, defaulting to BASIC.
 * @property specialInstruction Any special instruction or note for the recipe generation.
 * @property ingredients List of food items to include in the recipe.
 * @property missingIngredients List of ingredient names that are not currently available.
 * @property servings Desired number of servings for the generated recipe.
 * @property shortDuration Indicates whether the recipe should be quick to prepare.
 * @property onlyHouseHoldItems Whether to prioritize only items available in the household.
 * @property prioritiseSoonToExpire Whether to prioritize ingredients close to expiration.
 * @property macros Target macronutrient distribution for the recipe.
 */
data class RecipePrompt(
    val name: String,
    val recipeType: RecipeType = RecipeType.BASIC,
    val specialInstruction: String = "",
    val ingredients: List<FoodItem> = listOf(),
    val missingIngredients: List<String> = listOf(),
    val servings: Float = 1.0f,
    val shortDuration: Boolean = false,
    val onlyHouseHoldItems: Boolean = false,
    val prioritiseSoonToExpire: Boolean = true,
    val macros: NutritionFacts = NutritionFacts()
)

/**
 * Data class representing an ingredient in a recipe.
 *
 * @property name Name of the ingredient.
 * @property quantity Quantity of the ingredient needed.
 * @property macros Macronutrient information for the ingredient. This helps in estimating
 * the nutritional profile of the recipe and allows dynamic updates when ingredients are adjusted.
 */
data class Ingredient(
    val name: String,
    val quantity: Quantity,
    val macros: NutritionFacts = NutritionFacts() // Macronutrient information for the ingredient
)

/**
 * Enum class representing the type of a recipe.
 *
 * The type determines the characteristics or focus of the recipe, such as being high in protein
 * or low in calories.
 */
enum class RecipeType {
    BASIC, // A standard recipe
    HIGH_PROTEIN, // A recipe focused on high protein content
    LOW_CALORIE, // A recipe focused on low calorie content
    PERSONAL; // A user-defined or custom recipe

    /**
     * Converts the enum name to a user-friendly string.
     *
     * @return The formatted string representation of the recipe type.
     */
    override fun toString(): String {
        return name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}