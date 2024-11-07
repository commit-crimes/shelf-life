package com.android.shelflife.model.foodItem

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodItemTest {

  // Mock FoodFacts to use in tests
  private val mockFoodFacts =
      FoodFacts(
          name = "Sample Food",
          nutritionFacts = NutritionFacts(energyKcal = 150, proteins = 5.0),
          quantity = Quantity(amount = 200.0),
      )

  @Test
  fun `test getImportantDetails`() {
    val foodItem =
        FoodItem(uid = "1", foodFacts = mockFoodFacts, expiryDate = getFutureTimestamp(days = 10))

    val importantDetails = foodItem.getImportantDetails()
    println("Hey $importantDetails")
    assertTrue(importantDetails.contains("Sample Food"))
    assertTrue(importantDetails.contains("150Kcal"))
    assertTrue(importantDetails.contains("5.0g protein"))
    assertTrue(importantDetails.contains("Expires in 10 days"))
    assertTrue(importantDetails.contains("200.0 gram"))
  }

  @Test
  fun `test openDate for a FoodItem`() {
    val today = Timestamp.now()

    val foodItem =
      FoodItem(uid = "1", foodFacts = mockFoodFacts, expiryDate = getFutureTimestamp(days = 3), openDate = today)

    assertEquals(today, foodItem.openDate)
  }

  @Test
  fun `test getRemainingDays for future expiry date`() {
    val foodItem =
        FoodItem(uid = "1", foodFacts = mockFoodFacts, expiryDate = getFutureTimestamp(days = 5))

    assertEquals(5, foodItem.getRemainingDays())
  }

  @Test
  fun `test getRemainingDays for past expiry date`() {
    val foodItem =
      FoodItem(uid = "1", foodFacts = mockFoodFacts, expiryDate = getPastTimestamp(days = 3))

    assertEquals(-3, foodItem.getRemainingDays())
  }

  @Test
  fun `test isExpired for expired item`() {
    val foodItem =
        FoodItem(uid = "1", foodFacts = mockFoodFacts, expiryDate = getPastTimestamp(days = 1))

    assertTrue(foodItem.isExpired())
  }

  @Test
  fun `test isExpired for non-expired item`() {
    val foodItem =
        FoodItem(uid = "1", foodFacts = mockFoodFacts, expiryDate = getFutureTimestamp(days = 1))

    assertTrue(!foodItem.isExpired())
  }

  // Helper functions to create future and past timestamps
  private fun getFutureTimestamp(days: Int): Timestamp {
    val calendar =
        Calendar.getInstance().apply {
          time = Date()
          add(Calendar.DAY_OF_YEAR, days)
        }
    return Timestamp(calendar.time)
  }

  private fun getPastTimestamp(days: Int): Timestamp {
    val calendar =
        Calendar.getInstance().apply {
          time = Date()
          add(Calendar.DAY_OF_YEAR, -days)
        }
    return Timestamp(calendar.time)
  }
}
