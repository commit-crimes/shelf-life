package com.android.shelfLife.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
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

// Custom VisualTransformation with proper OffsetMapping
class DateVisualTransformation : VisualTransformation {

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

// Helper function to get error message for date input
fun getDateErrorMessage(dateStr: String, isRequired: Boolean = true): String? {
  if (dateStr.isEmpty()) {
    return if (isRequired) "Date cannot be empty" else null
  }
  if (dateStr.length != 8) {
    return "Incomplete date"
  }
  val formattedDateStr = insertSlashes(dateStr)
  return if (isValidDate(formattedDateStr)) null else "Invalid date"
}

// Function to insert slashes into the date string
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

// Function to validate date in dd/MM/yyyy format without using exceptions
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

  if (day !in 1..daysInMonth) {
    return false
  }

  // Additional checks can be added (e.g., year range)
  return true
}

// Function to check if a date is not in the past
fun isValidDateNotPast(dateStr: String): Boolean {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val date = sdf.parse(insertSlashes(dateStr)) ?: return false
  val today = sdf.parse(sdf.format(Date())) ?: return false
  return !date.before(today)
}


// Helper function to check if a year is a leap year
fun isLeapYear(year: Int): Boolean {
  return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))
}

// Function to compare two dates (returns true if date1 >= date2)
fun isDateAfterOrEqual(dateStr1: String, dateStr2: String): Boolean {
  val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val date1 = sdf.parse(insertSlashes(dateStr1)) ?: return false
  val date2 = sdf.parse(insertSlashes(dateStr2)) ?: return false
  return !date1.before(date2)
}

// Function to convert a string date to Timestamp, handling exceptions
fun formatDateToTimestamp(dateString: String): Timestamp? {
  return try {
    val formattedDateStr = insertSlashes(dateString)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = sdf.parse(formattedDateStr)
    if (date != null) Timestamp(date) else null
  } catch (e: Exception) {
    null
  }
}

// Function to format a Timestamp to a date string (stored as digits without slashes)
fun formatTimestampToDate(timestamp: Timestamp): String {
  val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
  return sdf.format(timestamp.toDate())
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

