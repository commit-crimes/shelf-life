package com.android.shelfLife.model.foodItem
import com.google.firebase.Timestamp

data class FoodItem(
    val uid: String, // Unique ID of the food item
    val name: String, // Name of the food item
    val barcode: String, // Barcode number, default to an empty string if not provided
    val quantity: Quantity,// Quantity of the food item
    val expiryDate: Timestamp? = null, // Expiry date can be null if not provided
    val buyDate: Timestamp = Timestamp.now(), // Default buy date is the current time
    val status: FoodItemStatus = FoodItemStatus.CLOSED, // Default status is CLOSED
    val nutritionFacts: NutritionFacts = NutritionFacts() // Default empty NutritionFacts object
)

data class Quantity(
    val amount: Double, // Amount of the quantity
    val unit: Unit = Unit.GRAM // Unit of the quantity
)

enum class Unit {
    GRAM, // Gram unit
    ML, // Milliliter unit
    COUNT // Item count (2 pineapples, 3 apples, etc.)
}

/** This enum class represents the status of a food item. */
enum class FoodItemStatus {
    OPEN, // Food item is opened
    CLOSED, // Food item is closed or sealed
    FROZEN, // Food item is frozen
    EXPIRED // Food item has expired
}

/** This data class represents the nutrition facts for a food item per 100g/100ml, with default values. */
data class NutritionFacts(
    val energyKcal: Int = 0, // Default energy in kilocalories per 100g/ml
    val fat: Double = 0.0, // Default fat value is 0.0 if not provided
    val saturatedFat: Double = 0.0, // Default saturated fat value is 0.0 if not provided
    val carbohydrates: Double = 0.0, // Default carbohydrates is 0.0 if not provided
    val sugars: Double = 0.0, // Default sugars value is 0.0 if not provided
    val proteins: Double = 0.0, // Default proteins value is 0.0
    val salt: Double = 0.0, // Default salt value is 0.0
)
