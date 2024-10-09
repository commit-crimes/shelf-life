package com.android.shelfLife.model.recipe

import com.google.firebase.Timestamp

data class Recipe(
    val name : String, // name of recipe
    //val ingredients : List<FoodItem>, // ingredients in recipe todo check with Alex about this
    val instructions : String, // instructions of recipes
    val servings : Int, // total number of servings
    val time : Timestamp // time it take to cook
)