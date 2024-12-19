package com.android.shelfLife.utilities

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity

/**
 * Converts a FoodFacts object into a Map for storing in Firestore.
 *
 * @return A Map representation of the FoodFacts object.
 */
fun FoodFacts.toMap(): Map<String, Any?> {
  return mapOf(
      "name" to name,
      "barcode" to barcode,
      "quantity" to quantity.toMap(),
      "category" to category.name,
      "nutritionFacts" to nutritionFacts.toMap(),
      "imageUrl" to imageUrl)
}

/**
 * Converts a Map into a FoodFacts object.
 *
 * @return A FoodFacts object created from the Map.
 */
fun Map<String, Any?>.toFoodFacts(): FoodFacts {
  val name = this["name"] as? String ?: ""
  val barcode = this["barcode"] as? String ?: ""
  val quantityMap = this["quantity"] as? Map<String, Any?>
  val quantity = quantityMap?.toQuantity() ?: Quantity(0.0)
  val categoryStr = this["category"] as? String
  val category =
      categoryStr?.let { com.android.shelfLife.model.foodFacts.FoodCategory.valueOf(it) }
          ?: com.android.shelfLife.model.foodFacts.FoodCategory.OTHER
  val nutritionFactsMap = this["nutritionFacts"] as? Map<String, Any?>
  val nutritionFacts = nutritionFactsMap?.toNutritionFacts() ?: NutritionFacts()
  val imageUrl = this["imageUrl"] as? String ?: FoodFacts.DEFAULT_IMAGE_URL

  return FoodFacts(
      name = name,
      barcode = barcode,
      quantity = quantity,
      category = category,
      nutritionFacts = nutritionFacts,
      imageUrl = imageUrl)
}

/**
 * Converts a Quantity object to a Map.
 *
 * @return A Map representation of the Quantity object.
 */
fun Quantity.toMap(): Map<String, Any?> {
  return mapOf("amount" to amount, "unit" to unit.name)
}

/**
 * Converts a Map into a Quantity object.
 *
 * @return A Quantity object created from the Map.
 */
fun Map<String, Any?>.toQuantity(): Quantity {
  val amount = (this["amount"] as? Double) ?: 0.0
  val unitStr = this["unit"] as? String
  val unit =
      unitStr?.let { com.android.shelfLife.model.foodFacts.FoodUnit.valueOf(it) }
          ?: com.android.shelfLife.model.foodFacts.FoodUnit.GRAM

  return Quantity(amount, unit)
}

/**
 * Converts a NutritionFacts object to a Map.
 *
 * @return A Map representation of the NutritionFacts object.
 */
fun NutritionFacts.toMap(): Map<String, Any?> {
  return mapOf(
      "energyKcal" to energyKcal,
      "fat" to fat,
      "saturatedFat" to saturatedFat,
      "carbohydrates" to carbohydrates,
      "sugars" to sugars,
      "proteins" to proteins,
      "salt" to salt)
}

/**
 * Converts a Map into a NutritionFacts object.
 *
 * @return A NutritionFacts object created from the Map.
 */
fun Map<String, Any?>.toNutritionFacts(): NutritionFacts {
  val energyKcal = (this["energyKcal"] as? Long)?.toInt() ?: 0
  val fat = this["fat"] as? Double ?: 0.0
  val saturatedFat = this["saturatedFat"] as? Double ?: 0.0
  val carbohydrates = this["carbohydrates"] as? Double ?: 0.0
  val sugars = this["sugars"] as? Double ?: 0.0
  val proteins = this["proteins"] as? Double ?: 0.0
  val salt = this["salt"] as? Double ?: 0.0

  return NutritionFacts(
      energyKcal = energyKcal,
      fat = fat,
      saturatedFat = saturatedFat,
      carbohydrates = carbohydrates,
      sugars = sugars,
      proteins = proteins,
      salt = salt)
}
