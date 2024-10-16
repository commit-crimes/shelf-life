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
  return (timestamp.seconds / 60).toInt() // Convert seconds to minutes
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
 * Coverts from a string to a type Timestamp for dd/MM/yyyy
 *
 * @param dateString: String in dd/MM/yyyy
 * @return
 */
fun formatDateToTimestamp(dateString: String): Timestamp {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val date = sdf.parse(dateString)
  return Timestamp(date)
}

/**
 * Coverts a string into a lowercase with its first letter as Capital Letter
 *
 * @param enum: string
 * @return string with capital letter and lowercase
 */
fun fromCapitalStringtoLowercaseString(enum: String): String {
  return enum.lowercase().replaceFirstChar { it.uppercase() }
}
