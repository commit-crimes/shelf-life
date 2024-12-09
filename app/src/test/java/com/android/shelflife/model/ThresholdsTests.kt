// ExpiryUtilsTest.kt

package com.android.shelfLife.ui.newutils

import com.android.shelfLife.ui.theme.expired
import com.android.shelfLife.ui.theme.expiresInALongTime
import com.android.shelfLife.ui.theme.expiresLater
import com.android.shelfLife.ui.theme.expiresSoon
import java.text.SimpleDateFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class ThresholdsTests {

  @Test
  fun `getProgressBarState returns correct state and color when expired`() {
    val (fill, color) = getProgressBarState(-1)
    assertEquals(0.25f, fill, 0.0f)
    assertEquals(expired, color)
  }

  @Test
  fun `getProgressBarState returns correct state and color when daysDifference is zero`() {
    val (fill, color) = getProgressBarState(0)
    assertEquals(0.5f, fill, 0.0f)
    assertEquals(expiresSoon, color)
  }

  @Test
  fun `getProgressBarState returns correct state and color when within expiresSoon threshold`() {
    val (fill, color) = getProgressBarState(3)
    assertEquals(0.5f, fill, 0.0f)
    assertEquals(expiresSoon, color)
  }

  @Test
  fun `getProgressBarState returns correct state and color when within expiresLater threshold`() {
    val (fill, color) = getProgressBarState(10)
    assertEquals(0.75f, fill, 0.0f)
    assertEquals(expiresLater, color)
  }

  @Test
  fun `getProgressBarState returns correct state and color when expiresInALongTime`() {
    val (fill, color) = getProgressBarState(20)
    assertEquals(1.0f, fill, 0.0f)
    assertEquals(expiresInALongTime, color)
  }

  @Test
  fun `getExpiryMessageBasedOnDays returns correct message when expired`() {
    val message = getExpiryMessageBasedOnDays(-1, "18/11/2024")
    assertEquals("Expired", message)
  }

  @Test
  fun `getExpiryMessageBasedOnDays returns correct message when expires today`() {
    val message = getExpiryMessageBasedOnDays(0, "18/11/2024")
    assertEquals("Expires today", message)
  }

  @Test
  fun `getExpiryMessageBasedOnDays returns correct message when expires tomorrow`() {
    val message = getExpiryMessageBasedOnDays(1, "19/11/2024")
    assertEquals("Expires tomorrow", message)
  }

  @Test
  fun `getExpiryMessageBasedOnDays returns correct message when expires in few days`() {
    val message = getExpiryMessageBasedOnDays(3, "21/11/2024")
    assertEquals("Expires in 3 days", message)
  }

  @Test
  fun `getExpiryMessageBasedOnDays returns correct message when expires in a week`() {
    val message = getExpiryMessageBasedOnDays(10, "28/11/2024")
    assertEquals("Expires in a week", message)
  }

  @Test
  fun `getExpiryMessageBasedOnDays returns correct message when expires on specific date`() {
    val message = getExpiryMessageBasedOnDays(20, "08/12/2024")
    assertEquals("Expires on 08/12/2024", message)
  }

  @Test
  fun `getExpiryInfo returns correct message and progress bar state for valid date`() {
    val currentDate = SimpleDateFormat("dd/MM/yyyy").parse("18/11/2024")!!
    val expiryDateString = "19/11/2024"
    val (message, progressBarState) = getExpiryInfo(expiryDateString, currentDate)
    assertEquals("Expires tomorrow", message)
    assertEquals(0.5f, progressBarState.first, 0.0f)
    assertEquals(expiresSoon, progressBarState.second)
  }

  @Test
  fun `getExpiryInfo returns correct message and progress bar state when date is null`() {
    val (message, progressBarState) = getExpiryInfo(null)
    assertEquals("No Expiry Date", message)
    assertEquals(0f, progressBarState.first, 0.0f)
    assertEquals(expired, progressBarState.second)
  }

  @Test
  fun `getExpiryInfo returns correct message and progress bar state for invalid date format`() {
    val (message, progressBarState) = getExpiryInfo("invalid date")
    assertEquals("Invalid Date Format", message)
    assertEquals(0f, progressBarState.first, 0.0f)
    assertEquals(expired, progressBarState.second)
  }
}
