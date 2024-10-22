package com.android.shelflife.model.foodItem

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.lang.reflect.Method
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FoodItemRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var auth: FirebaseAuth
  @Mock private lateinit var authStateListener: FirebaseAuth.AuthStateListener

  @Mock private lateinit var collectionReference: CollectionReference

  @Mock private lateinit var documentReference: DocumentReference

  @Mock private lateinit var querySnapshot: QuerySnapshot

  @Mock private lateinit var documentSnapshot: DocumentSnapshot

  @Mock private lateinit var taskVoid: Task<Void>

  @Mock private lateinit var taskQuerySnapshot: Task<QuerySnapshot>

  @Mock private lateinit var timestamp: Timestamp

  private lateinit var repo: FoodItemRepositoryFirestore

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
  private val mockFoodItem = mock(FoodItem::class.java)

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
    // Initialize FirebaseApp with the application context if not already initialized
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Proceed with setting up mocks and initializing repository
    MockitoAnnotations.openMocks(this)

    // Mock FirebaseAuth instance
    `when`(auth.addAuthStateListener(ArgumentMatchers.any())).then {
      authStateListener = it.getArgument(0)
      authStateListener.onAuthStateChanged(auth)
      null
    }

    // Mock FirebaseFirestore instance
    `when`(db.collection(anyString())).thenReturn(collectionReference)
    `when`(collectionReference.document()).thenReturn(documentReference)
    `when`(collectionReference.document(anyString())).thenReturn(documentReference)
    `when`(documentReference.id).thenReturn("new_document_id")
    `when`(taskVoid.addOnSuccessListener(ArgumentMatchers.any())).thenReturn(taskVoid)
    `when`(taskVoid.addOnFailureListener(ArgumentMatchers.any())).thenReturn(taskVoid)
    `when`(taskQuerySnapshot.addOnCompleteListener(ArgumentMatchers.any()))
        .thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(ArgumentMatchers.any()))
        .thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnFailureListener(ArgumentMatchers.any()))
        .thenReturn(taskQuerySnapshot)

    // Initialize the repository with the mocked FirebaseFirestore
    repo = FoodItemRepositoryFirestore(db)
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

  @Test
  fun testGetNewUid_returnsNonEmptyString() {
    val uid = repo.getNewUid()
    assertNotNull(uid)
    Assert.assertEquals("new_document_id", uid)
  }

  @Test
  fun testGetFI_onSuccess_callsOnSuccessWithFoodItems() {
    // Mock Firestore query results
    val queryDocumentSnapshot = mock(QueryDocumentSnapshot::class.java)
    `when`(collectionReference.get()).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(ArgumentMatchers.any())).thenAnswer {
      val listener = it.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(querySnapshot)
      taskQuerySnapshot
    }

    // Return an iterator with a QueryDocumentSnapshot
    `when`(querySnapshot.iterator())
        .thenReturn(
            listOf(queryDocumentSnapshot).iterator() as MutableIterator<QueryDocumentSnapshot>?)

    // Mock queryDocumentSnapshot to return valid data
    mockDocumentSnapshot(queryDocumentSnapshot)

    var onSuccessCalled = false
    repo.getFoodItems(
        onSuccess = { foodItems ->
          onSuccessCalled = true
          Assert.assertEquals(1, foodItems.size)
          val foodItem = foodItems[0]
          Assert.assertEquals("test_uid", foodItem.uid)
          Assert.assertEquals("Test Food", foodItem.foodFacts.name)
        },
        onFailure = { Assert.fail("onFailure should not be called") })

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testGetFI_onFailure_callsOnFailure() {
    `when`(collectionReference.get()).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(ArgumentMatchers.any()))
        .thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnFailureListener(ArgumentMatchers.any())).thenAnswer {
      val listener = it.getArgument<OnFailureListener>(0)
      listener.onFailure(Exception("Test Exception"))
      taskQuerySnapshot
    }

    var onFailureCalled = false
    repo.getFoodItems(
        onSuccess = { Assert.fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          Assert.assertEquals("Test Exception", exception.message)
        })

    assertTrue(onFailureCalled)
  }

  @Test
  fun testAddFI_onSuccess_callsOnSuccess() {
    `when`(mockFoodItem.uid).thenReturn("test_uid")
    `when`(documentReference.set(mockFoodItem)).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(ArgumentMatchers.any())).thenAnswer {
      val listener = it.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      taskVoid
    }

    var onSuccessCalled = false
    repo.addFoodItem(
        mockFoodItem,
        onSuccess = { onSuccessCalled = true },
        onFailure = { Assert.fail("onFailure should not be called") })

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testAddFI_onFailure_callsOnFailure() {
    `when`(mockFoodItem.uid).thenReturn("test_uid")
    `when`(documentReference.set(mockFoodItem)).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(ArgumentMatchers.any())).thenReturn(taskVoid)
    `when`(taskVoid.addOnFailureListener(ArgumentMatchers.any())).thenAnswer {
      val listener = it.getArgument<OnFailureListener>(0)
      listener.onFailure(Exception("Test Exception"))
      taskVoid
    }

    var onFailureCalled = false
    repo.addFoodItem(
        mockFoodItem,
        onSuccess = { Assert.fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          Assert.assertEquals("Test Exception", exception.message)
        })

    assertTrue(onFailureCalled)
  }

  @Test
  fun testUpFI_onSuccess_callsOnSuccess() {
    `when`(mockFoodItem.uid).thenReturn("test_uid")
    `when`(documentReference.set(mockFoodItem)).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(ArgumentMatchers.any())).thenAnswer {
      val listener = it.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      taskVoid
    }

    var onSuccessCalled = false
    repo.updateFoodItem(
        mockFoodItem,
        onSuccess = { onSuccessCalled = true },
        onFailure = { Assert.fail("onFailure should not be called") })

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testUpFI_onFailure_callsOnFailure() {
    `when`(mockFoodItem.uid).thenReturn("test_uid")
    `when`(documentReference.set(mockFoodItem)).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(ArgumentMatchers.any())).thenReturn(taskVoid)
    `when`(taskVoid.addOnFailureListener(ArgumentMatchers.any())).thenAnswer {
      val listener = it.getArgument<OnFailureListener>(0)
      listener.onFailure(Exception("Test Exception"))
      taskVoid
    }

    var onFailureCalled = false
    repo.updateFoodItem(
        mockFoodItem,
        onSuccess = { Assert.fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          Assert.assertEquals("Test Exception", exception.message)
        })

    assertTrue(onFailureCalled)
  }

  @Test
  fun testDelFIById_onSuccess_callsOnSuccess() {
    `when`(documentReference.delete()).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(ArgumentMatchers.any())).thenAnswer {
      val listener = it.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      taskVoid
    }

    var onSuccessCalled = false
    repo.deleteFoodItemById(
        "test_uid",
        onSuccess = { onSuccessCalled = true },
        onFailure = { Assert.fail("onFailure should not be called") })

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testDelFIById_onFailure_callsOnFailure() {
    `when`(documentReference.delete()).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(ArgumentMatchers.any())).thenReturn(taskVoid)
    `when`(taskVoid.addOnFailureListener(ArgumentMatchers.any())).thenAnswer {
      val listener = it.getArgument<OnFailureListener>(0)
      listener.onFailure(Exception("Test Exception"))
      taskVoid
    }

    var onFailureCalled = false
    repo.deleteFoodItemById(
        "test_uid",
        onSuccess = { Assert.fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          Assert.assertEquals("Test Exception", exception.message)
        })

    assertTrue(onFailureCalled)
  }

  @Test
  fun testConvToFI_validDocument_returnsFoodItem() {
    mockDocumentSnapshot(documentSnapshot)

    val foodItem = repo.convertToFoodItem(documentSnapshot)

    assertNotNull(foodItem)
    Assert.assertEquals("test_uid", foodItem?.uid)
    Assert.assertEquals("Test Food", foodItem?.foodFacts?.name)
    Assert.assertEquals("1234567890", foodItem?.foodFacts?.barcode)
    foodItem?.foodFacts?.quantity?.amount?.let { Assert.assertEquals(100.0, it, 0.0) }
    Assert.assertEquals(FoodUnit.GRAM, foodItem?.foodFacts?.quantity?.unit)
    Assert.assertEquals(200, foodItem?.foodFacts?.nutritionFacts?.energyKcal)
    Assert.assertEquals(FoodCategory.DAIRY, foodItem?.foodFacts?.category)
    Assert.assertEquals(FoodStatus.OPEN, foodItem?.status)
    Assert.assertEquals(FoodStorageLocation.FRIDGE, foodItem?.location)
  }

  @Test
  fun testConvToFI_missingRequiredField_returnsNull() {
    `when`(documentSnapshot.getString("uid")).thenReturn(null)

    val foodItem = repo.convertToFoodItem(documentSnapshot)

    assertNull(foodItem)
  }

  // Helper function to mock DocumentSnapshot
  private fun mockDocumentSnapshot(documentSnapshot: DocumentSnapshot) {
    val uid = "test_uid"
    val name = "Test Food"
    val barcode = "1234567890"
    val quantityMap = mapOf("amount" to 100.0, "unit" to "GRAM")
    val nutritionFactsMap =
        mapOf(
            "energyKcal" to 200L,
            "fat" to 10.0,
            "saturatedFat" to 5.0,
            "carbohydrates" to 30.0,
            "sugars" to 20.0,
            "proteins" to 15.0,
            "salt" to 1.0)
    val expiryDate = timestamp
    val buyDate = timestamp
    val status = "OPEN"
    val location = mapOf("location" to "FRIDGE")
    val category = "DAIRY"

    `when`(documentSnapshot.getString("uid")).thenReturn(uid)
    `when`(documentSnapshot.getString("name")).thenReturn(name)
    `when`(documentSnapshot.getString("barcode")).thenReturn(barcode)
    `when`(documentSnapshot.get("quantity")).thenReturn(quantityMap)
    `when`(documentSnapshot.get("nutritionFacts")).thenReturn(nutritionFactsMap)
    `when`(documentSnapshot.getString("category")).thenReturn(category)
    `when`(documentSnapshot.getTimestamp("expiryDate")).thenReturn(expiryDate)
    `when`(documentSnapshot.getTimestamp("buyDate")).thenReturn(buyDate)
    `when`(documentSnapshot.getString("status")).thenReturn(status)
    `when`(documentSnapshot.get("location")).thenReturn(location)
  }

  @Test
  fun testConvertToFIFromMap_ValidInput() {
    val validMap =
        mapOf(
            "uid" to "testUid",
            "name" to "Apple",
            "barcode" to "123456789012",
            "quantity" to mapOf("amount" to 1.5, "unit" to "GRAM"),
            "expiryDate" to Timestamp.now(),
            "status" to "OPEN")

    val foodItem =
        FoodItemRepositoryFirestore(FirebaseFirestore.getInstance())
            .convertToFoodItemFromMap(validMap)

    assertNotNull(foodItem)
    Assert.assertEquals("testUid", foodItem?.uid)
    Assert.assertEquals("Apple", foodItem?.foodFacts?.name)
    foodItem?.foodFacts?.quantity?.amount?.let { Assert.assertEquals(1.5, it, 0.0) }
    Assert.assertEquals(FoodUnit.GRAM, foodItem?.foodFacts?.quantity?.unit)
    Assert.assertEquals(FoodStatus.OPEN, foodItem?.status)
  }

  @Test
  fun testConvToFIFromMap_MissingFields() {
    val incompleteMap =
        mapOf(
            "uid" to "testUid",
            "name" to "Apple",
            "quantity" to mapOf("amount" to 1.5, "unit" to "GRAM"))

    val foodItem =
        FoodItemRepositoryFirestore(FirebaseFirestore.getInstance())
            .convertToFoodItemFromMap(incompleteMap)

    assertNotNull(foodItem)
    Assert.assertEquals("testUid", foodItem?.uid)
    Assert.assertEquals("Apple", foodItem?.foodFacts?.name)
    Assert.assertEquals("", foodItem?.foodFacts?.barcode) // Expect default empty string for barcode
  }

  @Test
  fun testConvToFIFromMap_NullValues() {
    val mapWithNulls =
        mapOf(
            "uid" to "testUid",
            "name" to null,
            "quantity" to mapOf("amount" to 1.5, "unit" to "GRAM"))

    val foodItem =
        FoodItemRepositoryFirestore(FirebaseFirestore.getInstance())
            .convertToFoodItemFromMap(mapWithNulls)

    assertNull(foodItem) // Expect null because "name" is required
  }

  @Test
  fun testConvertFIToMap_ValidFoodItem() {
    val quantity = Quantity(2.0, FoodUnit.GRAM)
    val foodFacts = FoodFacts(name = "Apple", barcode = "123456789012", quantity = quantity)
    val foodItem =
        FoodItem(
            uid = "testUid",
            foodFacts = foodFacts,
            expiryDate = Timestamp.now(),
            buyDate = Timestamp.now(),
            status = FoodStatus.OPEN,
            location = FoodStorageLocation.PANTRY)

    val map =
        FoodItemRepositoryFirestore(FirebaseFirestore.getInstance()).convertFoodItemToMap(foodItem)

    Assert.assertEquals("testUid", map["uid"])
    Assert.assertEquals("Apple", map["name"])
    Assert.assertEquals("123456789012", map["barcode"])
    Assert.assertEquals(2.0, (map["quantity"] as Map<*, *>)["amount"])
    Assert.assertEquals("GRAM", (map["quantity"] as Map<*, *>)["unit"])
  }

  @Test
  fun testConvFIToMap_DefaultValues() {
    val quantity = Quantity(1.0, FoodUnit.GRAM)
    val foodFacts = FoodFacts(name = "Apple", quantity = quantity)
    val foodItem =
        FoodItem(
            uid = "testUid",
            foodFacts = foodFacts,
            expiryDate = Timestamp.now(),
            buyDate = Timestamp.now(),
            status = FoodStatus.CLOSED,
            location = FoodStorageLocation.PANTRY)

    val map =
        FoodItemRepositoryFirestore(FirebaseFirestore.getInstance()).convertFoodItemToMap(foodItem)

    Assert.assertEquals("Apple", map["name"])
    Assert.assertEquals("", map["barcode"]) // Default barcode should be empty
  }
}
