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
    val recipeType: RecipeType = RecipeType.PERSONAL,
    val workInProgress: Boolean = false, // if the recipe is currently being worked on
) {
  companion object {
    const val MAX_SERVINGS: Float = 20.0f
  }
}

/** Data class representing a recipe prompt, that we use to query the Recipe generation model */
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

data class Ingredient(
    val name: String,
    val quantity: Quantity,
    val macros: NutritionFacts =
        NutritionFacts() // need to save base macros to estimate correctly the macros of the recipe
    // (and allow dynamic updates to macros when executing a recipe)
)

enum class RecipeType {
  BASIC,
  HIGH_PROTEIN,
  LOW_CALORIE,
  PERSONAL;

  override fun toString(): String {
    return name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
  }
}
