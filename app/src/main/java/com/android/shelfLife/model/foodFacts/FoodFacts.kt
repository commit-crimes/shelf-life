package com.android.shelfLife.model.foodFacts

/**
 * Represents intrinsic details of a food item.
 *
 * This data class encapsulates all the general and unchanging information about a food item, such
 * as its name, barcode, nutritional facts, quantity, and category. The `FoodFacts` class provides a
 * standardized way to handle product-level data that remains consistent across different instances
 * of the same food item.
 *
 * @property name The name of the food product.
 * @property barcode The unique barcode identifier for the product.
 * @property quantity The quantity of the product (e.g., weight, volume, or count).
 * @property category The category of the food item (e.g., FRUIT, VEGETABLE, MEAT).
 * @property nutritionFacts Detailed nutritional information per 100g/ml of the product.
 */
data class FoodFacts(
    val name: String, // Name of the food item
    val barcode: String = "", // Barcode number
    val quantity: Quantity, // Quantity of the food item
    val category: FoodCategory = FoodCategory.OTHER, // Default category is OTHER
    val nutritionFacts: NutritionFacts = NutritionFacts() // Default empty NutritionFacts object
)

data class Quantity(
    val amount: Double, // Amount of the quantity
    val unit: FoodUnit = FoodUnit.GRAM // Unit of the quantity
)

enum class FoodUnit {
  GRAM, // Gram unit
  ML, // Milliliter unit
  COUNT // Item count (2 pineapples, 3 apples, etc.)
}

enum class FoodCategory {
  FRUIT, // Fruit category
  VEGETABLE, // Vegetable category
  MEAT, // Meat category
  DAIRY, // Dairy category
  GRAIN, // Grain category
  BEVERAGE, // Beverage category
  SNACK, // Snack category
  OTHER // Other category
}

/**
 * This data class represents the nutrition facts for a food item per 100g/100ml, with default
 * values.
 */
data class NutritionFacts(
    val energyKcal: Int = 0, // Default energy in kilocalories is 0Kcal if not provided
    val fat: Double = 0.0, // Default fat value is 0.0g if not provided
    val saturatedFat: Double = 0.0, // Default saturated fat value is 0.0g if not provided
    val carbohydrates: Double = 0.0, // Default carbohydrates is 0.0g if not provided
    val sugars: Double = 0.0, // Default sugars value is 0.0g if not provided
    val proteins: Double = 0.0, // Default proteins value is 0.0g
    val salt: Double = 0.0, // Default salt value is 0.0g
)
