package com.android.shelflife.model.household

import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.google.android.gms.tasks.OnCompleteListener
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
import org.mockito.kotlin.argumentCaptor
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

    // Capture the OnCompleteListener correctly with ArgumentCaptor
    val successCaptor =
        ArgumentCaptor.forClass(OnCompleteListener::class.java)
            as ArgumentCaptor<OnCompleteListener<Void>>
    `when`(task.addOnCompleteListener(successCaptor.capture())).thenReturn(task)

    // Call the function being tested
    householdRepository.updateHousehold(household, onSuccess, onFailure)

    // Simulate a successful task
    `when`(task.isSuccessful).thenReturn(true)

    // Invoke the captured OnCompleteListener with a successful task
    successCaptor.value.onComplete(task)

    // Verify that onSuccess was called, and onFailure was never invoked
    verify(onSuccess).invoke()
    verify(onFailure, never()).invoke(any())
  }

  @Test
  fun `updateHousehold calls onFailure when Firestore update fails`() {
    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val task: Task<Void> = mock()
    `when`(mockDocumentReference.update(any<Map<String, Any>>())).thenReturn(task)

    // Use ArgumentCaptor to capture the OnCompleteListener
    val listenerCaptor =
        ArgumentCaptor.forClass(OnCompleteListener::class.java)
            as ArgumentCaptor<OnCompleteListener<Void>>

    `when`(task.addOnCompleteListener(listenerCaptor.capture())).thenReturn(task)

    householdRepository.updateHousehold(household, onSuccess, onFailure)

    // Simulate a task failure
    val exception = Exception("Firestore update failed")
    `when`(task.isSuccessful).thenReturn(false)
    `when`(task.exception).thenReturn(exception)

    // Trigger the captured OnCompleteListener with the failed task
    listenerCaptor.value.onComplete(task)

    // Verify that the onFailure callback is called with the exception
    verify(onFailure).invoke(exception)

    // Verify that onSuccess was never invoked
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

  @Test
  fun `getUserIds returns correct mapping when users list is not empty and Firestore queries succeed`() {
    val users = listOf("user1@example.com", "user2@example.com")
    val callback = mock<(Map<String, String>) -> Unit>()

    // Mock Firestore behavior
    val usersCollectionRef: CollectionReference = mock()
    `when`(mockFirestore.collection("users")).thenReturn(usersCollectionRef)

    val query: Query = mock()
    `when`(usersCollectionRef.whereIn(eq("email"), eq(users))).thenReturn(query)

    val taskQuerySnapshot: Task<QuerySnapshot> = mock()
    `when`(query.get()).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

    // Simulate the querySnapshot
    val querySnapshot: QuerySnapshot = mock()
    val docSnapshot1: DocumentSnapshot = mock()
    val docSnapshot2: DocumentSnapshot = mock()
    `when`(docSnapshot1.getString("email")).thenReturn("user1@example.com")
    `when`(docSnapshot1.id).thenReturn("user1Id")
    `when`(docSnapshot2.getString("email")).thenReturn("user2@example.com")
    `when`(docSnapshot2.id).thenReturn("user2Id")
    val docSnapshots = listOf(docSnapshot1, docSnapshot2)
    `when`(querySnapshot.documents).thenReturn(docSnapshots)

    // Invoke getUserIds
    householdRepository.getUserIds(users, callback)

    // Capture and invoke the success listener
    val successCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
    verify(taskQuerySnapshot).addOnSuccessListener(successCaptor.capture())
    successCaptor.firstValue.onSuccess(querySnapshot)

    // Verify the callback is called with the correct mapping
    val expectedMap = mapOf("user1@example.com" to "user1Id", "user2@example.com" to "user2Id")
    verify(callback).invoke(expectedMap)
  }

  @Test
  fun `getUserIds returns correct mapping when users list exceeds batch size and Firestore queries succeed`() {
    val users = (1..15).map { "user$it@example.com" }
    val callback = mock<(Map<String, String>) -> Unit>()

    // Mock Firestore behavior
    val usersCollectionRef: CollectionReference = mock()
    `when`(mockFirestore.collection("users")).thenReturn(usersCollectionRef)

    val batches = users.chunked(10)
    val tasks = mutableListOf<Task<QuerySnapshot>>()

    for (batch in batches) {
      val query: Query = mock()
      val taskQuerySnapshot: Task<QuerySnapshot> = mock()

      `when`(usersCollectionRef.whereIn(eq("email"), eq(batch))).thenReturn(query)
      `when`(query.get()).thenReturn(taskQuerySnapshot)
      `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
      `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

      tasks.add(taskQuerySnapshot)
    }

    // Simulate querySnapshots for each batch
    val querySnapshots =
        batches.map { batch ->
          val querySnapshot: QuerySnapshot = mock()
          val docSnapshots =
              batch.map { email ->
                val docSnapshot: DocumentSnapshot = mock()
                `when`(docSnapshot.getString("email")).thenReturn(email)
                `when`(docSnapshot.id).thenReturn(email.replace("@example.com", "Id"))
                docSnapshot
              }
          `when`(querySnapshot.documents).thenReturn(docSnapshots)
          querySnapshot
        }

    householdRepository.getUserIds(users, callback)

    // Capture and invoke success listeners for each batch
    tasks.zip(querySnapshots).forEach { (task, querySnapshot) ->
      val successCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
      verify(task).addOnSuccessListener(successCaptor.capture())
      successCaptor.firstValue.onSuccess(querySnapshot)
    }

    // Verify the callback is called with the correct mapping
    val expectedMap = users.associateWith { it.replace("@example.com", "Id") }
    verify(callback).invoke(expectedMap)
  }

  @Test
  fun `getUserIds handles Firestore query failure gracefully`() {
    val users = listOf("user1@example.com", "user2@example.com")
    val callback = mock<(Map<String, String>) -> Unit>()

    // Mock Firestore behavior
    val usersCollectionRef: CollectionReference = mock()
    `when`(mockFirestore.collection("users")).thenReturn(usersCollectionRef)

    val query: Query = mock()
    `when`(usersCollectionRef.whereIn(eq("email"), eq(users))).thenReturn(query)

    val taskQuerySnapshot: Task<QuerySnapshot> = mock()
    `when`(query.get()).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

    // Invoke getUserIds
    householdRepository.getUserIds(users, callback)

    // Capture and invoke the failure listener
    val failureCaptor = argumentCaptor<OnFailureListener>()
    verify(taskQuerySnapshot).addOnFailureListener(failureCaptor.capture())
    val exception = Exception("Firestore query failed")
    failureCaptor.firstValue.onFailure(exception)

    // Verify the callback is called with an empty mapping due to failure
    verify(callback).invoke(emptyMap())
  }

  @Test
  fun `getUserIds processes all batches even if some fail`() {
    val users = (1..20).map { "user$it@example.com" }
    val callback = mock<(Map<String, String>) -> Unit>()

    // Mock Firestore behavior
    val usersCollectionRef: CollectionReference = mock()
    `when`(mockFirestore.collection("users")).thenReturn(usersCollectionRef)

    val batches = users.chunked(10)
    val tasks = mutableListOf<Task<QuerySnapshot>>()

    batches.forEachIndexed { index, batch ->
      val query: Query = mock()
      val taskQuerySnapshot: Task<QuerySnapshot> = mock()

      `when`(usersCollectionRef.whereIn(eq("email"), eq(batch))).thenReturn(query)
      `when`(query.get()).thenReturn(taskQuerySnapshot)
      `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
      `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

      tasks.add(taskQuerySnapshot)
    }

    // Simulate querySnapshots, first batch succeeds, second fails
    val querySnapshotSuccess: QuerySnapshot = mock()
    val docSnapshotsSuccess =
        batches[0].map { email ->
          val docSnapshot: DocumentSnapshot = mock()
          `when`(docSnapshot.getString("email")).thenReturn(email)
          `when`(docSnapshot.id).thenReturn(email.replace("@example.com", "Id"))
          docSnapshot
        }
    `when`(querySnapshotSuccess.documents).thenReturn(docSnapshotsSuccess)

    householdRepository.getUserIds(users, callback)

    // Capture and invoke success/failure listeners
    tasks.forEachIndexed { index, task ->
      val successCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
      val failureCaptor = argumentCaptor<OnFailureListener>()
      verify(task).addOnSuccessListener(successCaptor.capture())
      verify(task).addOnFailureListener(failureCaptor.capture())

      if (index == 0) {
        successCaptor.firstValue.onSuccess(querySnapshotSuccess)
      } else {
        val exception = Exception("Firestore query failed")
        failureCaptor.firstValue.onFailure(exception)
      }
    }

    // Verify the callback is called with mapping from the successful batch
    val expectedMap = batches[0].associateWith { it.replace("@example.com", "Id") }
    verify(callback).invoke(expectedMap)
  }

  @Test
  fun `getUserEmails calls callback with empty map when userIds is empty`() {
    val callback = mock<(Map<String, String>) -> Unit>()

    // Invoke getUserEmails with an empty list
    householdRepository.getUserEmails(emptyList(), callback)

    // Verify that the callback is called with an empty map
    verify(callback).invoke(emptyMap())
  }

  @Test
  fun `getUserEmails retrieves emails when userIds list is less than batch size and Firestore query succeeds`() {
    val userIds = listOf("user1Id", "user2Id")
    val callback = mock<(Map<String, String>) -> Unit>()

    // Mock Firestore behavior
    val usersCollectionRef: CollectionReference = mock()
    `when`(mockFirestore.collection("users")).thenReturn(usersCollectionRef)

    val query: Query = mock()
    `when`(usersCollectionRef.whereIn(eq(FieldPath.documentId()), eq(userIds))).thenReturn(query)

    val taskQuerySnapshot: Task<QuerySnapshot> = mock()
    `when`(query.get()).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

    // Simulate the querySnapshot
    val querySnapshot: QuerySnapshot = mock()
    val docSnapshot1: DocumentSnapshot = mock()
    val docSnapshot2: DocumentSnapshot = mock()
    `when`(docSnapshot1.getString("email")).thenReturn("user1@example.com")
    `when`(docSnapshot1.id).thenReturn("user1Id")
    `when`(docSnapshot2.getString("email")).thenReturn("user2@example.com")
    `when`(docSnapshot2.id).thenReturn("user2Id")
    val docSnapshots = listOf(docSnapshot1, docSnapshot2)
    `when`(querySnapshot.documents).thenReturn(docSnapshots)

    // Invoke getUserEmails
    householdRepository.getUserEmails(userIds, callback)

    // Capture and invoke the success listener
    val successCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
    verify(taskQuerySnapshot).addOnSuccessListener(successCaptor.capture())
    successCaptor.firstValue.onSuccess(querySnapshot)

    // Verify the callback is called with the correct mapping
    val expectedMap = mapOf("user1Id" to "user1@example.com", "user2Id" to "user2@example.com")
    verify(callback).invoke(expectedMap)
  }

  @Test
  fun `getUserEmails retrieves emails when userIds list exceeds batch size and Firestore queries succeed`() {
    val userIds = (1..15).map { "user${it}Id" }
    val callback = mock<(Map<String, String>) -> Unit>()

    // Mock Firestore behavior
    val usersCollectionRef: CollectionReference = mock()
    `when`(mockFirestore.collection("users")).thenReturn(usersCollectionRef)

    val batches = userIds.chunked(10)
    val tasks = mutableListOf<Task<QuerySnapshot>>()

    for (batch in batches) {
      val query: Query = mock()
      val taskQuerySnapshot: Task<QuerySnapshot> = mock()

      `when`(usersCollectionRef.whereIn(eq(FieldPath.documentId()), eq(batch))).thenReturn(query)
      `when`(query.get()).thenReturn(taskQuerySnapshot)
      `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
      `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

      tasks.add(taskQuerySnapshot)
    }

    // Simulate querySnapshots for each batch
    val querySnapshots =
        batches.map { batch ->
          val querySnapshot: QuerySnapshot = mock()
          val docSnapshots =
              batch.map { userId ->
                val docSnapshot: DocumentSnapshot = mock()
                `when`(docSnapshot.getString("email")).thenReturn("$userId@example.com")
                `when`(docSnapshot.id).thenReturn(userId)
                docSnapshot
              }
          `when`(querySnapshot.documents).thenReturn(docSnapshots)
          querySnapshot
        }

    // Invoke getUserEmails
    householdRepository.getUserEmails(userIds, callback)

    // Capture and invoke success listeners for each batch
    tasks.zip(querySnapshots).forEach { (task, querySnapshot) ->
      val successCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
      verify(task).addOnSuccessListener(successCaptor.capture())
      successCaptor.firstValue.onSuccess(querySnapshot)
    }

    // Verify the callback is called with the correct mapping
    val expectedMap = userIds.associateWith { "$it@example.com" }
    verify(callback).invoke(expectedMap)
  }

  @Test
  fun `getUserEmails handles Firestore query failure and calls callback with empty map`() {
    val userIds = listOf("user1Id", "user2Id")
    val callback = mock<(Map<String, String>) -> Unit>()

    // Mock Firestore behavior
    val usersCollectionRef: CollectionReference = mock()
    `when`(mockFirestore.collection("users")).thenReturn(usersCollectionRef)

    val query: Query = mock()
    `when`(usersCollectionRef.whereIn(eq(FieldPath.documentId()), eq(userIds))).thenReturn(query)

    val taskQuerySnapshot: Task<QuerySnapshot> = mock()
    `when`(query.get()).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
    `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

    // Invoke getUserEmails
    householdRepository.getUserEmails(userIds, callback)

    // Capture and invoke the failure listener
    val failureCaptor = argumentCaptor<OnFailureListener>()
    verify(taskQuerySnapshot).addOnFailureListener(failureCaptor.capture())
    val exception = Exception("Firestore query failed")
    failureCaptor.firstValue.onFailure(exception)

    // Verify the callback is called with an empty map due to failure
    verify(callback).invoke(emptyMap())
  }

  @Test
  fun `getUserEmails processes all batches even if some fail`() {
    val userIds = (1..20).map { "user${it}Id" }
    val callback = mock<(Map<String, String>) -> Unit>()

    // Mock Firestore behavior
    val usersCollectionRef: CollectionReference = mock()
    `when`(mockFirestore.collection("users")).thenReturn(usersCollectionRef)

    val batches = userIds.chunked(10)
    val tasks = mutableListOf<Task<QuerySnapshot>>()

    batches.forEachIndexed { index, batch ->
      val query: Query = mock()
      val taskQuerySnapshot: Task<QuerySnapshot> = mock()

      `when`(usersCollectionRef.whereIn(eq(FieldPath.documentId()), eq(batch))).thenReturn(query)
      `when`(query.get()).thenReturn(taskQuerySnapshot)
      `when`(taskQuerySnapshot.addOnSuccessListener(any())).thenReturn(taskQuerySnapshot)
      `when`(taskQuerySnapshot.addOnFailureListener(any())).thenReturn(taskQuerySnapshot)

      tasks.add(taskQuerySnapshot)
    }

    // Simulate querySnapshots, first batch succeeds, second fails
    val querySnapshotSuccess: QuerySnapshot = mock()
    val docSnapshotsSuccess =
        batches[0].map { userId ->
          val docSnapshot: DocumentSnapshot = mock()
          `when`(docSnapshot.getString("email")).thenReturn("$userId@example.com")
          `when`(docSnapshot.id).thenReturn(userId)
          docSnapshot
        }
    `when`(querySnapshotSuccess.documents).thenReturn(docSnapshotsSuccess)

    // Invoke getUserEmails
    householdRepository.getUserEmails(userIds, callback)

    // Capture and invoke success/failure listeners
    tasks.forEachIndexed { index, task ->
      val successCaptor = argumentCaptor<OnSuccessListener<QuerySnapshot>>()
      val failureCaptor = argumentCaptor<OnFailureListener>()
      verify(task).addOnSuccessListener(successCaptor.capture())
      verify(task).addOnFailureListener(failureCaptor.capture())

      if (index == 0) {
        successCaptor.firstValue.onSuccess(querySnapshotSuccess)
      } else {
        val exception = Exception("Firestore query failed")
        failureCaptor.firstValue.onFailure(exception)
      }
    }

    // Verify the callback is called with mapping from the successful batch
    val expectedMap = batches[0].associateWith { "$it@example.com" }
    verify(callback).invoke(expectedMap)
  }
}