package com.android.shelflife.model.household

import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepositoryFirestore
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class HouseHoldRepositoryFirestoreTest {

    @Mock private lateinit var db: FirebaseFirestore
    @Mock private lateinit var auth: FirebaseAuth
    @Mock private lateinit var currentUser: FirebaseUser
    @Mock private lateinit var householdRepository: HouseHoldRepositoryFirestore
    @Mock private lateinit var foodItemRepository: FoodItemRepositoryFirestore

    @Before
    fun setUp() {
        // Mock FirebaseFirestore and its methods
        db = mockk(relaxed = true)

        // Mock FirebaseAuth and its methods
        auth = mockk(relaxed = true)
        currentUser = mockk(relaxed = true)

        // Mock FirebaseAuth.getInstance() to return our mocked auth
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns auth

        // Set up currentUser in auth
        every { auth.currentUser } returns currentUser
        every { currentUser.uid } returns "testUserId"

        // Mock FoodItemRepositoryFirestore
        foodItemRepository = mockk(relaxed = true)
        mockkConstructor(FoodItemRepositoryFirestore::class)
        every { anyConstructed<FoodItemRepositoryFirestore>().convertFoodItemToMap(any()) } returns emptyMap()
        every { anyConstructed<FoodItemRepositoryFirestore>().convertToFoodItemFromMap(any()) } returns null

        // Initialize the repository with the mocked db
        householdRepository = HouseHoldRepositoryFirestore(db)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test getNewUid returns new unique ID`() {
        val collectionReference: CollectionReference = mockk()
        val documentReference: DocumentReference = mockk()

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.document() } returns documentReference
        every { documentReference.id } returns "newUniqueId"

        val newUid = householdRepository.getNewUid()

        assertEquals("newUniqueId", newUid)
    }

    @Test
    fun `test getHouseholds when user not logged in`() {
        every { auth.currentUser } returns null

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        householdRepository.getHouseholds(
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = { exception ->
                onFailureCalled = true
                failureException = exception
            }
        )

        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertNotNull(failureException)
        assertEquals("User not logged in", failureException?.message)
    }

    @Test
    fun `test getHouseholds success`() {
        val collectionReference: CollectionReference = mockk()
        val query: Query = mockk()
        val querySnapshot: QuerySnapshot = mockk()
        val documentSnapshot: DocumentSnapshot = mockk()

        val household = HouseHold(
            uid = "householdId",
            name = "Test Household",
            members = listOf("testUserId"),
            foodItems = emptyList()
        )

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.whereArrayContains("members", "testUserId") } returns query

        val getTask: Task<QuerySnapshot> = mockk()
        every { query.get() } returns getTask

        val successListenerSlot = slot<OnSuccessListener<QuerySnapshot>>()
        val failureListenerSlot = slot<OnFailureListener>()

        every { getTask.addOnSuccessListener(capture(successListenerSlot)) } returns getTask
        every { getTask.addOnFailureListener(capture(failureListenerSlot)) } returns getTask

        every { querySnapshot.documents } returns listOf(documentSnapshot)
        every { documentSnapshot.getString("uid") } returns "householdId"
        every { documentSnapshot.getString("name") } returns "Test Household"
        every { documentSnapshot.get("members") } returns listOf("testUserId")
        every { documentSnapshot.get("foodItems") } returns emptyList<Map<String, Any>>()

        var onSuccessCalled = false
        var onFailureCalled = false
        var householdsResult: List<HouseHold>? = null

        householdRepository.getHouseholds(
            onSuccess = { households ->
                onSuccessCalled = true
                householdsResult = households
            },
            onFailure = { _ ->
                onFailureCalled = true
            }
        )

        // Simulate success callback
        successListenerSlot.captured.onSuccess(querySnapshot)

        assert(onSuccessCalled)
        assert(!onFailureCalled)
        assertNotNull(householdsResult)
        assertEquals(1, householdsResult?.size)
        assertEquals(household.uid, householdsResult?.get(0)?.uid)
        assertEquals(household.name, householdsResult?.get(0)?.name)
        assertEquals(household.members, householdsResult?.get(0)?.members)
    }

    @Test
    fun `test getHouseholds failure`() {
        val collectionReference: CollectionReference = mockk()
        val query: Query = mockk()
        val exception = Exception("Firestore error")

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.whereArrayContains("members", "testUserId") } returns query

        val getTask: Task<QuerySnapshot> = mockk()
        every { query.get() } returns getTask

        val successListenerSlot = slot<OnSuccessListener<QuerySnapshot>>()
        val failureListenerSlot = slot<OnFailureListener>()

        every { getTask.addOnSuccessListener(capture(successListenerSlot)) } returns getTask
        every { getTask.addOnFailureListener(capture(failureListenerSlot)) } returns getTask

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        householdRepository.getHouseholds(
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = { e ->
                onFailureCalled = true
                failureException = e
            }
        )

        // Simulate failure callback
        failureListenerSlot.captured.onFailure(exception)

        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertEquals(exception, failureException)
    }

    @Test
    fun `test addHousehold when user not logged in`() {
        every { auth.currentUser } returns null

        val household = HouseHold(
            uid = "householdId",
            name = "Test Household",
            members = emptyList(),
            foodItems = emptyList()
        )

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        householdRepository.addHousehold(
            household,
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = { exception ->
                onFailureCalled = true
                failureException = exception
            }
        )

        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertNotNull(failureException)
        assertEquals("User not logged in", failureException?.message)
    }

    @Test
    fun `test addHousehold success`() {
        val household = HouseHold(
            uid = "householdId",
            name = "Test Household",
            members = emptyList(),
            foodItems = emptyList()
        )

        val collectionReference: CollectionReference = mockk()
        val documentReference: DocumentReference = mockk()
        val setTask: Task<Void> = mockk()

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.document("householdId") } returns documentReference
        every { documentReference.set(any()) } returns setTask

        val successListenerSlot = slot<OnSuccessListener<Void>>()
        val failureListenerSlot = slot<OnFailureListener>()

        every { setTask.addOnSuccessListener(capture(successListenerSlot)) } returns setTask
        every { setTask.addOnFailureListener(capture(failureListenerSlot)) } returns setTask

        var onSuccessCalled = false
        var onFailureCalled = false

        householdRepository.addHousehold(
            household,
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = {
                onFailureCalled = true
            }
        )

        // Simulate success callback
        successListenerSlot.captured.onSuccess(null)

        assert(onSuccessCalled)
        assert(!onFailureCalled)
    }

    @Test
    fun `test addHousehold failure`() {
        val household = HouseHold(
            uid = "householdId",
            name = "Test Household",
            members = emptyList(),
            foodItems = emptyList()
        )

        val collectionReference: CollectionReference = mockk()
        val documentReference: DocumentReference = mockk()
        val setTask: Task<Void> = mockk()
        val exception = Exception("Firestore error")

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.document("householdId") } returns documentReference
        every { documentReference.set(any()) } returns setTask

        val successListenerSlot = slot<OnSuccessListener<Void>>()
        val failureListenerSlot = slot<OnFailureListener>()

        every { setTask.addOnSuccessListener(capture(successListenerSlot)) } returns setTask
        every { setTask.addOnFailureListener(capture(failureListenerSlot)) } returns setTask

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        householdRepository.addHousehold(
            household,
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = { e ->
                onFailureCalled = true
                failureException = e
            }
        )

        // Simulate failure callback
        failureListenerSlot.captured.onFailure(exception)

        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertEquals(exception, failureException)
    }

    @Test
    fun `test updateHousehold when user not logged in`() {
        every { auth.currentUser } returns null

        val household = HouseHold(
            uid = "householdId",
            name = "Test Household",
            members = emptyList(),
            foodItems = emptyList()
        )

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        householdRepository.updateHousehold(
            household,
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = { exception ->
                onFailureCalled = true
                failureException = exception
            }
        )

        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertNotNull(failureException)
        assertEquals("User not logged in", failureException?.message)
    }

    @Test
    fun `test updateHousehold success`() {
        val household = HouseHold(
            uid = "householdId",
            name = "Updated Household",
            members = listOf("testUserId"),
            foodItems = emptyList()
        )

        val collectionReference: CollectionReference = mockk()
        val documentReference: DocumentReference = mockk()
        val updateTask: Task<Void> = mockk()

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.document("householdId") } returns documentReference
        every { documentReference.update(any<Map<String, Any>>()) } returns updateTask

        val successListenerSlot = slot<OnSuccessListener<Void>>()
        val failureListenerSlot = slot<OnFailureListener>()

        every { updateTask.addOnSuccessListener(capture(successListenerSlot)) } returns updateTask
        every { updateTask.addOnFailureListener(capture(failureListenerSlot)) } returns updateTask

        var onSuccessCalled = false
        var onFailureCalled = false

        householdRepository.updateHousehold(
            household,
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = {
                onFailureCalled = true
            }
        )

        // Simulate success callback
        successListenerSlot.captured.onSuccess(null)

        assert(onSuccessCalled)
        assert(!onFailureCalled)
    }

    @Test
    fun `test updateHousehold failure`() {
        val household = HouseHold(
            uid = "householdId",
            name = "Updated Household",
            members = listOf("testUserId"),
            foodItems = emptyList()
        )

        val collectionReference: CollectionReference = mockk()
        val documentReference: DocumentReference = mockk()
        val updateTask: Task<Void> = mockk()
        val exception = Exception("Firestore error")

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.document("householdId") } returns documentReference
        every { documentReference.update(any<Map<String, Any>>()) } returns updateTask

        val successListenerSlot = slot<OnSuccessListener<Void>>()
        val failureListenerSlot = slot<OnFailureListener>()

        every { updateTask.addOnSuccessListener(capture(successListenerSlot)) } returns updateTask
        every { updateTask.addOnFailureListener(capture(failureListenerSlot)) } returns updateTask

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        householdRepository.updateHousehold(
            household,
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = { e ->
                onFailureCalled = true
                failureException = e
            }
        )

        // Simulate failure callback
        failureListenerSlot.captured.onFailure(exception)

        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertEquals(exception, failureException)
    }

    @Test
    fun `test deleteHouseholdById when user not logged in`() {
        every { auth.currentUser } returns null

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        householdRepository.deleteHouseholdById(
            id = "householdId",
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = { exception ->
                onFailureCalled = true
                failureException = exception
            }
        )

        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertNotNull(failureException)
        assertEquals("User not logged in", failureException?.message)
    }

    @Test
    fun `test deleteHouseholdById success`() {
        val collectionReference: CollectionReference = mockk()
        val documentReference: DocumentReference = mockk()
        val deleteTask: Task<Void> = mockk()

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.document("householdId") } returns documentReference
        every { documentReference.delete() } returns deleteTask

        val successListenerSlot = slot<OnSuccessListener<Void>>()
        val failureListenerSlot = slot<OnFailureListener>()

        every { deleteTask.addOnSuccessListener(capture(successListenerSlot)) } returns deleteTask
        every { deleteTask.addOnFailureListener(capture(failureListenerSlot)) } returns deleteTask

        var onSuccessCalled = false
        var onFailureCalled = false

        householdRepository.deleteHouseholdById(
            id = "householdId",
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = {
                onFailureCalled = true
            }
        )

        // Simulate success callback
        successListenerSlot.captured.onSuccess(null)

        assert(onSuccessCalled)
        assert(!onFailureCalled)
    }

    @Test
    fun `test deleteHouseholdById failure`() {
        val collectionReference: CollectionReference = mockk()
        val documentReference: DocumentReference = mockk()
        val deleteTask: Task<Void> = mockk()
        val exception = Exception("Firestore error")

        every { db.collection(any()) } returns collectionReference
        every { collectionReference.document("householdId") } returns documentReference
        every { documentReference.delete() } returns deleteTask

        val successListenerSlot = slot<OnSuccessListener<Void>>()
        val failureListenerSlot = slot<OnFailureListener>()

        every { deleteTask.addOnSuccessListener(capture(successListenerSlot)) } returns deleteTask
        every { deleteTask.addOnFailureListener(capture(failureListenerSlot)) } returns deleteTask

        var onSuccessCalled = false
        var onFailureCalled = false
        var failureException: Exception? = null

        householdRepository.deleteHouseholdById(
            id = "householdId",
            onSuccess = {
                onSuccessCalled = true
            },
            onFailure = { e ->
                onFailureCalled = true
                failureException = e
            }
        )

        // Simulate failure callback
        failureListenerSlot.captured.onFailure(exception)

        assert(!onSuccessCalled)
        assert(onFailureCalled)
        assertEquals(exception, failureException)
    }
}