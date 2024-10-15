package com.android.shelfLife.model.foodItem

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.google.firebase.Timestamp

/**
 * Represents details of a specific food item in a user's household.
 *
 * The `FoodItem` class encapsulates all details that are specific to an instance of a product as it
 * exists in the user's possession, distinct from general product information.
 *
 * @property uid Unique identifier for the food item instance.
 * @property foodFacts General product information (e.g., name, barcode, nutritional facts) shared
 *   across all instances of this food item.
 * @property status Current status of the food item (e.g., OPEN, CLOSED, EXPIRED). Default: CLOSED.
 * @property location Physical location of the food item within the household (e.g., pantry,
 *   fridge). Default: PANTRY.
 * @property expiryDate Expiry date of the food item, if available.
 * @property openDate The date the food item was opened, if applicable.
 * @property buyDate The date the food item was purchased or added to inventory. Default: current
 *   time.
 */
data class FoodItem(
    val uid: String,
    val foodFacts: FoodFacts,
    val location: FoodStorageLocation = FoodStorageLocation.PANTRY,
    val expiryDate: Timestamp? = null,
    val openDate: Timestamp? = null,
    val buyDate: Timestamp = Timestamp.now(),
    val status: FoodStatus = FoodStatus.CLOSED
) {
    override fun toString(): String {
        return "Name: ${foodFacts.name}\n" +
                "Quantity: ${foodFacts.quantity}\n" +
                "Location: $location\n" +
                "Days before expiry: ${getRemainingDays()}\n"
    }

    /**
     * returns the remaining days until the food item expires.
     */
    fun getRemainingDays(): Long {
        val expiry = expiryDate?.toDate()?.time ?: return -1
        val today = Timestamp.now().toDate().time
        return (expiry - today) / (1000 * 60 * 60 * 24)
    }

    fun isExpired(): Boolean {
        return getRemainingDays() < 0
    }
}

/** This enum class represents the status of a food item. */
enum class FoodStatus {
  OPEN,
  CLOSED,
  EXPIRED
}

enum class FoodStorageLocation {
  PANTRY,
  FRIDGE,
  FREEZER,
}
