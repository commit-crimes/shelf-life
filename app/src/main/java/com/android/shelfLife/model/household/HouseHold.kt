package com.android.shelfLife.model.household

/**
 * Data class representing a household.
 *
 * @property uid Unique ID of the household
 * @property name Name of the household
 * @property members List of member IDs in the household
 * @property sharedRecipes List of shared recipe IDs
 */
data class HouseHold(
    val uid: String, // Unique ID of the household
    var name: String, // Name of the household
    val members: List<String>, // List of member IDs in the household
    val sharedRecipes: List<String> // List of shared recipe IDs
)
