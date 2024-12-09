package com.android.shelfLife.model.foodItem

import com.android.shelfLife.model.foodFacts.FoodFacts
import com.google.firebase.firestore.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
class FoodItemRepositoryFirestoreTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var foodItemRepository: FoodItemRepositoryFirestore

    @Inject
    lateinit var mockFirestore: FirebaseFirestore

    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference

    @Before
    fun setUp() {
        hiltRule.inject()

        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)

        // Mock Firestore collection and document calls
        `when`(mockFirestore.collection("foodItems")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
        `when`(mockDocument.collection("items")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
    }

    @Test
    fun `addFoodItem adds item to Firestore`(): Unit = runBlocking {
        // Arrange
        val foodItem = FoodItem(
            uid = "item123",
            foodFacts = mock(FoodFacts::class.java),
            location = FoodStorageLocation.PANTRY,
            owner = "user1"
        )

        // Act
        foodItemRepository.addFoodItem("household1", foodItem)

        // Assert
        verify(mockCollection).document("household1")
        verify(mockCollection).document("item123")
        verify(mockDocument).set(foodItem)
    }

    @Test
    fun `updateFoodItem updates an existing item in Firestore`(): Unit = runBlocking {
        // Arrange
        val foodItem = FoodItem(
            uid = "item123",
            foodFacts = mock(FoodFacts::class.java),
            location = FoodStorageLocation.FRIDGE,
            owner = "user1"
        )

        // Act
        foodItemRepository.updateFoodItem("household1", foodItem)

        // Assert
        verify(mockCollection).document("item123")
        verify(mockDocument).set(foodItem)
    }

    @Test
    fun `deleteFoodItem removes item from Firestore`(): Unit = runBlocking {
        // Arrange
        val foodItemId = "item123"

        // Act
        foodItemRepository.deleteFoodItem("household1", foodItemId)

        // Assert
        verify(mockCollection).document(foodItemId)
        verify(mockDocument).delete()
    }

    @Test
    fun `getFoodItems fetches items from Firestore`(): Unit = runBlocking {
        // Arrange
        val mockSnapshot = mock(QuerySnapshot::class.java)
        `when`(mockCollection.get()).thenReturn(mock())
        `when`(mockCollection.get().await()).thenReturn(mockSnapshot)

        // Act
        val foodItems = foodItemRepository.getFoodItems("household1")

        // Assert
        assertNotNull(foodItems)
        verify(mockCollection).get()
    }

    @Test
    fun `getFoodItems handles Firestore errors`() = runBlocking {
        // Arrange
        `when`(mockCollection.get()).thenThrow(RuntimeException("Firestore error"))

        // Act
        val foodItems = foodItemRepository.getFoodItems("household1")

        // Assert
        assertTrue(foodItems.isEmpty())
    }

    @Test
    fun `selectFoodItem updates selectedFood state`() = runBlocking {
        // Arrange
        val foodItem = FoodItem(
            uid = "item123",
            foodFacts = mock(FoodFacts::class.java),
            location = FoodStorageLocation.FRIDGE,
            owner = "user1"
        )

        // Act
        foodItemRepository.selectFoodItem(foodItem)

        // Assert
        assertEquals(foodItem, foodItemRepository.selectedFoodItem.first())
    }

    @Test
    fun `startListeningForFoodItems sets up Firestore listener`() {
        // Arrange
        val mockListenerRegistration = mock(ListenerRegistration::class.java)
        `when`(
            mockCollection.addSnapshotListener(any<EventListener<QuerySnapshot>>())
        ).thenReturn(mockListenerRegistration)

        // Act
        foodItemRepository.startListeningForFoodItems("household1")

        // Assert
        verify(mockCollection).addSnapshotListener(any<EventListener<QuerySnapshot>>())
    }

    @Test
    fun `stopListeningForFoodItems removes Firestore listener`() {
        // Arrange
        val mockListenerRegistration = mock(ListenerRegistration::class.java)

        // Use reflection to access private `foodItemsListenerRegistration` for testing
        val field = FoodItemRepositoryFirestore::class.java.getDeclaredField("foodItemsListenerRegistration")
        field.isAccessible = true
        field.set(foodItemRepository, mockListenerRegistration)

        // Act
        foodItemRepository.stopListeningForFoodItems()

        // Assert
        verify(mockListenerRegistration).remove()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object TestFoodItemModule {

    @Provides
    @Singleton
    fun provideMockFirestore(): FirebaseFirestore {
        return mock(FirebaseFirestore::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodItemRepository(firestore: FirebaseFirestore): FoodItemRepository {
        return FoodItemRepositoryFirestore(firestore)
    }
}