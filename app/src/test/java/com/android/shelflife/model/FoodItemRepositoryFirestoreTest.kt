package com.android.shelflife.model

import android.content.Context
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.FoodStorageLocation
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

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
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
  fun addFoodItemHandlesFirestoreErrorGracefully(): Unit = runBlocking {
    // Arrange
    val foodItem =
        FoodItem(
            uid = "item123",
            foodFacts = mock(FoodFacts::class.java),
            location = FoodStorageLocation.PANTRY,
            owner = "user1")
    `when`(mockCollection.document("household1")).thenReturn(mockDocument)
    `when`(mockDocument.collection("items")).thenReturn(mockCollection)
    `when`(mockCollection.document(foodItem.uid)).thenReturn(mockDocument)
    `when`(mockDocument.set(any())).thenThrow(RuntimeException("Firestore error"))

    // Act
    foodItemRepository.addFoodItem("household1", foodItem)

    // Assert
    val cachedItems = foodItemRepository.foodItems.value
    assertFalse(cachedItems.contains(foodItem)) // Item should not remain in cache
    assertEquals("Failed to add item. Please try again.", foodItemRepository.errorMessage.value)
  }

  @Test
  fun updateFoodItemUpdatesAnExistingItemInFirestore(): Unit = runBlocking {
    // Arrange
    val foodItem =
        FoodItem(
            uid = "item123",
            foodFacts = mock(FoodFacts::class.java),
            location = FoodStorageLocation.FRIDGE,
            owner = "user1")

    // Act
    foodItemRepository.updateFoodItem("household1", foodItem)

    // Assert
    verify(mockCollection).document("item123")
    verify(mockDocument).set(foodItem)
  }

  @Test
  fun deleteFoodItemRemovesItemFromFirestore(): Unit = runBlocking {
    // Arrange
    val foodItemId = "item123"

    // Act
    foodItemRepository.deleteFoodItem("household1", foodItemId)

    // Assert
    verify(mockCollection).document(foodItemId)
    verify(mockDocument).delete()
  }

  @Test
  fun getFoodItemsHandlesFirestoreErrors() = runBlocking {
    // Arrange
    `when`(mockCollection.get()).thenThrow(RuntimeException("Firestore error"))

    // Act
    val foodItems = foodItemRepository.getFoodItems("household1")

    // Assert
    assertTrue(foodItems.isEmpty())
  }

  @Test
  fun selectFoodItemUpdatesSelectedFoodState() = runBlocking {
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

  @Test
  fun startListeningForFoodItemsSetsUpFirestoreListener() {
    // Arrange
    val mockListenerRegistration = mock(ListenerRegistration::class.java)
    `when`(mockCollection.addSnapshotListener(any<EventListener<QuerySnapshot>>()))
        .thenReturn(mockListenerRegistration)

    // Act
    foodItemRepository.startListeningForFoodItems("household1")

    // Assert
    verify(mockCollection).addSnapshotListener(any<EventListener<QuerySnapshot>>())
  }

  @Test
  fun stopListeningForFoodItemsRemovesFirestoreListener() {
    // Arrange
    val mockListenerRegistration = mock(ListenerRegistration::class.java)

    // Use reflection to access private `foodItemsListenerRegistration` for testing
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
  fun updateFoodItemHandlesNonExistentItem(): Unit = runBlocking {
    // Arrange
    val originalItem =
        FoodItem(
            uid = "item321",
            foodFacts = mock(FoodFacts::class.java),
            location = FoodStorageLocation.FRIDGE,
            owner = "userX")
    // No items in local cache initially
    // Update an item that doesn't exist in cache
    val updatedItem = originalItem.copy(location = FoodStorageLocation.FREEZER)

    `when`(mockCollection.document("item321")).thenReturn(mockDocument)
    `when`(mockDocument.set(updatedItem)).thenReturn(Tasks.forResult(null))

    // Act
    foodItemRepository.updateFoodItem("household1", updatedItem)

    // Assert
    val cachedItems = foodItemRepository.foodItems.value
    assertTrue("Updated item should now appear in cache", cachedItems.contains(updatedItem))
    verify(mockDocument).set(updatedItem)
  }

  @Test
  fun deleteFoodItemHandlesNonExistentItem(): Unit = runBlocking {
    // Arrange
    // Attempt to delete an item not in cache or Firestore
    val nonExistentItemId = "nonexistent_item"
    `when`(mockCollection.document(nonExistentItemId)).thenReturn(mockDocument)
    `when`(mockDocument.delete()).thenReturn(Tasks.forResult(null))

    // Act
    foodItemRepository.deleteFoodItem("household1", nonExistentItemId)

    // Assert
    val cachedItems = foodItemRepository.foodItems.value
    assertTrue("Cache should remain empty as item didn't exist", cachedItems.isEmpty())
    verify(mockDocument).delete()
  }

  @Test
  fun getFoodItemsReturnsEmptyWhenNoData(): Unit = runBlocking {
    // Arrange
    val emptySnapshot = mock(QuerySnapshot::class.java)
    `when`(emptySnapshot.documents).thenReturn(emptyList())
    `when`(mockCollection.get()).thenReturn(Tasks.forResult(emptySnapshot))

    // Act
    val items = foodItemRepository.getFoodItems("household1")

    // Assert
    assertTrue("Should return empty list when no data", items.isEmpty())
    assertTrue("Cache should also be empty", foodItemRepository.foodItems.value.isEmpty())
  }
}
