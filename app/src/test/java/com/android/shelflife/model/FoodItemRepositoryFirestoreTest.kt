package com.android.shelflife.model

import android.content.Context
import android.os.Looper
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.toMap
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class FoodItemRepositoryFirestoreTest {

  @get:Rule var hiltRule = HiltAndroidRule(this)

  @Inject lateinit var foodItemRepository: FoodItemRepositoryFirestore
  @Inject lateinit var mockFirestore: FirebaseFirestore

  private lateinit var mockCollection: CollectionReference
  private lateinit var mockDocument: DocumentReference

  @Before
  fun setUp() {
    hiltRule.inject()
    val mockContext = mock(Context::class.java)
    `when`(mockContext.getApplicationContext()).thenReturn(mock(Context::class.java))
    mockCollection = mock(CollectionReference::class.java)
    mockDocument = mock(DocumentReference::class.java)

    // Mock Firestore collection and document calls
    `when`(mockFirestore.collection(anyString())).thenReturn(mockCollection)
    `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
    `when`(mockDocument.collection(anyString())).thenReturn(mockCollection)
  }

  @Test
  fun addFoodItemHandlesFirestoreErrorGracefully() {
    runBlocking {
      // Arrange
      val validFoodFacts =
          FoodFacts(
              name = "Test Food",
              barcode = "1234567890123",
              quantity = Quantity(1.0),
              category = FoodCategory.FRUIT,
              nutritionFacts = NutritionFacts(),
              imageUrl = "http://example.com/image.png")
      val foodItem =
          FoodItem(
              uid = "item123",
              foodFacts = validFoodFacts,
              location = FoodStorageLocation.PANTRY,
              owner = "user1")

      `when`(mockCollection.document("household1")).thenReturn(mockDocument)
      `when`(mockDocument.collection("items")).thenReturn(mockCollection)
      `when`(mockCollection.document(foodItem.uid)).thenReturn(mockDocument)

      // Simulate Firestore failure
      val failureTask = Tasks.forException<Void>(RuntimeException("Firestore error"))
      `when`(mockDocument.set(any())).thenReturn(failureTask)

      // Act
      foodItemRepository.addFoodItem("household1", foodItem)

      // Simulate processing of pending tasks on the main looper
      shadowOf(Looper.getMainLooper()).idle()

      // Assert
      val cachedItems = foodItemRepository.foodItems.value
      assertFalse(cachedItems.contains(foodItem)) // Item should not remain in cache
      assertEquals("Failed to add item. Please try again.", foodItemRepository.errorMessage.value)
    }
  }

  @Test
  fun updateFoodItemUpdatesAnExistingItemInFirestore() {
    runBlocking {
      // Arrange
      val validFoodFacts =
          FoodFacts(
              name = "Test Food",
              barcode = "1234567890123",
              quantity = Quantity(1.0),
              category = FoodCategory.FRUIT,
              nutritionFacts = NutritionFacts(),
              imageUrl = "http://example.com/image.png")
      val foodItem =
          FoodItem(
              uid = "item123",
              foodFacts = validFoodFacts,
              location = FoodStorageLocation.FRIDGE,
              owner = "user1")

      // Simulate success
      `when`(mockCollection.document("household1")).thenReturn(mockDocument)
      `when`(mockDocument.collection("items")).thenReturn(mockCollection)
      `when`(mockCollection.document(foodItem.uid)).thenReturn(mockDocument)
      `when`(mockDocument.set(foodItem.toMap())).thenReturn(Tasks.forResult(null))

      // Act
      foodItemRepository.updateFoodItem("household1", foodItem)

      // Assert
      verify(mockCollection).document("item123")
      verify(mockDocument).set(foodItem.toMap())
      // Check local cache updated
      assertTrue(foodItemRepository.foodItems.value.contains(foodItem))
    }
  }

  @Test
  fun deleteFoodItemRemovesItemFromFirestore() {
    runBlocking {
      // Arrange
      val foodItemId = "item123"
      `when`(mockCollection.document("household1")).thenReturn(mockDocument)
      `when`(mockDocument.collection("items")).thenReturn(mockCollection)
      `when`(mockCollection.document(foodItemId)).thenReturn(mockDocument)
      // Simulate success
      `when`(mockDocument.delete()).thenReturn(Tasks.forResult(null))

      // Act
      foodItemRepository.deleteFoodItem("household1", foodItemId)

      // Assert
      verify(mockCollection).document(foodItemId)
      verify(mockDocument).delete()
      assertTrue(foodItemRepository.foodItems.value.isEmpty())
    }
  }

  @Test
  fun getFoodItemsHandlesFirestoreErrors() {
    runBlocking {
      // Arrange
      `when`(mockCollection.get())
          .thenReturn(Tasks.forException(RuntimeException("Firestore error")))
      `when`(mockCollection.document("household1")).thenReturn(mockDocument)
      `when`(mockDocument.collection("items")).thenReturn(mockCollection)

      // Act
      val foodItems = foodItemRepository.getFoodItems("household1")

      // Assert
      assertTrue(foodItems.isEmpty())
      // No need to check listeners here since `getFoodItems` uses `await()` which handles
      // exceptions directly.
    }
  }

  @Test
  fun selectFoodItemUpdatesSelectedFoodState() {
    runBlocking {
      // Arrange
      val foodItem =
          FoodItem(
              uid = "item123",
              foodFacts = mock(FoodFacts::class.java),
              location = FoodStorageLocation.FRIDGE,
              owner = "user1")

      // Act
      foodItemRepository.selectFoodItem(foodItem)

      // Assert
      assertEquals(foodItem, foodItemRepository.selectedFoodItem.first())
    }
  }

  @Test
  fun startListeningForFoodItemsSetsUpFirestoreListener() {
    // Arrange
    val mockListenerRegistration = mock(ListenerRegistration::class.java)
    `when`(mockCollection.addSnapshotListener(any<EventListener<QuerySnapshot>>()))
        .thenReturn(mockListenerRegistration)
    `when`(mockCollection.document("household1")).thenReturn(mockDocument)
    `when`(mockDocument.collection("items")).thenReturn(mockCollection)

    // Act
    foodItemRepository.startListeningForFoodItems("household1")

    // Assert
    verify(mockCollection).addSnapshotListener(any<EventListener<QuerySnapshot>>())
  }

  @Test
  fun stopListeningForFoodItemsRemovesFirestoreListener() {
    val mockListenerRegistration = mock(ListenerRegistration::class.java)
    val field =
        FoodItemRepositoryFirestore::class.java.getDeclaredField("foodItemsListenerRegistration")
    field.isAccessible = true
    field.set(foodItemRepository, mockListenerRegistration)

    // Act
    foodItemRepository.stopListeningForFoodItems()

    // Assert
    verify(mockListenerRegistration).remove()
  }

  @Test
  fun updateFoodItemHandlesNonExistentItem() {
    runBlocking {
      // Arrange
      val validFoodFacts =
          FoodFacts(
              name = "Test Food",
              barcode = "1234567890123",
              quantity = Quantity(1.0),
              category = FoodCategory.FRUIT,
              nutritionFacts = NutritionFacts(),
              imageUrl = "http://example.com/image.png")
      val updatedItem =
          FoodItem(
              uid = "item321",
              foodFacts = validFoodFacts,
              location = FoodStorageLocation.FREEZER,
              owner = "userX")

      `when`(mockCollection.document("household1")).thenReturn(mockDocument)
      `when`(mockDocument.collection("items")).thenReturn(mockCollection)
      `when`(mockCollection.document("item321")).thenReturn(mockDocument)
      // Simulate success
      `when`(mockDocument.set(updatedItem.toMap())).thenReturn(Tasks.forResult(null))

      // Act
      foodItemRepository.updateFoodItem("household1", updatedItem)

      // Assert
      val cachedItems = foodItemRepository.foodItems.value
      assertTrue("Updated item should now appear in cache", cachedItems.contains(updatedItem))
      verify(mockDocument).set(updatedItem.toMap())
    }
  }

  @Test
  fun deleteFoodItemHandlesNonExistentItem() {
    runBlocking {
      // Arrange
      val nonExistentItemId = "nonexistent_item"
      `when`(mockCollection.document("household1")).thenReturn(mockDocument)
      `when`(mockDocument.collection("items")).thenReturn(mockCollection)
      `when`(mockCollection.document(nonExistentItemId)).thenReturn(mockDocument)
      // Simulate success
      `when`(mockDocument.delete()).thenReturn(Tasks.forResult(null))

      // Act
      foodItemRepository.deleteFoodItem("household1", nonExistentItemId)

      // Assert
      val cachedItems = foodItemRepository.foodItems.value
      assertTrue("Cache should remain empty as item didn't exist", cachedItems.isEmpty())
      verify(mockDocument).delete()
    }
  }

  @Test
  fun getFoodItemsReturnsEmptyWhenNoData() {
    runBlocking {
      // Arrange
      val emptySnapshot = mock(QuerySnapshot::class.java)
      `when`(emptySnapshot.documents).thenReturn(emptyList())
      `when`(mockCollection.get()).thenReturn(Tasks.forResult(emptySnapshot))
      `when`(mockCollection.document("household1")).thenReturn(mockDocument)
      `when`(mockDocument.collection("items")).thenReturn(mockCollection)

      // Act
      val items = foodItemRepository.getFoodItems("household1")

      // Assert
      assertTrue("Should return empty list when no data", items.isEmpty())
      assertTrue("Cache should also be empty", foodItemRepository.foodItems.value.isEmpty())
    }
  }
}
