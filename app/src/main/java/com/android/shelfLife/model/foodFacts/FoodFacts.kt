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
 * @property imageUrl The URL of the image representing the food item.
 */
data class FoodFacts(
    var name: String, // Name of the food item
    val barcode: String = "", // Barcode number
    val quantity: Quantity, // Quantity of the food item
    val category: FoodCategory = FoodCategory.OTHER, // Default category is OTHER
    val nutritionFacts: NutritionFacts = NutritionFacts(), // Default empty NutritionFacts object
    val imageUrl: String = DEFAULT_IMAGE_URL // New property for image URL
) {
    companion object {
        const val DEFAULT_IMAGE_URL =
            "https://media.istockphoto.com/id/1354776457/vector/default-image-icon-vector-missing-picture-page-for-website-design-or-mobile-app-no-photo.jpg?s=612x612&w=0&k=20&c=w3OW0wX3LyiFRuDHo9A32Q0IUMtD4yjXEvQlqyYk9O4="
    }

    override fun toString(): String {
        return "Name: $name\n" +
                "Barcode: $barcode\n" +
                "Quantity: $quantity\n" +
                "Category: ${category.name}\n" +
                "Nutrition facts: $nutritionFacts"
    }
}

/**
 * Represents the quantity of a food item.
 *
 * @property amount The amount of the quantity.
 * @property unit The unit of the quantity (e.g., GRAM, ML, COUNT).
 */
data class Quantity(
    var amount: Double, // Amount of the quantity
    val unit: FoodUnit = FoodUnit.GRAM // Unit of the quantity
) {
    override fun toString(): String {
        return when (unit) {
            FoodUnit.GRAM -> {
                if (amount >= 1000) {
                    val convertedAmount = amount / 1000
                    "${convertedAmount.toIntIfWhole()}kg"
                } else {
                    "${amount.toIntIfWhole()}g"
                }
            }
            FoodUnit.ML -> {
                if (amount >= 1000) {
                    val convertedAmount = amount / 1000
                    "${convertedAmount.toIntIfWhole()}L"
                } else {
                    "${amount.toIntIfWhole()}ml"
                }
            }
            FoodUnit.COUNT -> {
                "${amount.toInt()} in stock"
            }
        }
    }

    // Helper extension to display whole numbers without ".0"
    private fun Double.toIntIfWhole(): String {
        return if (this % 1.0 == 0.0) this.toInt().toString() else this.toString()
    }
}

/**
 * Enum class representing the unit of a food item's quantity.
 */
enum class FoodUnit {
    GRAM, // Gram unit
    ML, // Milliliter unit
    COUNT // Item count (2 pineapples, 3 apples, etc.)
}

/**
 * Enum class representing the category of a food item.
 */
enum class FoodCategory {
    FRUIT, // Fruit category
    VEGETABLE, // Vegetable category
    MEAT, // Meat category
    DAIRY, // Dairy category
    GRAIN, // Grain category
    BEVERAGE, // Beverage category
    SNACK, // Snack category
    FISH, // Fish category
    OTHER // Other category
}

/**
 * This data class represents the nutrition facts for a food item per 100g/100ml, with default
 * values.
 *
 * @property energyKcal The energy content in kilocalories.
 * @property fat The fat content in grams.
 * @property saturatedFat The saturated fat content in grams.
 * @property carbohydrates The carbohydrate content in grams.
 * @property sugars The sugar content in grams.
 * @property proteins The protein content in grams.
 * @property salt The salt content in grams.
 */
data class NutritionFacts(
    val energyKcal: Int = 0, // Default energy in kilocalories is 0Kcal if not provided
    val fat: Double = 0.0, // Default fat value is 0.0g if not provided
    val saturatedFat: Double = 0.0, // Default saturated fat value is 0.0g if not provided
    val carbohydrates: Double = 0.0, // Default carbohydrates is 0.0g if not provided
    val sugars: Double = 0.0, // Default sugars value is 0.0g if not provided
    val proteins: Double = 0.0, // Default proteins value is 0.0g
    val salt: Double = 0.0, // Default salt value is 0.0g
) {
    override fun toString(): String {
        return "Energy: $energyKcal Kcal\n" +
                "Fat: $fat g\n" +
                "Saturated fat: $saturatedFat g\n" +
                "Carbohydrates: $carbohydrates g\n" +
                "Sugars: $sugars g\n" +
                "Proteins: $proteins g\n" +
                "Salt: $salt g"
    }
}