package com.android.shelfLife.model.household

import com.android.shelfLife.model.foodItem.FoodItem

data class HouseHold(
    val uid: String, // Unique ID of the household
    var name: String, // Name of the household
    val members: List<String>, // List of members in the household, might become user IDs later
    val foodItems: List<FoodItem> // List of food items in the household
)
