package com.android.shelfLife.model.household

/**
 * Data class representing a household in the Shelf Life app.
 *
 * This class contains information about a household, including its unique ID, name,
 * members, shared recipes, and point systems for tracking member contributions or actions.
 *
 * @property uid Unique identifier of the household.
 * @property name Name of the household, which can be modified by members.
 * @property members List of member IDs associated with the household.
 * @property sharedRecipes List of shared recipe IDs that are accessible to all members.
 * @property ratPoints Map associating member IDs with their respective "rat points,"
 * which track food-related actions like taking food from others.
 * @property stinkyPoints Map associating member IDs with their respective "stinky points,"
 * which track actions like letting items expire.
 */
data class HouseHold(
    val uid: String, // Unique identifier of the household.
    var name: String, // Name of the household, can be updated.
    val members: List<String>, // List of member IDs in the household.
    val sharedRecipes: List<String>, // List of recipe IDs shared among members.
    val ratPoints: Map<String, Long>, // Map of member IDs to their respective "rat points."
    val stinkyPoints: Map<String, Long> // Map of member IDs to their respective "stinky points."
)