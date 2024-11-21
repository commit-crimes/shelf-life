package com.android.shelfLife.model.recipe

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import kotlin.time.Duration

data class Recipe(
    val uid: String, // unique identifier for the recipe
    val name: String, // recipe name
    val instructions: List<String>, // instructions of recipes step by step (hence the list)
    val servings: Float, // total number of servings
    val time: Duration, // time it takes to cook
    val ingredients: List<Ingredient> =
        listOf(), // ingredients in recipe
)

data class Ingredient(
    val name: String,
    val quantity: Quantity,
    val macros : NutritionFacts //need to save base macros to estimate correctly the macros of the recipe (and allow dynamic updates to macros when executing a recipe)
)

data class RecipePrompt (
    val name: String,
    val specialInstruction: String,
    val ingredients: List<FoodItem>,
    val missingIngredients: List<String>,
    val servings: Float,
    val shortDuration: Boolean,
    val macros : NutritionFacts
)
