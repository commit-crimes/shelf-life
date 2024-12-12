package com.android.shelflife.model

import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseholdRepositoryFirestore
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class HouseholdRepositoryFirestoreTest {

    @get:Rule var hiltRule = HiltAndroidRule(this)

    @Inject lateinit var houseHoldRepository: HouseholdRepositoryFirestore

    @Inject lateinit var mockFirestore: FirebaseFirestore

    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference

    @Before
    fun setUp() {
        hiltRule.inject()

        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)

        `when`(mockFirestore.collection("households")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
    }

    @Test
    fun addHouseholdAddsHouseholdToFirestore():  Unit = runBlocking {
        // Arrange
        val household =
            HouseHold(
                uid = "house123",
                name = "My Household",
                members = listOf("user1"),
                sharedRecipes = listOf("recipe1", "recipe2"))
        val expectedMap =
            mapOf(
                "name" to household.name,
                "members" to household.members,
                "sharedRecipes" to household.sharedRecipes)

        // Act
        houseHoldRepository.addHousehold(household)

        // Assert
        verify(mockCollection).document(household.uid)
        verify(mockDocument).set(eq(expectedMap))
    }

    @Test
    fun deleteHouseholdRemovesHouseholdFromFirestore(): Unit = runBlocking {
        val householdId = "house123"

        houseHoldRepository.deleteHouseholdById(householdId)

        verify(mockCollection).document(householdId)
        verify(mockDocument).delete()
    }

    @Test
    fun updateHouseholdUpdatesAnExistingHouseholdInFirestore(): Unit = runBlocking {
        // Arrange
        val household =
            HouseHold(
                uid = "house123",
                name = "Updated Household",
                members = listOf("user1"),
                sharedRecipes = listOf("recipe1"))
        val expectedMap =
            mapOf(
                "name" to household.name,
                "members" to household.members,
                "sharedRecipes" to household.sharedRecipes)

        // Act
        houseHoldRepository.updateHousehold(household)

        // Assert
        verify(mockCollection).document(household.uid)
        verify(mockDocument).set(eq(expectedMap))
    }

    @Test
    fun startListeningForHouseholdsSetsUpFirestoreListener() : Unit = runBlocking {
        val mockCollectionReference = mock(CollectionReference::class.java)
        val mockListenerRegistration = mock(ListenerRegistration::class.java)
        val mockQuery = mock(Query::class.java)

        `when`(mockFirestore.collection("households")).thenReturn(mockCollectionReference)
        `when`(mockCollectionReference.whereIn(eq(FieldPath.documentId()), anyList()))
            .thenReturn(mockQuery)
        `when`(mockQuery.addSnapshotListener(any())).thenReturn(mockListenerRegistration)

        houseHoldRepository.startListeningForHouseholds(listOf("house123"))

        verify(mockQuery).addSnapshotListener(any())
    }

    @Test
    fun stopListeningForHouseholdsRemovesFirestoreListener() : Unit = runBlocking {
        val mockListenerRegistration = mock(ListenerRegistration::class.java)

        val field =
            HouseholdRepositoryFirestore::class.java.getDeclaredField("householdsListenerRegistration")
        field.isAccessible = true
        field.set(houseHoldRepository, mockListenerRegistration)

        houseHoldRepository.stopListeningForHouseholds()

        verify(mockListenerRegistration).remove()
    }

    @Test
    fun initializeHouseholdsLoadsData() : Unit = runBlocking {
        // Arrange
        // Mock snapshot data to return multiple households
        val mockSnapshot = mock(QuerySnapshot::class.java)
        val mockDoc1 = mock(DocumentSnapshot::class.java)
        val mockDoc2 = mock(DocumentSnapshot::class.java)
        val mockQuery = mock(Query::class.java)

        `when`(mockDoc1.id).thenReturn("house123")
        `when`(mockDoc1.getString("name")).thenReturn("Household 123")
        `when`(mockDoc1.get("members")).thenReturn(listOf("user1", "user2"))
        `when`(mockDoc1.get("sharedRecipes")).thenReturn(emptyList<String>())

        `when`(mockDoc2.id).thenReturn("house456")
        `when`(mockDoc2.getString("name")).thenReturn("Household 456")
        `when`(mockDoc2.get("members")).thenReturn(listOf("user3"))
        `when`(mockDoc2.get("sharedRecipes")).thenReturn(listOf("recipeX"))

        `when`(mockSnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

        val householdIds = listOf("house123", "house456")
        `when`(mockCollection.whereIn(FieldPath.documentId(), householdIds)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockSnapshot))

        // Act
        houseHoldRepository.initializeHouseholds(householdIds, "house456")

        // Assert
        val households = houseHoldRepository.households.value
        assertNotNull("Households should not be null", households)
        assertEquals("Should load 2 households", 2, households.size)
        assertEquals("First household name should match", "Household 123", households[0].name)
        assertEquals("Second household name should match", "Household 456", households[1].name)

        val selected = houseHoldRepository.selectedHousehold.value
        assertNotNull("Selected household should not be null", selected)
        assertEquals("Selected household UID should match", "house456", selected?.uid)
    }

    @Test
    fun checkIfHouseholdNameExistsReturnsTrue() : Unit = runBlocking {
        // Arrange: Put some households into the state flow
        val mockHouseholds = listOf(
            HouseHold(uid = "house123", name = "My Household", members = listOf("user1"), sharedRecipes = listOf("recipe1")),
            HouseHold(uid = "house456", name = "Another Household", members = emptyList(), sharedRecipes = emptyList())
        )

        // Directly update the state since initialization logic is elsewhere
        val field = HouseholdRepositoryFirestore::class.java.getDeclaredField("_households")
        field.isAccessible = true
        field.set(houseHoldRepository, kotlinx.coroutines.flow.MutableStateFlow(mockHouseholds))

        // Act
        val exists = houseHoldRepository.checkIfHouseholdNameExists("My Household")

        // Assert
        assertTrue("Household name 'My Household' should exist", exists)
    }

    @Test
    fun checkIfHouseholdNameExistsReturnsFalse() : Unit = runBlocking {
        // Arrange: State with some households
        val mockHouseholds = listOf(
            HouseHold(uid = "house123", name = "My Household", members = listOf("user1"), sharedRecipes = listOf("recipe1"))
        )
        val field = HouseholdRepositoryFirestore::class.java.getDeclaredField("_households")
        field.isAccessible = true
        field.set(houseHoldRepository, kotlinx.coroutines.flow.MutableStateFlow(mockHouseholds))

        // Act
        val exists = houseHoldRepository.checkIfHouseholdNameExists("Non Existing Household")

        // Assert
        assertFalse("Non existing household name should return false", exists)
    }


    @Test
    fun initializeHouseholdsWithEmptyListSetsEmptyCache() : Unit = runBlocking {
        // Arrange & Act
        houseHoldRepository.initializeHouseholds(emptyList(), null)

        // Assert
        val households = houseHoldRepository.households.value
        assertTrue("Households should be empty when initialized with no IDs", households.isEmpty())
        assertNull("Selected household should be null", houseHoldRepository.selectedHousehold.value)
    }
}
