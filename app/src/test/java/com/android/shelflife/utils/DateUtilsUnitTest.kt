// UtilsTest.kt

package com.android.shelfLife.ui.utils

import androidx.compose.ui.text.AnnotatedString
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import org.junit.Assert.*
import org.junit.Test

class DateUtilsUnitTest {

  @Test
  fun testGetTotalMinutes() {
    // Given a timestamp representing a specific time
    val date = Date()
    val timestamp = Timestamp(date)

    val totalMinutes = getTotalMinutes(timestamp)

    val expectedMinutes = (timestamp.seconds / 60).toInt()
    assertEquals(expectedMinutes, totalMinutes)
  }

  @Test
  fun testInsertSlashes() {
    // Test with 8 digits
    val input = "01012020"
    val expected = "01/01/2020"
    val result = insertSlashes(input)
    assertEquals(expected, result)

    // Test with less than 8 digits
    val inputShort = "010120"
    val expectedShort = "01/01/20"
    val resultShort = insertSlashes(inputShort)
    assertEquals(expectedShort, resultShort)

    // Test with more than 8 digits
    val inputLong = "01012020123"
    val expectedLong = "01/01/2020"
    val resultLong = insertSlashes(inputLong)
    assertEquals(expectedLong, resultLong)
  }

  @Test
  fun testIsValidDate() {
    // Valid date
    assertTrue(isValidDate("01/01/2020"))
    // Invalid date format
    assertFalse(isValidDate("1/1/2020"))
    // Invalid date value
    assertFalse(isValidDate("31/02/2020")) // February 31st doesn't exist
    // Leap year
    assertTrue(isValidDate("29/02/2020")) // 2020 is a leap year
    // Non-leap year
    assertFalse(isValidDate("29/02/2019")) // 2019 is not a leap year
    // Invalid month
    assertFalse(isValidDate("01/13/2020"))
    // Invalid day
    assertFalse(isValidDate("32/01/2020"))
  }

  @Test
  fun testIsValidDateNotPast() {
    val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
    val todayDate = Date()
    val today = sdf.format(todayDate)
    // Date in the future
    val futureDate = "01012100"
    assertTrue(isValidDateNotPast(futureDate))
    // Today's date
    assertTrue(isValidDateNotPast(today))
    // Date in the past
    val pastDate = "01012000"
    assertFalse(isValidDateNotPast(pastDate))
  }

  @Test
  fun testIsDateAfterOrEqual() {
    // date1 > date2
    assertTrue(isDateAfterOrEqual("02012020", "01012020"))
    // date1 == date2
    assertTrue(isDateAfterOrEqual("01012020", "01012020"))
    // date1 < date2
    assertFalse(isDateAfterOrEqual("01012020", "02012020"))
    // Invalid dates
    assertFalse(isDateAfterOrEqual("31022020", "01012020"))
    // Same dates
    assertTrue(isDateAfterOrEqual("15082021", "15082021"))
  }

  @Test
  fun testFormatDateToTimestamp() {
    val timestamp = formatDateToTimestamp("01012020")
    val expectedDate = SimpleDateFormat("dd/MM/yyyy").parse("01/01/2020")
    assertNotNull(timestamp)
    assertEquals(expectedDate, timestamp?.toDate())
    // Invalid date
    val invalidTimestamp = formatDateToTimestamp("31022020") // Invalid date
    assertNull(invalidTimestamp)
    // Empty date
    val emptyTimestamp = formatDateToTimestamp("")
    assertNull(emptyTimestamp)
  }

  @Test
  fun testFormatTimestampToDate() {
    val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
    val date = sdf.parse("01012020")
    val timestamp = Timestamp(date!!)
    val dateStr = formatTimestampToDate(timestamp)
    assertEquals("01012020", dateStr)
  }

  @Test
  fun testFormatTimestampToDisplayDate() {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = sdf.parse("01/01/2020")
    val timestamp = Timestamp(date!!)
    val dateStr = formatTimestampToDisplayDate(timestamp)
    assertEquals("01/01/2020", dateStr)
  }

  @Test
  fun testFromCapitalStringToLowercaseString() {
    val input = "HELLO"
    val expected = "Hello"
    val result = fromCapitalStringToLowercaseString(input)
    assertEquals(expected, result)

    val inputMixed = "hELLO"
    val expectedMixed = "Hello"
    val resultMixed = fromCapitalStringToLowercaseString(inputMixed)
    assertEquals(expectedMixed, resultMixed)
  }

  @Test
  fun testGetDateErrorMessage() {
    // Empty date required
    val error1 = getDateErrorMessage("", true)
    assertEquals("Date cannot be empty", error1)
    // Empty date not required
    val error2 = getDateErrorMessage("", false)
    assertNull(error2)
    // Incomplete date
    val error3 = getDateErrorMessage("010120", true)
    assertEquals("Incomplete date", error3)
    // Invalid date
    val error4 = getDateErrorMessage("31022020", true)
    assertEquals("Invalid date", error4)
    // Valid date
    val error5 = getDateErrorMessage("01012020", true)
    assertNull(error5)
  }

  @Test
  fun testIsLeapYear() {
    assertTrue(isLeapYear(2020))
    assertFalse(isLeapYear(2019))
    assertTrue(isLeapYear(2000))
    assertFalse(isLeapYear(1900))
  }

  @Test
  fun testDateVisualTransformation() {
    val transformation = DateVisualTransformation()

    // Test full date
    val input = AnnotatedString("01012020")
    val transformedText = transformation.filter(input)
    assertEquals("01/01/2020", transformedText.text.text)

    // Test incomplete date
    val inputIncomplete = AnnotatedString("0101")
    val transformedTextIncomplete = transformation.filter(inputIncomplete)
    assertEquals("01/01", transformedTextIncomplete.text.text)

    // Test non-digit characters
    val inputNonDigit = AnnotatedString("01a1b2020c")
    val transformedTextNonDigit = transformation.filter(inputNonDigit)
    assertEquals("01/12/020", transformedTextNonDigit.text.text) // Non-digits are removed

    // Test cursor position mapping
    val offsetMapping = transformedText.offsetMapping
    val originalOffset = 4
    val transformedOffset = offsetMapping.originalToTransformed(originalOffset)
    val expectedTransformedOffset = 5 // Due to the slash at position 2
    assertEquals(expectedTransformedOffset, transformedOffset)
  }

  @Test
  fun testGetTotalMinutesEdgeCases() {
    // Test with zero timestamp
    val timestampZero = Timestamp(0, 0)
    val totalMinutesZero = getTotalMinutes(timestampZero)
    assertEquals(0, totalMinutesZero)

    // Test with negative timestamp (unlikely but for completeness)
    val timestampNegative = Timestamp(-60, 0)
    val totalMinutesNegative = getTotalMinutes(timestampNegative)
    assertEquals(-1, totalMinutesNegative)
  }
}
