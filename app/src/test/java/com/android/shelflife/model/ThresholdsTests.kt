package com.android.shelflife.model

import androidx.compose.ui.graphics.Color
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.ui.utils.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShelfLifeUtilsTest {

  @Test
  fun `daysToSeconds converts days to seconds correctly`() {
    assertEquals(86400L, daysToSeconds(1)) // 1 day = 86400 seconds
    assertEquals(172800L, daysToSeconds(2)) // 2 days = 172800 seconds
    assertEquals(2592000L, daysToSeconds(30)) // 30 days = 2592000 seconds
  }

  @Test
  fun `getThresholdsForCategory returns correct thresholds for MEAT`() {
    val thresholds = getThresholdsForCategory(FoodCategory.MEAT)
    assertEquals(daysToSeconds(3), thresholds.redThreshold)
    assertEquals(daysToSeconds(7), thresholds.orangeThreshold)
  }

  @Test
  fun `getThresholdsForCategory returns correct thresholds for FRUIT`() {
    val thresholds = getThresholdsForCategory(FoodCategory.FRUIT)
    assertEquals(daysToSeconds(2), thresholds.redThreshold)
    assertEquals(daysToSeconds(5), thresholds.orangeThreshold)
  }

  @Test
  fun `getThresholdsForCategory returns correct thresholds for VEGETABLE`() {
    val thresholds = getThresholdsForCategory(FoodCategory.VEGETABLE)
    assertEquals(daysToSeconds(3), thresholds.redThreshold)
    assertEquals(daysToSeconds(7), thresholds.orangeThreshold)
  }

  @Test
  fun `getThresholdsForCategory returns correct thresholds for DAIRY`() {
    val thresholds = getThresholdsForCategory(FoodCategory.DAIRY)
    assertEquals(daysToSeconds(5), thresholds.redThreshold)
    assertEquals(daysToSeconds(10), thresholds.orangeThreshold)
  }

  @Test
  fun `getThresholdsForCategory returns correct thresholds for GRAIN`() {
    val thresholds = getThresholdsForCategory(FoodCategory.GRAIN)
    assertEquals(daysToSeconds(30), thresholds.redThreshold)
    assertEquals(daysToSeconds(90), thresholds.orangeThreshold)
  }

  @Test
  fun `getThresholdsForCategory returns correct thresholds for BEVERAGE`() {
    val thresholds = getThresholdsForCategory(FoodCategory.BEVERAGE)
    assertEquals(daysToSeconds(30), thresholds.redThreshold)
    assertEquals(daysToSeconds(90), thresholds.orangeThreshold)
  }

  @Test
  fun `getThresholdsForCategory returns correct thresholds for SNACK`() {
    val thresholds = getThresholdsForCategory(FoodCategory.SNACK)
    assertEquals(daysToSeconds(15), thresholds.redThreshold)
    assertEquals(daysToSeconds(30), thresholds.orangeThreshold)
  }

  @Test
  fun `getThresholdsForCategory returns correct thresholds for OTHER`() {
    val thresholds = getThresholdsForCategory(FoodCategory.OTHER)
    assertEquals(daysToSeconds(7), thresholds.redThreshold)
    assertEquals(daysToSeconds(14), thresholds.orangeThreshold)
  }

  @Test
  fun `getProgressBarState returns correct state and color when expired`() {
    val thresholds = Thresholds(daysToSeconds(3), daysToSeconds(7))
    val (fill, color) = getProgressBarState(0, thresholds)
    assertEquals(0.25f, fill, 0.0f)
    assertEquals(Color(0xFF8F0303), color)
  }

  @Test
  fun `getProgressBarState returns correct state and color when within red threshold`() {
    val thresholds = Thresholds(daysToSeconds(3), daysToSeconds(7))
    val (fill, color) = getProgressBarState(daysToSeconds(2), thresholds)
    assertEquals(0.5f, fill, 0.0f)
    assertTrue(colorsApproximatelyEqual(color, Color(0xFFF67800)))
  }

  @Test
  fun `getProgressBarState returns correct state and color when within orange threshold`() {
    val thresholds = Thresholds(daysToSeconds(3), daysToSeconds(7))
    val (fill, color) = getProgressBarState(daysToSeconds(5), thresholds)
    assertEquals(0.75f, fill, 0.0f)
    assertEquals(Color(0xFF71B504), color)
  }

  @Test
  fun `getProgressBarState returns correct state and color when within green threshold`() {
    val thresholds = Thresholds(daysToSeconds(3), daysToSeconds(7))
    val (fill, color) = getProgressBarState(daysToSeconds(10), thresholds)
    assertEquals(1.0f, fill, 0.0f)
    assertEquals(Color(0xFF4CAF50), color)
  }

  private fun colorsApproximatelyEqual(
      color1: Color,
      color2: Color,
      tolerance: Float = 0.01f
  ): Boolean {
    return (Math.abs(color1.red - color2.red) < tolerance) &&
        (Math.abs(color1.green - color2.green) < tolerance) &&
        (Math.abs(color1.blue - color2.blue) < tolerance) &&
        (Math.abs(color1.alpha - color2.alpha) < tolerance)
  }
}
