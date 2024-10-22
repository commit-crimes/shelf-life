package com.android.shelflife.utils

import com.android.shelfLife.ui.utils.formatDateToTimestamp
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.ui.utils.fromCapitalStringToLowercaseString
import com.android.shelfLife.ui.utils.getTotalMinutes
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUnitTest {

  @Test
  fun testGetTotalMinutesWithValidTimestamp() {
    val timestamp = Timestamp(Date(3600000)) // 1 hour = 3600 seconds
    assertEquals(60, getTotalMinutes(timestamp)) // 3600 seconds = 60 minutes
  }

  @Test
  fun testFormatTimestampToDateWithValidTimestamp() {
    val calendar = Calendar.getInstance()
    calendar.set(2024, Calendar.OCTOBER, 17) // Set date to 17th October 2024
    val timestamp = Timestamp(calendar.time)

    val expectedDateString = "17/10/2024"
    assertEquals(expectedDateString, formatTimestampToDate(timestamp))
  }

  @Test
  fun testFormatDateToTimestampWithValidDateString() {
    val dateString = "17/10/2024"
    val timestamp = formatDateToTimestamp(dateString)

    // Validate that the timestamp represents the correct date
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val expectedDate = sdf.parse(dateString)
    assertEquals(expectedDate?.time, timestamp.toDate().time)
  }

  @Test
  fun testFromCapitalStringToLowercaseStringWithCapitalizedString() {
    val inputString = "hELLO wORLD"
    val expectedOutput = "Hello world"
    assertEquals(expectedOutput, fromCapitalStringToLowercaseString(inputString))
  }

  @Test
  fun testFromCapitalStringToLowercaseStringWithAlreadyCorrectFormat() {
    val inputString = "Hello World"
    val expectedOutput = "Hello world"
    assertEquals(expectedOutput, fromCapitalStringToLowercaseString(inputString))
  }

  @Test
  fun testFromCapitalStringToLowercaseStringWithEmptyString() {
    val inputString = ""
    val expectedOutput = ""
    assertEquals(expectedOutput, fromCapitalStringToLowercaseString(inputString))
  }

  @Test
  fun testFromCapitalStringToLowercaseStringWithSingleLetter() {
    val inputString = "A"
    val expectedOutput = "A"
    assertEquals(expectedOutput, fromCapitalStringToLowercaseString(inputString))
  }
}
