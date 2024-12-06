package com.android.shelfLife.model.newFoodItem

import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import java.lang.reflect.Field
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class FoodItemRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollection: CollectionReference
  @Mock private lateinit var mockDocument: DocumentReference
  @Mock private lateinit var mockSubCollection: CollectionReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  private lateinit var foodItemRepository: FoodItemRepositoryFirestore

  private val householdId = "testHouseholdId"

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    // Mock Firestore collection and document references
    `when`(mockFirestore.collection("foodItems")).thenReturn(mockCollection)
    `when`(mockCollection.document(householdId)).thenReturn(mockDocument)
    `when`(mockDocument.collection("items")).thenReturn(mockSubCollection)
    `when`(mockSubCollection.document(anyString())).thenReturn(mockDocument)

    // Initialize the FoodItemRepositoryFirestore with mocks
    foodItemRepository = FoodItemRepositoryFirestore(mockFirestore)

    // Set the Dispatchers to use the TestCoroutineDispatcher
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `addFoodItem adds item to Firestore and updates local cache`() = runTest {
    val newFoodItem =
        FoodItem(
            uid = "item1",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "123456789",
                    quantity = Quantity(amount = 100.0, unit = FoodUnit.GRAM)),
            buyDate = null,
            expiryDate = null,
            openDate = null,
            location = FoodStorageLocation.PANTRY,
            status = FoodStatus.UNOPENED,
            owner = "user1")

    val mockTask: Task<Void> = Tasks.forResult(null)
    `when`(mockDocument.set(any<Map<String, Any>>())).thenReturn(mockTask)

    // Access the private _foodItems variable via reflection
    val foodItemsField: Field = foodItemRepository.javaClass.getDeclaredField("_foodItems")
    foodItemsField.isAccessible = true
    val _foodItems = foodItemsField.get(foodItemRepository) as MutableStateFlow<List<FoodItem>>
    _foodItems.value = emptyList()

    foodItemRepository.addFoodItem(householdId, newFoodItem)

    val foodItems = foodItemRepository.foodItems.value

    assertEquals(1, foodItems.size)
    assertEquals(newFoodItem, foodItems[0])
  }

  @Test
  fun `getFoodItems fetches items from Firestore and updates local cache`() = runTest {
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    // Create separate mocks for each document
    val mockDocumentSnapshot1 = mock(DocumentSnapshot::class.java)
    val mockDocumentSnapshot2 = mock(DocumentSnapshot::class.java)
    val mockDocuments = listOf(mockDocumentSnapshot1, mockDocumentSnapshot2)

    `when`(mockSubCollection.get()).thenReturn(mockTask)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocuments)

    // Mock the conversion from DocumentSnapshot to FoodItem
    val foodItem1 =
        FoodItem(
            uid = "item1",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "123456789",
                    quantity = Quantity(amount = 100.0, unit = FoodUnit.GRAM)),
            buyDate = null,
            expiryDate = null,
            openDate = null,
            location = FoodStorageLocation.PANTRY,
            status = FoodStatus.UNOPENED,
            owner = "user1")
    val foodItem2 =
        foodItem1.copy(
            uid = "item2",
            foodFacts =
                FoodFacts(
                    name = "Banana",
                    barcode = "987654321",
                    quantity = Quantity(amount = 150.0, unit = FoodUnit.GRAM)))

    `when`(mockDocumentSnapshot1.toObject(FoodItem::class.java)).thenReturn(foodItem1)
    `when`(mockDocumentSnapshot2.toObject(FoodItem::class.java)).thenReturn(foodItem2)

    // Access the private _foodItems variable via reflection
    val foodItemsField: Field = foodItemRepository.javaClass.getDeclaredField("_foodItems")
    foodItemsField.isAccessible = true
    val _foodItems = foodItemsField.get(foodItemRepository) as MutableStateFlow<List<FoodItem>>
    _foodItems.value = emptyList()

    val fetchedItems = foodItemRepository.getFoodItems(householdId)

    val foodItems = foodItemRepository.foodItems.value

    // Assertions
    assertEquals(2, fetchedItems.size)
    assertEquals(fetchedItems, foodItems)
    assertEquals("item1", fetchedItems[0].uid)
    assertEquals("item2", fetchedItems[1].uid)
  }

  @Test
  fun `startListeningForFoodItems updates local cache on snapshot changes`() {
    val listenerCaptor =
        ArgumentCaptor.forClass(EventListener::class.java as Class<EventListener<QuerySnapshot>>)
    val registrationMock = mock(ListenerRegistration::class.java)

    `when`(mockSubCollection.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(registrationMock)

    // Start listening
    foodItemRepository.startListeningForFoodItems(householdId)

    // Simulate a snapshot event
    val mockSnapshot = mock(QuerySnapshot::class.java)
    val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val mockDocuments = listOf(mockDocumentSnapshot)
    val foodItem =
        FoodItem(
            uid = "item1",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "123456789",
                    quantity = Quantity(amount = 100.0, unit = FoodUnit.GRAM)),
            buyDate = null,
            expiryDate = null,
            openDate = null,
            location = FoodStorageLocation.PANTRY,
            status = FoodStatus.UNOPENED,
            owner = "user1")

    `when`(mockSnapshot.documents).thenReturn(mockDocuments)
    `when`(mockDocumentSnapshot.toObject(FoodItem::class.java)).thenReturn(foodItem)

    val listener = listenerCaptor.value
    listener.onEvent(mockSnapshot, null)

    val foodItems = foodItemRepository.foodItems.value

    assertEquals(1, foodItems.size)
    assertEquals(foodItem, foodItems[0])
  }

  @Test
  fun `updateFoodItem updates item in Firestore and local cache`() = runTest {
    val existingFoodItem =
        FoodItem(
            uid = "item1",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "123456789",
                    quantity = Quantity(amount = 100.0, unit = FoodUnit.GRAM)),
            buyDate = null,
            expiryDate = null,
            openDate = null,
            location = FoodStorageLocation.PANTRY,
            status = FoodStatus.UNOPENED,
            owner = "user1")

    val updatedFoodItem =
        existingFoodItem.copy(
            status = FoodStatus.OPENED,
            foodFacts =
                existingFoodItem.foodFacts.copy(
                    quantity = Quantity(amount = 80.0, unit = FoodUnit.GRAM)))

    // Mock Firestore set operation for any argument
    val mockTask: Task<Void> = Tasks.forResult(null)
    `when`(mockDocument.set(any())).thenReturn(mockTask)

    // Set initial food items in cache
    val foodItemsField: Field = foodItemRepository.javaClass.getDeclaredField("_foodItems")
    foodItemsField.isAccessible = true
    val _foodItems = foodItemsField.get(foodItemRepository) as MutableStateFlow<List<FoodItem>>
    _foodItems.value = listOf(existingFoodItem)

    // Call the method under test
    foodItemRepository.updateFoodItem(householdId, updatedFoodItem)

    // Advance time if necessary
    advanceUntilIdle()

    // Assertions
    val foodItems = foodItemRepository.foodItems.value
    assertEquals(1, foodItems.size)
    assertEquals(updatedFoodItem, foodItems[0])
  }

  @Test
  fun `deleteFoodItem deletes item from Firestore and local cache`() = runTest {
    val existingFoodItem =
        FoodItem(
            uid = "item1",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "123456789",
                    quantity = Quantity(amount = 100.0, unit = FoodUnit.GRAM)),
            buyDate = null,
            expiryDate = null,
            openDate = null,
            location = FoodStorageLocation.PANTRY,
            status = FoodStatus.UNOPENED,
            owner = "user1")

    val mockTask: Task<Void> = Tasks.forResult(null)
    `when`(mockDocument.delete()).thenReturn(mockTask)

    // Set initial food items in cache
    val foodItemsField: Field = foodItemRepository.javaClass.getDeclaredField("_foodItems")
    foodItemsField.isAccessible = true
    val _foodItems = foodItemsField.get(foodItemRepository) as MutableStateFlow<List<FoodItem>>
    _foodItems.value = listOf(existingFoodItem)

    foodItemRepository.deleteFoodItem(householdId, "item1")

    val foodItems = foodItemRepository.foodItems.value

    assertEquals(0, foodItems.size)
  }
}
