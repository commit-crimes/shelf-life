package com.android.shelflife.model.household

import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HouseholdRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockUser: FirebaseUser
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockFoodItemRepository: FoodItemRepositoryFirestore

  private lateinit var householdRepository: HouseholdRepositoryFirestore

  private val household =
      HouseHold(
          uid = "1", name = "Test Household", members = listOf("testUserId"), foodItems = listOf())

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    // Mock FirebaseAuth and FirebaseUser
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn("testUserId")

    // Mock FirebaseFirestore
    `when`(mockFirestore.collection(any<String>())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any<String>())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)

    // Mock whereArrayContains to return a Query
    `when`(mockCollectionReference.whereArrayContains(any<String>(), any())).thenReturn(mockQuery)

    // Mock Query.get() to return a Task<QuerySnapshot>
    val mockTaskQuerySnapshot: Task<QuerySnapshot> = mock()
    `when`(mockQuery.get()).thenReturn(mockTaskQuerySnapshot)
    `when`(mockTaskQuerySnapshot.addOnSuccessListener(any<OnSuccessListener<QuerySnapshot>>()))
        .thenReturn(mockTaskQuerySnapshot)
    `when`(mockTaskQuerySnapshot.addOnFailureListener(any<OnFailureListener>()))
        .thenReturn(mockTaskQuerySnapshot)

    // Initialize the HouseholdRepositoryFirestore with mocks
    householdRepository =
        HouseholdRepositoryFirestore(mockFirestore).apply {
          auth = mockAuth // Inject the mocked FirebaseAuth
          foodItemRepository = mockFoodItemRepository // Inject the mocked FoodItemRepository
        }
  }

  @Test
  fun `getNewUid returns new document ID`() {
    `when`(mockDocumentReference.id).thenReturn("newHouseholdId")
    val uid = householdRepository.getNewUid()
    assertEquals("newHouseholdId", uid)
  }

  @Test
  fun `addHousehold calls onFailure when user is not logged in`() {
    `when`(mockAuth.currentUser).thenReturn(null)

    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    householdRepository.addHousehold(household, onSuccess, onFailure)

    verify(onFailure).invoke(any())
    verify(onSuccess, never()).invoke()
  }

  @Test
  fun `addHousehold adds household when user is logged in`() {
    var onSuccessCalled = false
    var onFailureCalled = false

    val onSuccess = { onSuccessCalled = true }
    val onFailure = { e: Exception -> onFailureCalled = true }

    val task: Task<Void> = mock()
    `when`(mockDocumentReference.set(any<Map<String, Any>>())).thenReturn(task)
    `when`(task.addOnSuccessListener(any<OnSuccessListener<Void>>())).thenReturn(task)
    `when`(task.addOnFailureListener(any<OnFailureListener>())).thenReturn(task)

    householdRepository.addHousehold(household, onSuccess, onFailure)

    // Capture the success listener and invoke it
    val successCaptor =
        ArgumentCaptor.forClass(OnSuccessListener::class.java)
            as ArgumentCaptor<OnSuccessListener<Void>>
    verify(task).addOnSuccessListener(capture(successCaptor))
    successCaptor.value.onSuccess(null)

    // Since addHousehold calls getHouseholds, we need to handle its callbacks
    // Capture the success listener for getHouseholds and invoke it
    val querySuccessCaptor =
        ArgumentCaptor.forClass(OnSuccessListener::class.java)
            as ArgumentCaptor<OnSuccessListener<QuerySnapshot>>
    verify(mockQuery.get()).addOnSuccessListener(capture(querySuccessCaptor))
    querySuccessCaptor.value.onSuccess(mockQuerySnapshot)

    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)
  }

  @Test
  fun `addHousehold calls onFailure when Firestore set fails`() {
    var onSuccessCalled = false
    var onFailureCalled = false
    var capturedException: Exception? = null

    val onSuccess = { onSuccessCalled = true }
    val onFailure = { e: Exception ->
      onFailureCalled = true
      capturedException = e
    }

    val task: Task<Void> = mock()
    `when`(mockDocumentReference.set(any<Map<String, Any>>())).thenReturn(task)
    `when`(task.addOnSuccessListener(any<OnSuccessListener<Void>>())).thenReturn(task)
    `when`(task.addOnFailureListener(any<OnFailureListener>())).thenReturn(task)

    householdRepository.addHousehold(household, onSuccess, onFailure)

    // Capture the failure listener and invoke it with an exception
    val failureCaptor = ArgumentCaptor.forClass(OnFailureListener::class.java)
    verify(task).addOnFailureListener(capture(failureCaptor))
    val exception = Exception("Firestore set failed")
    failureCaptor.value.onFailure(exception)

    assertTrue(onFailureCalled)
    assertFalse(onSuccessCalled)
    assertEquals(exception, capturedException)
  }

  @Test
  fun `deleteHouseholdById calls onFailure when user is not logged in`() {
    `when`(mockAuth.currentUser).thenReturn(null)

    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    householdRepository.deleteHouseholdById("householdId", onSuccess, onFailure)

    verify(onFailure).invoke(any())
    verify(onSuccess, never()).invoke()
  }

  @Test
  fun `deleteHouseholdById deletes household when user is logged in`() {
    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val task: Task<Void> = mock()
    `when`(mockDocumentReference.delete()).thenReturn(task)
    `when`(task.addOnSuccessListener(any())).thenReturn(task)
    `when`(task.addOnFailureListener(any())).thenReturn(task)

    householdRepository.deleteHouseholdById("householdId", onSuccess, onFailure)

    // Capture the success listener and invoke it
    val successCaptor =
        ArgumentCaptor.forClass(OnSuccessListener::class.java)
            as ArgumentCaptor<OnSuccessListener<Void>>
    verify(task).addOnSuccessListener(capture(successCaptor))
    successCaptor.value.onSuccess(null)

    verify(onSuccess).invoke()
    verify(onFailure, never()).invoke(any())
  }

  @Test
  fun `deleteHouseholdById calls onFailure when Firestore delete fails`() {
    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val task: Task<Void> = mock()
    `when`(mockDocumentReference.delete()).thenReturn(task)
    `when`(task.addOnSuccessListener(any())).thenReturn(task)
    `when`(task.addOnFailureListener(any())).thenReturn(task)

    householdRepository.deleteHouseholdById("householdId", onSuccess, onFailure)

    // Capture the failure listener and invoke it with an exception
    val failureCaptor =
        ArgumentCaptor.forClass(OnFailureListener::class.java) as ArgumentCaptor<OnFailureListener>
    verify(task).addOnFailureListener(capture(failureCaptor))
    val exception = Exception("Firestore delete failed")
    failureCaptor.value.onFailure(exception)

    verify(onFailure).invoke(exception)
    verify(onSuccess, never()).invoke()
  }

  @Test
  fun `updateHousehold calls onFailure when user is not logged in`() {
    `when`(mockAuth.currentUser).thenReturn(null)

    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    householdRepository.updateHousehold(household, onSuccess, onFailure)

    verify(onFailure).invoke(any())
    verify(onSuccess, never()).invoke()
  }

  @Test
  fun `updateHousehold updates household when user is logged in`() {
    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val task: Task<Void> = mock()
    `when`(mockDocumentReference.update(any<Map<String, Any>>())).thenReturn(task)
    `when`(task.addOnSuccessListener(any())).thenReturn(task)
    `when`(task.addOnFailureListener(any())).thenReturn(task)

    householdRepository.updateHousehold(household, onSuccess, onFailure)

    // Capture the success listener and invoke it
    val successCaptor =
        ArgumentCaptor.forClass(OnSuccessListener::class.java)
            as ArgumentCaptor<OnSuccessListener<Void>>
    verify(task).addOnSuccessListener(capture(successCaptor))
    successCaptor.value.onSuccess(null)

    verify(onSuccess).invoke()
    verify(onFailure, never()).invoke(any())
  }

  @Test
  fun `updateHousehold calls onFailure when Firestore update fails`() {
    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val task: Task<Void> = mock()
    `when`(mockDocumentReference.update(any<Map<String, Any>>())).thenReturn(task)
    `when`(task.addOnSuccessListener(any())).thenReturn(task)
    `when`(task.addOnFailureListener(any())).thenReturn(task)

    householdRepository.updateHousehold(household, onSuccess, onFailure)

    // Capture the failure listener and invoke it with an exception
    val failureCaptor =
        ArgumentCaptor.forClass(OnFailureListener::class.java) as ArgumentCaptor<OnFailureListener>
    verify(task).addOnFailureListener(capture(failureCaptor))
    val exception = Exception("Firestore update failed")
    failureCaptor.value.onFailure(exception)

    verify(onFailure).invoke(exception)
    verify(onSuccess, never()).invoke()
  }

  @Test
  fun `getHouseholds calls onFailure when user is not logged in`() {
    `when`(mockAuth.currentUser).thenReturn(null)

    val onSuccess: (List<HouseHold>) -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    householdRepository.getHouseholds(onSuccess, onFailure)

    verify(onFailure).invoke(any())
    verify(onSuccess, never()).invoke(any())
  }

  @Test
  fun `getHouseholds retrieves households when user is logged in`() {
    val onSuccess: (List<HouseHold>) -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    `when`(mockCollectionReference.whereArrayContains(any<String>(), any())).thenReturn(mockQuery)

    val task: Task<QuerySnapshot> = mock()
    `when`(mockQuery.get()).thenReturn(task)
    `when`(task.addOnSuccessListener(any())).thenReturn(task)
    `when`(task.addOnFailureListener(any())).thenReturn(task)

    // Mock the QuerySnapshot
    val documentSnapshots = listOf(mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(documentSnapshots)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test Household")
    `when`(mockDocumentSnapshot.get("members")).thenReturn(listOf("testUserId"))
    `when`(mockDocumentSnapshot.get("foodItems")).thenReturn(null)
    `when`(householdRepository.convertToHousehold(mockDocumentSnapshot)).thenCallRealMethod()

    `when`(mockFoodItemRepository.convertToFoodItemFromMap(any())).thenReturn(null)

    // Capture the success listener and invoke it with the mock QuerySnapshot
    householdRepository.getHouseholds(onSuccess, onFailure)

    val successCaptor =
        ArgumentCaptor.forClass(OnSuccessListener::class.java)
            as ArgumentCaptor<OnSuccessListener<QuerySnapshot>>
    verify(task).addOnSuccessListener(capture(successCaptor))
    successCaptor.value.onSuccess(mockQuerySnapshot)

    verify(onSuccess).invoke(any())
    verify(onFailure, never()).invoke(any())
  }

  @Test
  fun `getHouseholds calls onFailure when Firestore get fails`() {
    val onSuccess: (List<HouseHold>) -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    `when`(mockCollectionReference.whereArrayContains(any<String>(), any())).thenReturn(mockQuery)

    val task: Task<QuerySnapshot> = mock()
    `when`(mockQuery.get()).thenReturn(task)
    `when`(task.addOnSuccessListener(any())).thenReturn(task)
    `when`(task.addOnFailureListener(any())).thenReturn(task)

    householdRepository.getHouseholds(onSuccess, onFailure)

    // Capture the failure listener and invoke it with an exception
    val failureCaptor =
        ArgumentCaptor.forClass(OnFailureListener::class.java) as ArgumentCaptor<OnFailureListener>
    verify(task).addOnFailureListener(capture(failureCaptor))
    val exception = Exception("Firestore get failed")
    failureCaptor.value.onFailure(exception)

    verify(onFailure).invoke(exception)
    verify(onSuccess, never()).invoke(any())
  }

  @Test
  fun `convertToHousehold returns null when required fields are missing`() {
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("name")).thenReturn(null)

    val result = householdRepository.convertToHousehold(mockDocumentSnapshot)
    assertNull(result)
  }

  @Test
  fun `convertToHousehold returns HouseHold when data is valid`() {
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test Household")
    `when`(mockDocumentSnapshot.get("members")).thenReturn(listOf("testUserId"))
    `when`(mockDocumentSnapshot.get("foodItems")).thenReturn(null)

    val result = householdRepository.convertToHousehold(mockDocumentSnapshot)

    assertNotNull(result)
    assertEquals("1", result?.uid)
    assertEquals("Test Household", result?.name)
    assertEquals(listOf("testUserId"), result?.members)
    assertTrue(result?.foodItems?.isEmpty() == true)
  }

  @Test
  fun `convertToHousehold handles exception and returns null`() {
    `when`(mockDocumentSnapshot.getString("uid")).thenThrow(RuntimeException("Test Exception"))

    val result = householdRepository.convertToHousehold(mockDocumentSnapshot)
    assertNull(result)
  }
}
