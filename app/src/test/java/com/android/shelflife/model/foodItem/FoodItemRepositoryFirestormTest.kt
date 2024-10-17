package com.android.shelflife.model.foodFacts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FoodItemRepositoryFirestoreTest {

  @Mock private lateinit var db: FirebaseFirestore

  @Mock private lateinit var auth: FirebaseAuth

  @Mock private lateinit var authStateListener: FirebaseAuth.AuthStateListener

  @Mock private lateinit var currentUser: FirebaseUser

  @Mock private lateinit var collectionReference: CollectionReference

  @Mock private lateinit var documentReference: DocumentReference

  @Mock private lateinit var querySnapshot: QuerySnapshot

  @Mock private lateinit var documentSnapshot: DocumentSnapshot

  @Mock private lateinit var taskVoid: Task<Void>

  @Mock private lateinit var taskQuerySnapshot: Task<QuerySnapshot>

  @Mock private lateinit var timestamp: Timestamp

  @Captor
  private lateinit var authStateListenerCaptor: ArgumentCaptor<FirebaseAuth.AuthStateListener>

  @Captor
  private lateinit var onCompleteListenerCaptor: ArgumentCaptor<OnCompleteListener<QuerySnapshot>>

  @Captor private lateinit var onSuccessListenerCaptor: ArgumentCaptor<OnSuccessListener<Void>>

  @Captor private lateinit var onFailureListenerCaptor: ArgumentCaptor<OnFailureListener>

  private lateinit var repo: FoodItemRepositoryFirestore

  @Before
  fun setUp() {
    // Initialize FirebaseApp with the application context if not already initialized
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Proceed with setting up mocks and initializing repository
    MockitoAnnotations.openMocks(this)

    // Mock FirebaseAuth instance
    `when`(auth.addAuthStateListener(any())).then {
      authStateListener = it.getArgument(0)
      authStateListener.onAuthStateChanged(auth)
      null
    }

    // Mock FirebaseFirestore instance
    `when`(db.collection(anyString())).thenReturn(collectionReference)
    `when`(collectionReference.document()).thenReturn(documentReference)
    `when`(collectionReference.document(anyString())).thenReturn(documentReference)
    `when`(documentReference.id).thenReturn("new_document_id")
    `when`(taskVoid.addOnSuccessListener(any())).thenReturn(taskVoid)
    `when`(taskVoid.addOnFailureListener(any())).thenReturn(taskVoid)
    `when`(taskQuerySnapshot.addOnCompleteListener(any())).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

    // Initialize the repository with the mocked FirebaseFirestore
    repo = FoodItemRepositoryFirestore(db)
  }

  @Test
  fun testGetNewUid_returnsNonEmptyString() {
    val uid = repo.getNewUid()
    assertNotNull(uid)
    assertEquals("new_document_id", uid)
  }

  @Test
  fun testGetFoodItems_onSuccess_callsOnSuccessWithFoodItems() {
    // Mock Firestore query results
    val queryDocumentSnapshot = mock(QueryDocumentSnapshot::class.java)
    `when`(collectionReference.get()).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenAnswer {
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
          assertEquals(1, foodItems.size)
          val foodItem = foodItems[0]
          assertEquals("test_uid", foodItem.uid)
          assertEquals("Test Food", foodItem.foodFacts.name)
        },
        onFailure = { fail("onFailure should not be called") })

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testGetFoodItems_onFailure_callsOnFailure() {
    `when`(collectionReference.get()).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnFailureListener(any())).thenAnswer {
      val listener = it.getArgument<OnFailureListener>(0)
      listener.onFailure(Exception("Test Exception"))
      taskQuerySnapshot
    }

    var onFailureCalled = false
    repo.getFoodItems(
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          assertEquals("Test Exception", exception.message)
        })

    assertTrue(onFailureCalled)
  }

  @Test
  fun testAddFoodItem_onSuccess_callsOnSuccess() {
    val foodItem = mock(FoodItem::class.java)
    `when`(foodItem.uid).thenReturn("test_uid")
    `when`(documentReference.set(foodItem)).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(any())).thenAnswer {
      val listener = it.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      taskVoid
    }

    var onSuccessCalled = false
    repo.addFoodItem(
        foodItem,
        onSuccess = { onSuccessCalled = true },
        onFailure = { fail("onFailure should not be called") })

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testAddFoodItem_onFailure_callsOnFailure() {
    val foodItem = mock(FoodItem::class.java)
    `when`(foodItem.uid).thenReturn("test_uid")
    `when`(documentReference.set(foodItem)).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(any())).thenReturn(taskVoid)
    `when`(taskVoid.addOnFailureListener(any())).thenAnswer {
      val listener = it.getArgument<OnFailureListener>(0)
      listener.onFailure(Exception("Test Exception"))
      taskVoid
    }

    var onFailureCalled = false
    repo.addFoodItem(
        foodItem,
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          assertEquals("Test Exception", exception.message)
        })

    assertTrue(onFailureCalled)
  }

  @Test
  fun testUpdateFoodItem_onSuccess_callsOnSuccess() {
    val foodItem = mock(FoodItem::class.java)
    `when`(foodItem.uid).thenReturn("test_uid")
    `when`(documentReference.set(foodItem)).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(any())).thenAnswer {
      val listener = it.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      taskVoid
    }

    var onSuccessCalled = false
    repo.updateFoodItem(
        foodItem,
        onSuccess = { onSuccessCalled = true },
        onFailure = { fail("onFailure should not be called") })

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testUpdateFoodItem_onFailure_callsOnFailure() {
    val foodItem = mock(FoodItem::class.java)
    `when`(foodItem.uid).thenReturn("test_uid")
    `when`(documentReference.set(foodItem)).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(any())).thenReturn(taskVoid)
    `when`(taskVoid.addOnFailureListener(any())).thenAnswer {
      val listener = it.getArgument<OnFailureListener>(0)
      listener.onFailure(Exception("Test Exception"))
      taskVoid
    }

    var onFailureCalled = false
    repo.updateFoodItem(
        foodItem,
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          assertEquals("Test Exception", exception.message)
        })

    assertTrue(onFailureCalled)
  }

  @Test
  fun testDeleteFoodItemById_onSuccess_callsOnSuccess() {
    `when`(documentReference.delete()).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(any())).thenAnswer {
      val listener = it.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      taskVoid
    }

    var onSuccessCalled = false
    repo.deleteFoodItemById(
        "test_uid",
        onSuccess = { onSuccessCalled = true },
        onFailure = { fail("onFailure should not be called") })

    assertTrue(onSuccessCalled)
  }

  @Test
  fun testDeleteFoodItemById_onFailure_callsOnFailure() {
    `when`(documentReference.delete()).thenReturn(taskVoid)
    `when`(taskVoid.addOnSuccessListener(any())).thenReturn(taskVoid)
    `when`(taskVoid.addOnFailureListener(any())).thenAnswer {
      val listener = it.getArgument<OnFailureListener>(0)
      listener.onFailure(Exception("Test Exception"))
      taskVoid
    }

    var onFailureCalled = false
    repo.deleteFoodItemById(
        "test_uid",
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { exception ->
          onFailureCalled = true
          assertEquals("Test Exception", exception.message)
        })

    assertTrue(onFailureCalled)
  }

  @Test
  fun testConvertToFoodItem_validDocument_returnsFoodItem() {
    mockDocumentSnapshot(documentSnapshot)

    val foodItem = repo.convertToFoodItem(documentSnapshot)

    assertNotNull(foodItem)
    assertEquals("test_uid", foodItem?.uid)
    assertEquals("Test Food", foodItem?.foodFacts?.name)
    assertEquals("1234567890", foodItem?.foodFacts?.barcode)
    foodItem?.foodFacts?.quantity?.amount?.let { assertEquals(100.0, it, 0.0) }
    assertEquals(FoodUnit.GRAM, foodItem?.foodFacts?.quantity?.unit)
    assertEquals(200, foodItem?.foodFacts?.nutritionFacts?.energyKcal)
    assertEquals(FoodCategory.DAIRY, foodItem?.foodFacts?.category)
    assertEquals(FoodStatus.OPEN, foodItem?.status)
    assertEquals(FoodStorageLocation.FRIDGE, foodItem?.location)
  }

  @Test
  fun testConvertToFoodItem_missingRequiredField_returnsNull() {
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
  fun testConvertToFoodItemFromMap_ValidInput() {
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
    assertEquals("testUid", foodItem?.uid)
    assertEquals("Apple", foodItem?.foodFacts?.name)
    foodItem?.foodFacts?.quantity?.amount?.let { assertEquals(1.5, it, 0.0) }
    assertEquals(FoodUnit.GRAM, foodItem?.foodFacts?.quantity?.unit)
    assertEquals(FoodStatus.OPEN, foodItem?.status)
  }

  @Test
  fun testConvertToFoodItemFromMap_MissingFields() {
    val incompleteMap =
        mapOf(
            "uid" to "testUid",
            "name" to "Apple",
            "quantity" to mapOf("amount" to 1.5, "unit" to "GRAM"))

    val foodItem =
        FoodItemRepositoryFirestore(FirebaseFirestore.getInstance())
            .convertToFoodItemFromMap(incompleteMap)

    assertNotNull(foodItem)
    assertEquals("testUid", foodItem?.uid)
    assertEquals("Apple", foodItem?.foodFacts?.name)
    assertEquals("", foodItem?.foodFacts?.barcode) // Expect default empty string for barcode
  }

  @Test
  fun testConvertToFoodItemFromMap_NullValues() {
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
  fun testConvertFoodItemToMap_ValidFoodItem() {
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

    assertEquals("testUid", map["uid"])
    assertEquals("Apple", map["name"])
    assertEquals("123456789012", map["barcode"])
    assertEquals(2.0, (map["quantity"] as Map<*, *>)["amount"])
    assertEquals("GRAM", (map["quantity"] as Map<*, *>)["unit"])
  }

  @Test
  fun testConvertFoodItemToMap_DefaultValues() {
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

    assertEquals("Apple", map["name"])
    assertEquals("", map["barcode"]) // Default barcode should be empty
  }
}
