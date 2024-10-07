package com.android.shelfLife.model.foodItem

import com.google.firebase.Timestamp

data class FoodItem(
    val uid: String, // Unique ID of the food item
    val name: String, // Name of the food item
    val barcode: String = "", // Barcode number, default to an empty string if not provided
    val quantity: Quantity, // Quantity of the food item
    val status: FoodStatus = FoodStatus.CLOSED, // Default status is CLOSED
    val nutritionFacts: NutritionFacts = NutritionFacts() // Default empty NutritionFacts object,
    val foodCategory: FoodCategory = FoodCategory.OTHER, // Default category is OTHER
    val location: FoodLocation = FoodLocation(0, _Location.PANTRY), // Default location is PANTRY
    val expiryDate: Timestamp? = null, // Expiry date can be null if not provided
    val openDate: Timestamp? = null, // Expiry date can be null if not provided
    val buyDate: Timestamp = Timestamp.now(), // Default buy date is the current time
)

data class Quantity(
    val amount: Double, // Amount of the quantity
    val unit: _Unit = _Unit.GRAM // Unit of the quantity
)

data class FoodLocation(
    val householdNumber: Int, // Household number
    val location: _Location // Location can be PANTRY, FRIDGE, or FREEZER
)

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

/** This enum class represents the status of a food item. */
enum class FoodStatus {
    OPEN, // Food item is opened
    CLOSED, // Food item is closed or sealed
    EXPIRED // Food item has expired
}

/**
 * This data class represents the nutrition facts for a food item per 100g/100ml, with default
 * values.
 */
data class NutritionFacts(
    val energyKcal: Int = 0, // Default energy in kilocalories per 100g/ml
    val fat: Double = 0.0, // Default fat value is 0.0 if not provided
    val saturatedFat: Double = 0.0, // Default saturated fat value is 0.0 if not provided
    val carbohydrates: Double = 0.0, // Default carbohydrates is 0.0 if not provided
    val sugars: Double = 0.0, // Default sugars value is 0.0 if not provided
    val proteins: Double = 0.0, // Default proteins value is 0.0
    val salt: Double = 0.0, // Default salt value is 0.0
)


enum class _Location {
    PANTRY, // Pantry location
    FRIDGE, // Fridge location
    FREEZER, // Freezer location
}

enum class _Unit {
    GRAM, // Gram unit
    ML, // Milliliter unit
    COUNT // Item count (2 pineapples, 3 apples, etc.)
}
