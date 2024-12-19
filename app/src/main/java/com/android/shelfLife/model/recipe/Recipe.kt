package com.android.shelfLife.model.recipe

import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import kotlin.time.Duration

/**
 * Data class representing a recipe object.
 *
 * @property uid Unique identifier for the recipe.
 * @property name Recipe name.
 * @property instructions Instructions of recipes step by step (hence the list).
 * @property servings Total number of servings.
 * @property time Time it takes to cook.
 * @property ingredients Ingredients in the recipe.
 * @property recipeType Type of the recipe.
 * @property workInProgress Indicates if the recipe is currently being worked on.
 */
data class Recipe(
    val uid: String, // unique identifier for the recipe
    val name: String, // recipe name
    val instructions: List<String>, // instructions of recipes step by step (hence the list)
    val servings: Float, // total number of servings
    val time: Duration, // time it takes to cook
    val ingredients: List<Ingredient> = listOf(), // ingredients in recipe
    val recipeType: RecipeType = RecipeType.PERSONAL,
    val workInProgress: Boolean = false, // if the recipe is currently being worked on
) {
    companion object {
        const val MAX_SERVINGS: Float = 20.0f
    }
}

/**
 * Data class representing a recipe prompt, used to query the Recipe generation model.
 *
 * @property name Name of the recipe.
 * @property recipeType Type of the recipe.
 * @property specialInstruction Special instructions for the recipe.
 * @property ingredients List of ingredients for the recipe.
 * @property missingIngredients List of missing ingredients for the recipe.
 * @property servings Number of servings for the recipe.
 * @property shortDuration Indicates if the recipe has a short duration.
 * @property onlyHouseHoldItems Indicates if only household items should be used.
 * @property prioritiseSoonToExpire Indicates if soon-to-expire items should be prioritized.
 * @property macros Nutritional facts of the recipe.
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
 * @property quantity Quantity of the ingredient.
 * @property macros Nutritional facts of the ingredient.
 */
data class Ingredient(
    val name: String,
    val quantity: Quantity,
    val macros: NutritionFacts =
        NutritionFacts() // need to save base macros to estimate correctly the macros of the recipe
    // (and allow dynamic updates to macros when executing a recipe)
)

/**
 * Enum class representing the type of a recipe.
 */
enum class RecipeType {
    BASIC,
    HIGH_PROTEIN,
    LOW_CALORIE,
    PERSONAL;

    /**
     * Returns the string representation of the enum value.
     *
     * @return The string representation of the enum value.
     */
    override fun toString(): String {
        return name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}