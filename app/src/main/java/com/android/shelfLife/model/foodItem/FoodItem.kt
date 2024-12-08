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
    val uid: String,
    val foodFacts: FoodFacts,
    val buyDate: Timestamp? = Timestamp.now(),
    val expiryDate: Timestamp? = null,
    val openDate: Timestamp? = null,
    val location: FoodStorageLocation,
    val status: FoodStatus = FoodStatus.UNOPENED,
    val owner: String
)

/** Represents the status of a food item. */
enum class FoodStatus {
  UNOPENED,
  OPENED,
  CONSUMED,
  EXPIRED
}

/** Represents storage locations within the household. */
enum class FoodStorageLocation {
  PANTRY,
  FRIDGE,
  FREEZER,
  OTHER
}
