package com.android.shelflife.model

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodStatus
import com.android.shelfLife.model.newFoodItem.FoodStorageLocation
import com.android.shelfLife.model.newFoodItem.toFoodItem
import com.android.shelfLife.model.newFoodItem.toMap
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
