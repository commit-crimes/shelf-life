package com.android.shelfLife.ui.utils

import androidx.compose.ui.graphics.Color
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.ui.theme.expired
import com.android.shelfLife.ui.theme.expiresInALongTime
import com.android.shelfLife.ui.theme.expiresLater
import com.android.shelfLife.ui.theme.expiresSoon
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gets the expiry information including the user-friendly expiry message and progress bar state.
 *
 * @param expiryDateString The expiry date as a string in the `dd/MM/yyyy` format. Can be null or
 *   blank.
 * @param currentDate The current date for calculation. Defaults to today's date if not provided.
 * @return A pair containing the expiry message and a pair of the progress bar fill level and color.
 */
fun getExpiryInfo(
    expiryDateString: String?,
    currentDate: Date = Date()
): Pair<String, Pair<Float, Color>> {
  if (expiryDateString.isNullOrBlank()) {
    return Pair("No Expiry Date", Pair(0f, expired))
  }

  val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val expiryDate: Date =
      try {
        formatter.parse(expiryDateString)!!
      } catch (e: Exception) {
        return Pair("Invalid Date Format", Pair(0f, expired))
      }

  val todayCalendar = Calendar.getInstance().apply { time = currentDate }
  val expiryCalendar = Calendar.getInstance().apply { time = expiryDate }

  // Calculate the difference in days
  val timeDifference = expiryCalendar.timeInMillis - todayCalendar.timeInMillis
  val daysDifference = (timeDifference / (1000 * 60 * 60 * 24)).toInt()

  val expiryMessage = getExpiryMessageBasedOnDays(daysDifference, expiryDateString)
  val progressBarState = getProgressBarState(daysDifference)

  return Pair(expiryMessage, progressBarState)
}

// Function to get thresholds based on food category
fun getThresholdsForCategory(category: FoodCategory): Thresholds {
    return when (category) {
        FoodCategory.MEAT ->
            Thresholds(redThreshold = daysToSeconds(3), orangeThreshold = daysToSeconds(7))
        FoodCategory.FISH ->
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

/**
 * Generates a user-friendly expiry message based on the number of days remaining.
 *
 * @param daysDifference The number of days remaining until the expiry date.
 * @param expiryDateString The expiry date as a string in the `dd/MM/yyyy` format.
 * @return A string representing the expiry status.
 */
fun getExpiryMessageBasedOnDays(daysDifference: Int, expiryDateString: String): String {
  return when {
    daysDifference < 0 -> "Expired"
    daysDifference == 0 -> "Expires today"
    daysDifference == 1 -> "Expires tomorrow"
    daysDifference in 2..5 -> "Expires in $daysDifference days"
    daysDifference in 6..14 -> "Expires in a week"
    else -> "Expires on $expiryDateString"
  }
}

/**
 * Determines the progress bar state (fill level and color) based on the number of days remaining.
 *
 * @param daysDifference The number of days remaining until the expiry date.
 * @return A pair containing the progress bar fill level (0.0 to 1.0) and the corresponding color.
 */
fun getProgressBarState(daysDifference: Int): Pair<Float, Color> {
  return when {
    daysDifference < 0 -> Pair(0.25f, expired) // Red
    daysDifference <= 5 -> Pair(0.5f, expiresSoon) // Orange
    daysDifference <= 14 -> Pair(0.75f, expiresLater) // Yellow
    else -> Pair(1.0f, expiresInALongTime) // Green
  }
}
