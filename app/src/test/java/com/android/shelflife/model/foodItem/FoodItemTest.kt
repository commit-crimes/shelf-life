package com.android.shelfLife.model.foodItem

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class FoodItemTest {

  private val mockFoodFacts =
      FoodFacts(
          name = "Sample Food",
          nutritionFacts = NutritionFacts(energyKcal = 150, proteins = 5.0),
          quantity = Quantity(amount = 200.0))

  @Test
  fun `test FoodItem toMap conversion`() {
    // Arrange
    val foodItem =
        FoodItem(
            uid = "item123",
            foodFacts = mockFoodFacts,
            buyDate = Timestamp.now(),
            expiryDate = Timestamp.now(),
            openDate = Timestamp.now(),
            location = FoodStorageLocation.PANTRY,
            status = FoodStatus.UNOPENED,
            owner = "user123")

    // Act
    val result = foodItem.toMap()

    // Assert
    assertEquals("item123", result["uid"])
    assertEquals("user123", result["owner"])
    assertEquals(FoodStorageLocation.PANTRY.name, result["location"])
    assertEquals(FoodStatus.UNOPENED.name, result["status"])
    assertNotNull(result["foodFacts"])
  }

  @Test
  fun `test DocumentSnapshot toFoodItem conversion`() {
    // Arrange
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    val foodFactsMap =
        mapOf(
            "name" to "Sample Food",
            "nutritionFacts" to mapOf("energyKcal" to 150, "proteins" to 5.0),
            "quantity" to mapOf("amount" to 200.0))

    `when`(mockSnapshot.getString("uid")).thenReturn("item123")
    `when`(mockSnapshot.get("foodFacts")).thenReturn(foodFactsMap)
    `when`(mockSnapshot.getTimestamp("buyDate")).thenReturn(Timestamp.now())
    `when`(mockSnapshot.getTimestamp("expiryDate")).thenReturn(Timestamp.now())
    `when`(mockSnapshot.getTimestamp("openDate")).thenReturn(Timestamp.now())
    `when`(mockSnapshot.getString("location")).thenReturn(FoodStorageLocation.FRIDGE.name)
    `when`(mockSnapshot.getString("status")).thenReturn(FoodStatus.OPENED.name)
    `when`(mockSnapshot.getString("owner")).thenReturn("user123")

    // Act
    val foodItem = mockSnapshot.toFoodItem()

    // Assert
    assertNotNull(foodItem)
    assertEquals("item123", foodItem?.uid)
    assertEquals("user123", foodItem?.owner)
    assertEquals(FoodStorageLocation.FRIDGE, foodItem?.location)
    assertEquals(FoodStatus.OPENED, foodItem?.status)
    assertNotNull(foodItem?.foodFacts)
  }

  @Test
  fun `test DocumentSnapshot toFoodItem conversion with missing data`() {
    // Arrange
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.data).thenReturn(null)

    // Act
    val foodItem = mockSnapshot.toFoodItem()

    // Assert
    assertNull(foodItem)
  }

  @Test
  fun `test DocumentSnapshot toFoodItem conversion with invalid data`() {
    // Arrange
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.getString("uid")).thenReturn(null) // Missing UID

    // Act
    val foodItem = mockSnapshot.toFoodItem()

    // Assert
    assertNull(foodItem)
  }
}
