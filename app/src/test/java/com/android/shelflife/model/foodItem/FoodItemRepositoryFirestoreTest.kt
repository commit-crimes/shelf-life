package com.android.shelflife.model.foodItem

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodUnit
import com.android.shelfLife.model.foodItem.NutritionFacts
import com.android.shelfLife.model.foodItem.Quantity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class FoodItemRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var foodItemRepositoryFirestore: FoodItemRepositoryFirestore

  private val foodItem =
      FoodItem(
          uid = "1",
          name = "Almond Butter",
          barcode = "123456789",
          quantity = Quantity(1.0, FoodUnit.GRAM),
          expiryDate = Timestamp.now(),
          buyDate = Timestamp.now(),
          status = FoodStatus.CLOSED,
          nutritionFacts = NutritionFacts())

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

    // Ensure Firestore collection method was called to reference the "foodItems" collection
    verify(mockDocumentReference).set(foodItemCaptor.capture())

    // Assert the captured values match the expected values
    val capturedFoodItem = foodItemCaptor.firstValue
    assert(capturedFoodItem.uid == "1")
    assert(capturedFoodItem.name == "Almond Butter")
    assert(capturedFoodItem.barcode == "123456789")
    assert(capturedFoodItem.quantity.amount == 1.0)
    assert(capturedFoodItem.quantity.unit == FoodUnit.GRAM)
    assert(capturedFoodItem.status == FoodStatus.CLOSED)
  }

  @Test
  fun deleteFoodItemById_shouldCallDocumentReferenceDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    foodItemRepositoryFirestore.deleteFoodItemById("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockDocumentReference).delete()
  }

}
