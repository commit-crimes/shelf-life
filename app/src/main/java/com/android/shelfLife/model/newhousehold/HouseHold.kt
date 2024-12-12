package com.android.shelfLife.model.newhousehold

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
    val sharedRecipes: List<String>, // List of shared recipe IDs
    val ratPoints: Map<String, Long>, // Map of member IDs to their respective rat points
    val stinkyPoints : Map<String, Long> // Map of member IDs to their respective stinky points
)
