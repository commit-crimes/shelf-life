package com.android.shelflife.model

import android.os.Looper
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class HouseholdRepositoryFirestoreTest {

    @get:Rule var hiltRule = HiltAndroidRule(this)

    private lateinit var householdRepository: HouseholdRepositoryFirestore
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference

    @Before
    fun setUp() {
        hiltRule.inject()
        mockFirestore = mock(FirebaseFirestore::class.java)
        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)

        householdRepository = HouseholdRepositoryFirestore(mockFirestore)

        // Mock Firestore collection and document
        `when`(mockFirestore.collection(anyString())).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)

        // Mock Firestore document methods to return a valid task
        `when`(mockDocument.set(any())).thenReturn(Tasks.forResult(null))
        `when`(mockDocument.delete()).thenReturn(Tasks.forResult(null))
        `when`(mockDocument.update(anyMap<String, Any>())).thenReturn(Tasks.forResult(null))
    }

    @Test
    fun getNewUidGeneratesUniqueId() {
        // Arrange
        `when`(mockCollection.document()).thenReturn(mockDocument)
        `when`(mockDocument.id).thenReturn("uniqueId123")

        // Act
        val newUid = householdRepository.getNewUid()

        // Assert
        assertEquals("uniqueId123", newUid)
    }

    @Test
    fun selectHouseholdUpdatesSelectedState() {
        // Arrange
        val household =
            HouseHold("id1", "Test Household", emptyList(), emptyList(), emptyMap(), emptyMap())

        // Act
        householdRepository.selectHousehold(household)

        // Assert
        runBlocking { assertEquals(household, householdRepository.selectedHousehold.first()) }
    }

    @Test
    fun addHouseholdHandlesFirestoreErrorGracefully() {
        runBlocking {
            // Arrange
            val household =
                HouseHold("id1", "Test Household", emptyList(), emptyList(), emptyMap(), emptyMap())
            val failureTask = Tasks.forException<Void>(Exception("Firestore error"))
            `when`(mockDocument.set(any())).thenReturn(failureTask)

            // Act
            householdRepository.addHousehold(household)
            shadowOf(Looper.getMainLooper()).idle()

            // Assert
            val cachedHouseholds = householdRepository.households.first()
            assertFalse(cachedHouseholds.contains(household))
        }
    }

    @Test
    fun deleteHouseholdByIdRemovesHousehold() {
        // Arrange
        val householdId = "id1"
        val household =
            HouseHold(householdId, "Test Household", emptyList(), emptyList(), emptyMap(), emptyMap())
        householdRepository.addHousehold(household)

        `when`(mockDocument.delete()).thenReturn(Tasks.forResult(null))

        // Act
        householdRepository.deleteHouseholdById(householdId) {}
        shadowOf(Looper.getMainLooper()).idle()

        // Assert
        runBlocking {
            val cachedHouseholds = householdRepository.households.first()
            assertFalse(cachedHouseholds.any { it.uid == householdId })
        }
    }

    @Test
    fun initializeHouseholdsHandlesEmptyList() = runBlocking {
        // Act
        householdRepository.initializeHouseholds(emptyList(), null)

        // Assert
        val households = householdRepository.households.first()
        assertTrue(households.isEmpty())
        assertNull(householdRepository.selectedHousehold.first())
    }

    @Test
    fun checkIfHouseholdNameExistsReturnsCorrectValue() {
        // Arrange
        val household =
            HouseHold("id1", "Test Household", emptyList(), emptyList(), emptyMap(), emptyMap())
        householdRepository.addHousehold(household)

        // Act
        val exists = householdRepository.checkIfHouseholdNameExists("Test Household")
        val doesNotExist = householdRepository.checkIfHouseholdNameExists("Nonexistent Household")

        // Assert
        assertTrue(exists)
        assertFalse(doesNotExist)
    }

    @Test
    fun updateHouseholdUpdatesFirestoreAndCache() {
        // Arrange
        val household =
            HouseHold("id1", "Test Household", emptyList(), emptyList(), emptyMap(), emptyMap())
        householdRepository.addHousehold(household)
        val updatedHousehold = household.copy(name = "Updated Name")

        `when`(mockDocument.set(any())).thenReturn(Tasks.forResult(null))

        // Act
        householdRepository.updateHousehold(updatedHousehold) {}
        shadowOf(Looper.getMainLooper()).idle()

        // Assert
        runBlocking {
            val cachedHouseholds = householdRepository.households.first()
            assertTrue(cachedHouseholds.any { it.name == "Updated Name" })
        }
    }

    @Test
    fun startListeningForHouseholdsSetsUpFirestoreListener() {
        // Arrange
        val listenerRegistration = mock(ListenerRegistration::class.java)
        val mockQuery = mock(Query::class.java)

        // Mock the behavior of whereIn and addSnapshotListener
        `when`(mockCollection.whereIn(any(FieldPath::class.java), anyList())).thenReturn(mockQuery)
        `when`(mockQuery.addSnapshotListener(any<EventListener<QuerySnapshot>>()))
            .thenReturn(listenerRegistration)

        // Act
        householdRepository.startListeningForHouseholds(listOf("id1", "id2"))

        // Assert
        verify(mockQuery).addSnapshotListener(any<EventListener<QuerySnapshot>>())
    }

    @Test
    fun stopListeningForHouseholdsRemovesListener() {
        // Arrange
        val listenerRegistration = mock(ListenerRegistration::class.java)
        householdRepository.stopListeningForHouseholds()

        // Assert
        verify(listenerRegistration, never()).remove()
    }
}