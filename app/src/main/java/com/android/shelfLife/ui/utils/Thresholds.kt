package com.android.shelfLife.ui.utils

import androidx.compose.ui.graphics.Color
import com.android.shelfLife.ui.theme.expired
import com.android.shelfLife.ui.theme.expiresInALongTime
import com.android.shelfLife.ui.theme.expiresLater
import com.android.shelfLife.ui.theme.expiresSoon

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
