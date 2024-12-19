package com.android.shelfLife.model.foodItem

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.google.firebase.Timestamp

/**
 * Represents a food item associated with a specific household.
 *
 * @property uid Unique identifier for the food item.
 * @property foodFacts General information about the food product.
 * @property buyDate Date when the item was purchased.
 * @property expiryDate Date when the item expires.
 * @property openDate Date when the item was opened.
 * @property location Storage location within the household.
 * @property status Current status of the item.
 * @property owner UID of the user who added the item.
 */
data class FoodItem(
    val uid: String, // Unique identifier for the food item
    val foodFacts: FoodFacts, // General information about the food product
    val buyDate: Timestamp? = Timestamp.now(), // Date when the item was purchased
    val expiryDate: Timestamp? = null, // Date when the item expires
    val openDate: Timestamp? = null, // Date when the item was opened
    val location: FoodStorageLocation = FoodStorageLocation.OTHER, // Storage location within the household
    val status: FoodStatus = FoodStatus.UNOPENED, // Current status of the item
    val owner: String // UID of the user who added the item
)

/** Represents the status of a food item. */
enum class FoodStatus {
    UNOPENED, // The item is unopened
    OPENED, // The item is opened
    CONSUMED, // The item is consumed
    EXPIRED // The item is expired
}

/** Represents storage locations within the household. */
enum class FoodStorageLocation {
    PANTRY, // The item is stored in the pantry
    FRIDGE, // The item is stored in the fridge
    FREEZER, // The item is stored in the freezer
    OTHER // The item is stored in another location
}