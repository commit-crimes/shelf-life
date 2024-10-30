package com.android.shelfLife.ui.utils

import androidx.compose.ui.graphics.Color
import com.android.shelfLife.model.foodFacts.FoodCategory

// Data class to hold the red and orange thresholds
data class Thresholds(val redThreshold: Long, val orangeThreshold: Long)

// Function to convert days to seconds
fun daysToSeconds(days: Int): Long {
  return days * 24L * 60 * 60
}

// Function to get thresholds based on food category
fun getThresholdsForCategory(category: FoodCategory): Thresholds {
  return when (category) {
    FoodCategory.MEAT ->
        Thresholds(redThreshold = daysToSeconds(3), orangeThreshold = daysToSeconds(7))
    FoodCategory.FRUIT ->
        Thresholds(redThreshold = daysToSeconds(2), orangeThreshold = daysToSeconds(5))
    FoodCategory.VEGETABLE ->
        Thresholds(redThreshold = daysToSeconds(3), orangeThreshold = daysToSeconds(7))
    FoodCategory.DAIRY ->
        Thresholds(redThreshold = daysToSeconds(5), orangeThreshold = daysToSeconds(10))
    FoodCategory.GRAIN ->
        Thresholds(redThreshold = daysToSeconds(30), orangeThreshold = daysToSeconds(90))
    FoodCategory.BEVERAGE ->
        Thresholds(redThreshold = daysToSeconds(30), orangeThreshold = daysToSeconds(90))
    FoodCategory.SNACK ->
        Thresholds(redThreshold = daysToSeconds(15), orangeThreshold = daysToSeconds(30))
    FoodCategory.OTHER ->
        Thresholds(redThreshold = daysToSeconds(7), orangeThreshold = daysToSeconds(14))
  }
}

// Function to determine progress bar fill level and color
fun getProgressBarState(timeRemaining: Long, thresholds: Thresholds): Pair<Float, Color> {
  return when {
    timeRemaining <= 0 -> Pair(1f, Color(0xFFF44336)) // Red, expired, full bar
    timeRemaining <= thresholds.redThreshold -> Pair(1f, Color(0xFFF44336)) // Red, full bar
    timeRemaining <= thresholds.orangeThreshold -> Pair(0.5f, Color(0xFFFFA500)) // Orange, half bar
    else -> Pair(0.25f, Color(0xFF4CAF50)) // Green, quarter bar
  }
}
