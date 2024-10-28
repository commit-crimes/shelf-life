package com.android.shelfLife.model.recipe

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodItem.FoodItem
import kotlin.time.Duration

data class Recipe(
    val name: String, // recipe name
    val instructions: List<String>, // instructions of recipes step by step (hence the list)
    val servings: Int, // total number of servings
    val time: Duration, // time it takes to cook
    val ingredients: List<Ingredient> =
        listOf(), // ingredients in recipe todo check with Alex about this
)

data class Ingredient(
    val foodFacts: FoodFacts,
    val isOwned: Boolean // false when the user does not have the ingredient (i.e. need to buy)
)
