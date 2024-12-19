package com.android.shelfLife.model.foodItem

import com.android.shelfLife.utilities.toFoodFacts
import com.android.shelfLife.utilities.toMap
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Converts a `FoodItem` object to a `Map` for Firestore.
 *
 * @return A `Map` representation of the `FoodItem`.
 */
fun FoodItem.toMap(): Map<String, Any?> {
    return mapOf(
        "uid" to uid,
        "foodFacts" to foodFacts.toMap(),
        "buyDate" to buyDate,
        "expiryDate" to expiryDate,
        "openDate" to openDate,
        "location" to location.name,
        "status" to status.name,
        "owner" to owner)
}

/**
 * Converts a `DocumentSnapshot` to a `FoodItem` object.
 *
 * @return A `FoodItem` object if the conversion is successful, otherwise `null`.
 */
fun DocumentSnapshot.toFoodItem(): FoodItem? {
    val data = data ?: return null

    val uid = getString("uid") ?: return null
    val foodFactsMap = data["foodFacts"] as? Map<String, Any?>
    val foodFacts = foodFactsMap?.toFoodFacts() ?: return null

    val buyDate = getTimestamp("buyDate")
    val expiryDate = getTimestamp("expiryDate")
    val openDate = getTimestamp("openDate")
    val locationStr = getString("location")
    val location = locationStr?.let { FoodStorageLocation.valueOf(it) } ?: FoodStorageLocation.OTHER
    val statusStr = getString("status")
    val status = statusStr?.let { FoodStatus.valueOf(it) } ?: FoodStatus.UNOPENED
    val owner = getString("owner") ?: ""

    return FoodItem(
        uid = uid,
        foodFacts = foodFacts,
        buyDate = buyDate,
        expiryDate = expiryDate,
        openDate = openDate,
        location = location,
        status = status,
        owner = owner)
}