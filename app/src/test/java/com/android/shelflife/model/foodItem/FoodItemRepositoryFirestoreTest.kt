package com.android.shelfLife.model.foodItem

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.lang.reflect.Method
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class FoodItemRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var foodItemRepositoryFirestore: FoodItemRepositoryFirestore

  private val foodFacts =
      FoodFacts(
          name = "Almond Butter",
          barcode = "123456789",
          quantity = Quantity(1.0, FoodUnit.GRAM),
          category = FoodCategory.OTHER,
          nutritionFacts = NutritionFacts(),
      )

  private val foodItem =
      FoodItem(
          uid = "1",
          foodFacts = foodFacts,
          location = FoodStorageLocation.PANTRY,
          expiryDate = Timestamp.now(),
          buyDate = Timestamp.now(),
          status = FoodStatus.CLOSED,
      )

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    foodItemRepositoryFirestore = FoodItemRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = foodItemRepositoryFirestore.getNewUid()
    assert(uid == "1")
  }

  @Test
  fun getFoodItems_callsDocuments() {
    // Ensure that mockQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockQuerySnapshot.documents).thenReturn(listOf())

    // Call the method under test
    foodItemRepositoryFirestore.getFoodItems(
        onSuccess = {
          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { mockQuerySnapshot.documents }
  }

  @Test
  fun addFoodItem_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    // This test verifies that when we add a new FoodItem, the Firestore `collection()` method is
    // called.
    foodItemRepositoryFirestore.addFoodItem(foodItem, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()
    val foodItemCaptor = argumentCaptor<FoodItem>()

    // Ensure Firestore's documentReference `set` method was called with the correct FoodItem.
    verify(mockDocumentReference).set(foodItemCaptor.capture())

    // Assert the captured values match the expected values
    val capturedFoodItem = foodItemCaptor.firstValue

    // Assertions for FoodFacts properties
    assert(capturedFoodItem.foodFacts.name == "Almond Butter")
    assert(capturedFoodItem.foodFacts.barcode == "123456789")
    assert(capturedFoodItem.foodFacts.quantity.amount == 1.0)
    assert(capturedFoodItem.foodFacts.quantity.unit == FoodUnit.GRAM)

    // Assertions for FoodItem-specific properties
    assert(capturedFoodItem.uid == "1")
    assert(capturedFoodItem.status == FoodStatus.CLOSED)

    // Check the location of the food item
    assert(capturedFoodItem.location == FoodStorageLocation.PANTRY)

    // Check the timestamp values
    assert(capturedFoodItem.expiryDate != null) // Make sure expiry date is not null
    assert(capturedFoodItem.buyDate != null) // Make sure buy date is set
    assert(capturedFoodItem.openDate == null) // Verify open date is null by default
  }

  @Test
  fun deleteFoodItemById_shouldCallDocumentReferenceDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    foodItemRepositoryFirestore.deleteFoodItemById("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockDocumentReference).delete()
  }

  /**
   * Uses "hacky" reflection to test private method (Prof. Candea's suggestion:
   * https://edstem.org/eu/courses/1567/discussion/131808)
   */
  @Test
  fun convertToFoodItem_withValidDocumentSnapshot_shouldReturnFoodItem() {
    // Mocking a valid DocumentSnapshot
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Almond Butter")
    `when`(mockDocumentSnapshot.getString("barcode")).thenReturn("123456789")
    `when`(mockDocumentSnapshot.get("quantity"))
        .thenReturn(mapOf("amount" to 1.0, "unit" to "GRAM"))
    `when`(mockDocumentSnapshot.get("nutritionFacts"))
        .thenReturn(mapOf("energyKcal" to 100L, "fat" to 10.0))
    `when`(mockDocumentSnapshot.getString("category")).thenReturn(FoodCategory.OTHER.name)
    `when`(mockDocumentSnapshot.getTimestamp("expiryDate")).thenReturn(Timestamp.now())
    `when`(mockDocumentSnapshot.getTimestamp("buyDate")).thenReturn(Timestamp.now())
    `when`(mockDocumentSnapshot.getString("status")).thenReturn(FoodStatus.CLOSED.name)
    `when`(mockDocumentSnapshot.get("location"))
        .thenReturn(mapOf("location" to FoodStorageLocation.PANTRY.name))

    // Call the method under test
    val method: Method =
        FoodItemRepositoryFirestore::class
            .java
            .getDeclaredMethod("convertToFoodItem", DocumentSnapshot::class.java)
    method.isAccessible = true // Make the method accessible

    // Invoke the private method
    val foodItem = method.invoke(foodItemRepositoryFirestore, mockDocumentSnapshot) as FoodItem?

    // Assertions for the returned FoodItem
    assert(foodItem != null)
    assert(foodItem?.uid == "1")
    assert(foodItem?.foodFacts?.name == "Almond Butter")
    assert(foodItem?.foodFacts?.barcode == "123456789")
    assert(foodItem?.foodFacts?.quantity?.amount == 1.0)
    assert(foodItem?.foodFacts?.quantity?.unit == FoodUnit.GRAM)
    assert(foodItem?.foodFacts?.nutritionFacts?.energyKcal == 100)
    assert(foodItem?.foodFacts?.nutritionFacts?.fat == 10.0)
    assert(foodItem?.foodFacts?.category == FoodCategory.OTHER)
    assert(foodItem?.status == FoodStatus.CLOSED)
    assert(foodItem?.location == FoodStorageLocation.PANTRY)
  }

  @Test
  fun convertToFoodItem_withMissingNutritionFacts_shouldReturnDefaultValues() {
    // Mocking a DocumentSnapshot with missing nutrition facts
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Almond Butter")
    `when`(mockDocumentSnapshot.getString("barcode")).thenReturn("123456789")
    `when`(mockDocumentSnapshot.get("quantity"))
        .thenReturn(mapOf("amount" to 1.0, "unit" to "GRAM"))
    `when`(mockDocumentSnapshot.get("nutritionFacts")).thenReturn(null)

    // Call the method under test
    val method: Method =
        FoodItemRepositoryFirestore::class
            .java
            .getDeclaredMethod("convertToFoodItem", DocumentSnapshot::class.java)
    method.isAccessible = true // Make the method accessible

    // Invoke the private method
    val foodItem = method.invoke(foodItemRepositoryFirestore, mockDocumentSnapshot) as FoodItem?

    // Assertions for the returned FoodItem
    assert(foodItem != null)
    assert(foodItem?.foodFacts?.nutritionFacts?.energyKcal == 0)
    assert(foodItem?.foodFacts?.nutritionFacts?.fat == 0.0)
    assert(foodItem?.foodFacts?.nutritionFacts?.carbohydrates == 0.0)
    assert(foodItem?.foodFacts?.nutritionFacts?.sugars == 0.0)
    assert(foodItem?.foodFacts?.nutritionFacts?.proteins == 0.0)
    assert(foodItem?.foodFacts?.nutritionFacts?.salt == 0.0)
  }

  @Test
  fun convertToFoodItemFromMap_withValidData_shouldReturnFoodItem() {
    val map =
        mapOf(
            "uid" to "1",
            "name" to "Almond Butter",
            "barcode" to "123456789",
            "quantity" to mapOf("amount" to 1.0, "unit" to "GRAM"),
            "expiryDate" to Timestamp.now(),
            "buyDate" to Timestamp.now(),
            "status" to "CLOSED",
            "location" to mapOf("location" to "PANTRY"),
            "category" to "OTHER")

    val foodItem = foodItemRepositoryFirestore.convertToFoodItemFromMap(map)

    assertNotNull(foodItem)
    assertEquals("1", foodItem?.uid)
    assertEquals("Almond Butter", foodItem?.foodFacts?.name)
    assertEquals("123456789", foodItem?.foodFacts?.barcode)
    assertEquals(1.0, foodItem?.foodFacts?.quantity?.amount)
    assertEquals(FoodUnit.GRAM, foodItem?.foodFacts?.quantity?.unit)
    assertEquals(FoodCategory.OTHER, foodItem?.foodFacts?.category)
    assertEquals(FoodStatus.CLOSED, foodItem?.status)
    assertEquals(FoodStorageLocation.PANTRY, foodItem?.location)
  }

  @Test
  fun convertToFoodItemFromMap_withInvalidData_shouldReturnNull() {
    val map =
        mapOf(
            "uid" to "1",
            "name" to "Almond Butter",
            "quantity" to mapOf("amount" to 1.0, "unit" to "INVALID_UNIT"), // Invalid unit
            "expiryDate" to Timestamp.now(),
            "buyDate" to Timestamp.now(),
            "status" to "CLOSED",
            "location" to mapOf("location" to "PANTRY"))

    val foodItem = foodItemRepositoryFirestore.convertToFoodItemFromMap(map)

    assertNull(foodItem)
  }

  @Test
  fun convertFoodItemToMap_withValidData_shouldReturnMap() {
    val foodItem =
        FoodItem(
            uid = "1",
            foodFacts =
                FoodFacts(
                    name = "Almond Butter",
                    barcode = "123456789",
                    quantity = Quantity(1.0, FoodUnit.GRAM),
                    category = FoodCategory.OTHER,
                    nutritionFacts =
                        NutritionFacts(
                            energyKcal = 100,
                            fat = 10.0,
                            saturatedFat = 2.0,
                            carbohydrates = 20.0,
                            sugars = 5.0,
                            proteins = 8.0,
                            salt = 0.5)),
            location = FoodStorageLocation.PANTRY,
            expiryDate = Timestamp.now(),
            buyDate = Timestamp.now(),
            status = FoodStatus.CLOSED)

    val map = foodItemRepositoryFirestore.convertFoodItemToMap(foodItem)

    assertNotNull(map)
    assertEquals("1", map["uid"])
    assertEquals("Almond Butter", map["name"])
    assertEquals("123456789", map["barcode"])
    assertEquals(mapOf("amount" to 1.0, "unit" to "GRAM"), map["quantity"])
    assertEquals("OTHER", map["category"])
    assertEquals("CLOSED", map["status"])
    assertEquals("PANTRY", map["location"])
    assertEquals(
        mapOf(
            "energyKcal" to 100,
            "fat" to 10.0,
            "saturatedFat" to 2.0,
            "carbohydrates" to 20.0,
            "sugars" to 5.0,
            "proteins" to 8.0,
            "salt" to 0.5),
        map["nutritionFacts"])
  }
}
