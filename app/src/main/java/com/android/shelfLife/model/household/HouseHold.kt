package com.android.shelfLife.model.household

import com.android.shelfLife.model.foodItem.FoodItem

/**
 * Data class representing a household.
 * @property uid Unique ID of the household
 * @property name Name of the household
 * @property members List of members in the household
 * @property foodItems List of food items in the household
 */
data class HouseHold(
    val uid: String, // Unique ID of the household
    var name: String, // Name of the household
    val members: List<String>, // List of members in the household, might become user IDs later
    val foodItems: List<FoodItem> // List of food items in the household
)
