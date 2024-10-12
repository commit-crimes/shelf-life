package com.android.shelfLife.model.recipe

import com.android.shelfLife.model.foodItem.FoodItem
import com.google.firebase.Timestamp
import kotlin.time.Duration

data class Recipe(
    val name: String, // name of recipe
    val instructions: String, // instructions of recipes
    val servings: Int, // total number of servings
    val time: Duration, // time it take to cook
    val ingredients : List<FoodItem> = listOf(), // ingredients in recipe todo check with Alex about this
)
