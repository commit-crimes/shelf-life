package com.android.shelfLife.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.android.shelfLife.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts a Timestamp into an Int representing the minutes.
 *
 * @param timestamp The timestamp to convert.
 * @return The total minutes as an Int.
 */
fun getTotalMinutes(timestamp: Timestamp): Int {
  // Convert nanoseconds to seconds and add to the total seconds
  val totalSeconds = timestamp.seconds + timestamp.nanoseconds / 1_000_000_000.0
  // Convert total seconds to minutes
  return (totalSeconds / 60).toInt()
}

/** Custom VisualTransformation with proper OffsetMapping for date input. */
class DateVisualTransformation : VisualTransformation {

  /**
   * Filters the input text to format it as a date with slashes.
   *
   * @param text The input text to transform.
   * @return The transformed text with slashes and the offset mapping.
   */
  override fun filter(text: AnnotatedString): TransformedText {
    // Remove any non-digit characters
    val digits = text.text.filter { it.isDigit() }

    // Build the formatted text with slashes
    val formattedText = buildString {
      for (i in digits.indices) {
        append(digits[i])
        if ((i == 1 || i == 3) && i != digits.lastIndex) {
          append('/')
        }
      }
    }

    // Create an OffsetMapping for the cursor position
    val offsetMapping =
      object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
          var transformedOffset = offset
          if (offset > 2) transformedOffset++
          if (offset > 4) transformedOffset++
          return transformedOffset.coerceAtMost(formattedText.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
          var originalOffset = offset
          if (offset > 2) originalOffset--
          if (offset > 5) originalOffset--
          return originalOffset.coerceAtMost(digits.length)
        }
      }

    return TransformedText(AnnotatedString(formattedText), offsetMapping)
  }
}

/**
 * Helper function to get error message resource ID for date input.
 *
 * @param dateStr The date string to validate.
 * @param isRequired Whether the date is required.
 * @return The resource ID of the error message if the date is invalid, null otherwise.
 */
fun getDateErrorMessageResId(dateStr: String, isRequired: Boolean = true): Int? {
  if (dateStr.isEmpty()) {
    return if (isRequired) R.string.date_empty_error else null
  }
  if (dateStr.length != 8) {
    return R.string.date_incomplete_error
  }
  val formattedDateStr = insertSlashes(dateStr)
  return if (isValidDate(formattedDateStr)) null else R.string.date_invalid_error
}

/**
 * Function to insert slashes into the date string.
 *
 * @param input The input date string without slashes.
 * @return The formatted date string with slashes.
 */
fun insertSlashes(input: String): String {
  // Input is expected to be up to 8 digits
  val sb = StringBuilder()
  val digits = input.take(8) // Ensure no more than 8 digits
  for (i in digits.indices) {
    sb.append(digits[i])
    if ((i == 1 || i == 3) && i != digits.lastIndex) {
      sb.append('/')
    }
  }
  return sb.toString()
}

/**
 * Function to validate date in dd/MM/yyyy format without using exceptions.
 *
 * @param dateStr The date string to validate.
 * @return True if the date is valid, false otherwise.
 */
fun isValidDate(dateStr: String): Boolean {
  // Check if the dateStr matches the pattern dd/MM/yyyy
  val datePattern = Regex("""\d{2}/\d{2}/\d{4}""")
  if (!datePattern.matches(dateStr)) {
    return false
  }

  val parts = dateStr.split("/")
  val day = parts[0].toIntOrNull() ?: return false
  val month = parts[1].toIntOrNull() ?: return false
  val year = parts[2].toIntOrNull() ?: return false

  // Check if month is valid
  if (month !in 1..12) {
    return false
  }

  // Check if day is valid for the given month
  val daysInMonth =
    when (month) {
      4,
      6,
      9,
      11 -> 30
      2 -> if (isLeapYear(year)) 29 else 28
      else -> 31
    }

  return day in 1..daysInMonth

  // Additional checks can be added (e.g., year range)
}

/**
 * Function to check if a date is not in the past.
 *
 * @param dateStr The date string to check.
 * @return True if the date is not in the past, false otherwise.
 */
fun isValidDateNotPast(dateStr: String): Boolean {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  sdf.isLenient = false
  val date = sdf.parse(insertSlashes(dateStr)) ?: return false
  val today = sdf.parse(sdf.format(Date())) ?: return false
  return !date.before(today)
}

/**
 * Helper function to check if a year is a leap year.
 *
 * @param year The year to check.
 * @return True if the year is a leap year, false otherwise.
 */
fun isLeapYear(year: Int): Boolean {
  return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))
}

/**
 * Function to compare two dates.
 *
 * @param dateStr1 The first date string.
 * @param dateStr2 The second date string.
 * @return True if date1 is after or equal to date2, false otherwise.
 */
fun isDateAfterOrEqual(dateStr1: String, dateStr2: String): Boolean {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  sdf.isLenient = false // Strict date parsing

  val date1 =
    try {
      sdf.parse(insertSlashes(dateStr1))
    } catch (e: Exception) {
      return false // dateStr1 is invalid
    }

  val date2 =
    try {
      sdf.parse(insertSlashes(dateStr2))
    } catch (e: Exception) {
      return false // dateStr2 is invalid
    }

  return !date1.before(date2)
}

/**
 * Function to convert a string date to Timestamp, handling exceptions.
 *
 * @param dateString The date string to convert.
 * @return The corresponding Timestamp, or null if the date is invalid.
 */
fun formatDateToTimestamp(dateString: String): Timestamp? {
  return try {
    val formattedDateStr = insertSlashes(dateString)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.isLenient = false
    val date = sdf.parse(formattedDateStr)
    if (date != null) Timestamp(date) else null
  } catch (e: Exception) {
    null
  }
}

/**
 * Function to format a Timestamp to a date string.
 *
 * @param timestamp The Timestamp to format.
 * @return The formatted date string (stored as digits without slashes).
 */
fun formatTimestampToDate(timestamp: Timestamp): String {
  val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
  return sdf.format(timestamp.toDate())
}

/**
 * Function to format a Timestamp to a date string to be displayed (includes the slashes).
 *
 * @param timestamp The Timestamp to format.
 * @return The formatted date string with slashes.
 */
fun formatTimestampToDisplayDate(timestamp: Timestamp): String {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return sdf.format(timestamp.toDate())
}

/**
 * Converts a string into a lowercase with its first letter as a capital letter.
 *
 * @param enum The string to convert.
 * @return The converted string with a capital letter and lowercase.
 */
fun fromCapitalStringToLowercaseString(enum: String): String {
  return enum.lowercase().replaceFirstChar { it.uppercase() }
}