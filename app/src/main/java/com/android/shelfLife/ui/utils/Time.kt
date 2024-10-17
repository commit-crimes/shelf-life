package com.android.shelfLife.ui.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * converts a Timestamp into an Int representing the minutes
 *
 * @param timestamp: the timestamp we want to convert
 */
fun getTotalMinutes(timestamp: Timestamp): Int {
  // Convert nanoseconds to seconds and add to the total seconds
  val totalSeconds = timestamp.seconds + timestamp.nanoseconds / 1_000_000_000.0
  // Convert total seconds to minutes
  return (totalSeconds / 60).toInt()
}

/**
 * Coverts from Timestamp type to a string in for dd/MM/yyyy
 *
 * @param timestamp: Timestamp to covert
 * @return String
 */
fun formatTimestampToDate(timestamp: Timestamp): String {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return sdf.format(timestamp.toDate())
}

/**
 * Converts from a string to a type Timestamp for dd/MM/yyyy
 *
 * @param dateString: String in dd/MM/yyyy
 * @return Timestamp
 * @throws IllegalArgumentException if the date string is not valid
 */
fun formatDateToTimestamp(dateString: String): Timestamp {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val date = sdf.parse(dateString) ?: throw IllegalArgumentException("Invalid date format: $dateString")

  return Timestamp(date)
}

/**
 * Coverts a string into a lowercase with its first letter as Capital Letter
 *
 * @param enum: string
 * @return string with capital letter and lowercase
 */
fun fromCapitalStringToLowercaseString(enum: String): String {
  return enum.lowercase().replaceFirstChar { it.uppercase() }
}
