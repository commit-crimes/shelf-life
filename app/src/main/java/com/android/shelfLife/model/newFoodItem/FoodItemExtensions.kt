package com.android.shelfLife.model.newFoodItem

import com.android.shelfLife.utils.*
import com.google.firebase.firestore.DocumentSnapshot

// Convert FoodItem to a Map for Firestore
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

// Convert DocumentSnapshot to FoodItem
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
