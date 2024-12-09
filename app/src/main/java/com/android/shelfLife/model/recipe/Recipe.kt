package com.android.shelfLife.model.recipe

import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import kotlin.time.Duration

/** Data class representing a recipe object */
data class Recipe(
    val uid: String, // unique identifier for the recipe
    val name: String, // recipe name
    val instructions: List<String>, // instructions of recipes step by step (hence the list)
    val servings: Float, // total number of servings
    val time: Duration, // time it takes to cook
    val ingredients: List<Ingredient> = listOf(), // ingredients in recipe
    val recipeType: RecipeType = RecipeType.USE_SOON_TO_EXPIRE
)

/** Data class representing a recipe prompt, that we use to query the Recipe generation model */
data class RecipePrompt(
    val name: String,
    val recipeType: RecipeType = RecipeType.USE_SOON_TO_EXPIRE,
    val specialInstruction: String = "",
    val ingredients: List<FoodItem> = listOf(),
    val missingIngredients: List<String> = listOf(),
    val servings: Float = 1.0f,
    val shortDuration: Boolean = false,
    val macros: NutritionFacts = NutritionFacts()
)

data class Ingredient(
    val name: String,
    val quantity: Quantity,
    val macros: NutritionFacts =
        NutritionFacts() // need to save base macros to estimate correctly the macros of the recipe
    // (and allow dynamic updates to macros when executing a recipe)
)

enum class RecipeType {
  USE_SOON_TO_EXPIRE,
  USE_ONLY_HOUSEHOLD_ITEMS,
  HIGH_PROTEIN,
  LOW_CALORIE,
  PERSONAL
}
