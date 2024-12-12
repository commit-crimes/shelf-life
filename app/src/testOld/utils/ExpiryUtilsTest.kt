// ExpiryUtilsTest.kt

package com.android.shelfLife.ui.newutils

import com.android.shelfLife.ui.theme.expired
import com.android.shelfLife.ui.theme.expiresInALongTime
import com.android.shelfLife.ui.theme.expiresLater
import com.android.shelfLife.ui.theme.expiresSoon
import com.android.shelfLife.ui.utils.getExpiryInfo
import com.android.shelfLife.ui.utils.getExpiryMessageBasedOnDays
import com.android.shelfLife.ui.utils.getProgressBarState
import java.text.SimpleDateFormat
import java.util.*
import org.junit.Assert.*
import org.junit.Test

class ExpiryUtilsTest {

  @Test
  fun testGetExpiryMessageBasedOnDays() {
    // Test for "Expired"
    val messageExpired = getExpiryMessageBasedOnDays(-1, "18/11/2024")
    assertEquals("Expired", messageExpired)

    // Test for "Expires today"
    val messageToday = getExpiryMessageBasedOnDays(0, "18/11/2024")
    assertEquals("Expires today", messageToday)

    // Test for "Expires tomorrow"
    val messageTomorrow = getExpiryMessageBasedOnDays(1, "19/11/2024")
    assertEquals("Expires tomorrow", messageTomorrow)

    // Test for "Expires in X days"
    val messageIn3Days = getExpiryMessageBasedOnDays(3, "21/11/2024")
    assertEquals("Expires in 3 days", messageIn3Days)

    // Test for "Expires in a week"
    val messageInWeek = getExpiryMessageBasedOnDays(10, "28/11/2024")
    assertEquals("Expires in a week", messageInWeek)

    // Test for "Expires on dd/MM/yyyy"
    val messageOnDate = getExpiryMessageBasedOnDays(20, "08/12/2024")
    assertEquals("Expires on 08/12/2024", messageOnDate)
  }

  @Test
  fun testGetProgressBarState() {
    // Test for "Expired"
    val (progressExpired, colorExpired) = getProgressBarState(-1)
    assertEquals(0.25f, progressExpired)
    assertEquals(expired, colorExpired)

    // Test for "Expires soon" (<=5 days)
    val (progressSoon, colorSoon) = getProgressBarState(3)
    assertEquals(0.5f, progressSoon)
    assertEquals(expiresSoon, colorSoon)

    // Test for "Expires later" (<=14 days)
    val (progressLater, colorLater) = getProgressBarState(10)
    assertEquals(0.75f, progressLater)
    assertEquals(expiresLater, colorLater)

    // Test for "Expires in a long time" (>14 days)
    val (progressLongTime, colorLongTime) = getProgressBarState(20)
    assertEquals(1.0f, progressLongTime)
    assertEquals(expiresInALongTime, colorLongTime)
  }

  @Test
  fun testGetExpiryInfo() {
    // Define a fixed current date
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDate = formatter.parse("18/11/2024")!!

    // Test with expiry date tomorrow
    val expiryDateStringTomorrow = "19/11/2024"
    val (messageTomorrow, progressBarStateTomorrow) =
        getExpiryInfo(expiryDateStringTomorrow, currentDate)
    assertEquals("Expires tomorrow", messageTomorrow)
    assertEquals(0.5f, progressBarStateTomorrow.first)
    assertEquals(expiresSoon, progressBarStateTomorrow.second)

    // Test with expiry date in 10 days
    val expiryDateStringIn10Days = "28/11/2024"
    val (messageIn10Days, progressBarStateIn10Days) =
        getExpiryInfo(expiryDateStringIn10Days, currentDate)
    assertEquals("Expires in a week", messageIn10Days)
    assertEquals(0.75f, progressBarStateIn10Days.first)
    assertEquals(expiresLater, progressBarStateIn10Days.second)

    // Test with expiry date null
    val (messageNoDate, progressBarStateNoDate) = getExpiryInfo(null, currentDate)
    assertEquals("No Expiry Date", messageNoDate)
    assertEquals(0f, progressBarStateNoDate.first)
    assertEquals(expired, progressBarStateNoDate.second)

    // Test with invalid date format
    val (messageInvalid, progressBarStateInvalid) = getExpiryInfo("invalid date", currentDate)
    assertEquals("Invalid Date Format", messageInvalid)
    assertEquals(0f, progressBarStateInvalid.first)
    assertEquals(expired, progressBarStateInvalid.second)
  }
}
