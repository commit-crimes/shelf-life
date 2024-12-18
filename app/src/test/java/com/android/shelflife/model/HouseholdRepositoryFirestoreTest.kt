package com.android.shelflife.model

import android.os.Looper
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
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

  @Test
  fun selectHouseholdSetsSelectedHousehold() = runBlocking {
    assertNull("Initially, no household selected", houseHoldRepository.selectedHousehold.value)
    val household =
        HouseHold(
            "house999",
            "Selected Household",
            listOf("userX"),
            listOf("recipeZ"),
            emptyMap(),
            emptyMap())
    houseHoldRepository.selectHousehold(household)
    assertEquals("house999", houseHoldRepository.selectedHousehold.value?.uid)
  }

  @Test
  fun selectHouseholdToEditSetsHouseholdToEdit() = runBlocking {
    assertNull("Initially, no household to edit", houseHoldRepository.householdToEdit.value)
    val household =
        HouseHold(
            "houseEdit",
            "Editing Household",
            listOf("userX"),
            listOf("recipeZ"),
            emptyMap(),
            emptyMap())
    houseHoldRepository.selectHouseholdToEdit(household)
    assertEquals("houseEdit", houseHoldRepository.householdToEdit.value?.uid)
  }

  @Test
  fun `updateStinkyPoints updates selected household`() = runBlocking {
    val household = HouseHold("houseSP", "Name", emptyList(), emptyList(), emptyMap(), emptyMap())
    houseHoldRepository.selectHousehold(household)

    val newStinky = mapOf("userX" to 3L)
    `when`(mockDocument.update("stinkyPoints", newStinky)).thenReturn(Tasks.forResult<Void>(null))

    houseHoldRepository.updateStinkyPoints("houseSP", newStinky)

    assertEquals(newStinky, houseHoldRepository.selectedHousehold.value?.stinkyPoints)
  }

  fun updateStinkyPointsErrorDoesNotChangeSelectedHousehold() = runBlocking {
    // Arrange
    val household =
        HouseHold(
            uid = "houseSP",
            name = "Name",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    // Set initial selected household
    houseHoldRepository.selectHousehold(household)

    val newStinky = mapOf("userX" to 3L)

    // Simulate failure in Firestore update
    `when`(mockCollection.document(household.uid)).thenReturn(mockDocument)
    `when`(mockDocument.update("stinkyPoints", newStinky))
        .thenThrow(RuntimeException("Stinky Error"))

    // Act
    houseHoldRepository.updateStinkyPoints(household.uid, newStinky)

    // Assert
    // Verify that the selected household remains unchanged
    val updatedHousehold = houseHoldRepository.selectedHousehold.value
    assertNotNull(
        "Selected household should not be null",
        updatedHousehold,
    )
    assertTrue(
        "Stinky points should not change on error",
        updatedHousehold?.stinkyPoints?.isEmpty() == true)
  }

  @Test
  fun `updateRatPoints updates selected household`() = runBlocking {
    val household = HouseHold("houseRP", "Name", emptyList(), emptyList(), emptyMap(), emptyMap())
    houseHoldRepository.selectHousehold(household)

    val newRat = mapOf("userY" to 5L)
    `when`(mockDocument.update("ratPoints", newRat)).thenReturn(Tasks.forResult<Void>(null))

    houseHoldRepository.updateRatPoints("houseRP", newRat)

    assertEquals(newRat, houseHoldRepository.selectedHousehold.value?.ratPoints)
  }

  @Test
  fun startListeningForHouseholdsWithEmptyListClearsHouseholds() = runBlocking {
    // Put a household in state first
    val field = HouseholdRepositoryFirestore::class.java.getDeclaredField("_households")
    field.isAccessible = true
    field.set(
        houseHoldRepository,
        kotlinx.coroutines.flow.MutableStateFlow(
            listOf(HouseHold("id1", "test", listOf(), listOf(), emptyMap(), emptyMap()))))

    houseHoldRepository.startListeningForHouseholds(emptyList())
    assertTrue(houseHoldRepository.households.value.isEmpty())
  }

  @Test
  fun initializeHouseholdsCatchesException() = runBlocking {
    val householdIds = listOf("house789")
    val mockQuery = mock(Query::class.java)
    `when`(mockCollection.whereIn(FieldPath.documentId(), householdIds)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenThrow(RuntimeException("Test exception"))

    houseHoldRepository.initializeHouseholds(householdIds, null)
    // Should not crash and presumably keep households empty
    assertTrue(houseHoldRepository.households.value.isEmpty())
    assertNull(houseHoldRepository.selectedHousehold.value)
  }

  @Test
  fun addHouseholdErrorRollsBackChanges() = runBlocking {
    val household =
        HouseHold("houseErr", "Error Household", listOf("u1"), listOf("r1"), emptyMap(), emptyMap())

    `when`(mockCollection.document(household.uid)).thenReturn(mockDocument)
    // Simulate error
    `when`(mockDocument.set(anyMap<String, Any>())).thenThrow(RuntimeException("Test Add Error"))

    val initialSize = houseHoldRepository.households.value.size
    houseHoldRepository.addHousehold(household)

    // After error, no household added
    assertEquals(initialSize, houseHoldRepository.households.value.size)
  }

  @Test
  fun getHouseholdMembersReturnsCorrectMembers() = runBlocking {
    val household =
        HouseHold("houseM", "Name", listOf("member1", "member2"), listOf(), emptyMap(), emptyMap())
    val fieldH = HouseholdRepositoryFirestore::class.java.getDeclaredField("_households")
    fieldH.isAccessible = true
    fieldH.set(houseHoldRepository, kotlinx.coroutines.flow.MutableStateFlow(listOf(household)))

    val members = houseHoldRepository.getHouseholdMembers("houseM")
    assertEquals(listOf("member1", "member2"), members)
  }

  @Test
  fun getHouseholdMembersReturnsEmptyIfNotFound() = runBlocking {
    val fieldH = HouseholdRepositoryFirestore::class.java.getDeclaredField("_households")
    fieldH.isAccessible = true
    fieldH.set(
        houseHoldRepository, kotlinx.coroutines.flow.MutableStateFlow(emptyList<HouseHold>()))

    val members = houseHoldRepository.getHouseholdMembers("nope")
    assertTrue(members.isEmpty())
  }

  @Test
  fun getHouseholdNewUidReturnsNewUid() {
    val newUid = houseHoldRepository.getNewUid()
    assertNotNull(newUid)
  }

  @Test
  fun `addHousehold rolls back changes on Firestore error`(): Unit = runBlocking {
    // Arrange
    val household =
        HouseHold(
            uid = "houseError",
            name = "Test Error Household",
            members = listOf("user1"),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    val field = HouseholdRepositoryFirestore::class.java.getDeclaredField("_households")
    field.isAccessible = true
    field.set(houseHoldRepository, MutableStateFlow(emptyList<HouseHold>()))

    `when`(mockCollection.document(household.uid)).thenReturn(mockDocument)
    `when`(mockDocument.set(anyMap<String, Any>()))
        .thenThrow(RuntimeException("Test Firestore Error"))

    val initialHouseholds = houseHoldRepository.households.value

    // Act
    houseHoldRepository.addHousehold(household)

    // Assert
    val updatedHouseholds = houseHoldRepository.households.value
    assertEquals(
        "The household list should remain unchanged after rollback",
        initialHouseholds,
        updatedHouseholds)
    verify(mockDocument).set(anyMap<String, Any>())
  }

  @Test
  fun `updateHousehold restores original state on Firestore error`(): Unit = runBlocking {
    // Arrange
    val originalHousehold =
        HouseHold(
            uid = "house123",
            name = "Original Household",
            members = listOf("user1"),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    houseHoldRepository.addHousehold(originalHousehold)
    val updatedHousehold = originalHousehold.copy(name = "Updated Household")

    val field = HouseholdRepositoryFirestore::class.java.getDeclaredField("_households")
    field.isAccessible = true
    field.set(houseHoldRepository, MutableStateFlow(listOf(originalHousehold)))

    `when`(mockCollection.document(updatedHousehold.uid)).thenReturn(mockDocument)
    `when`(mockDocument.set(anyMap<String, Any>()))
        .thenThrow(RuntimeException("Test Firestore Error"))

    // Act
    houseHoldRepository.updateHousehold(updatedHousehold)

    // Assert
    val households = houseHoldRepository.households.value
    assertEquals(
        "The original household should be restored after rollback",
        originalHousehold,
        households.first())
  }

  @Test
  fun `deleteHouseholdById restores deleted household on Firestore error`(): Unit = runBlocking {
    // Arrange
    val householdToDelete =
        HouseHold(
            uid = "houseToDelete",
            name = "Household to Delete",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    houseHoldRepository.addHousehold(householdToDelete)
    val field = HouseholdRepositoryFirestore::class.java.getDeclaredField("_households")
    field.isAccessible = true
    field.set(houseHoldRepository, MutableStateFlow(listOf(householdToDelete)))

    `when`(mockCollection.document(householdToDelete.uid)).thenReturn(mockDocument)
    `when`(mockDocument.delete()).thenThrow(RuntimeException("Test Firestore Error"))

    // Act
    houseHoldRepository.deleteHouseholdById(householdToDelete.uid)

    // Assert
    val households = houseHoldRepository.households.value
    assertEquals(
        "The deleted household should be restored after rollback",
        householdToDelete,
        households.first())
    verify(mockDocument).delete()
  }

  @Test
  fun `initializeHouseholds handles Firestore error gracefully`(): Unit = runBlocking {
    // Arrange
    val householdIds = listOf("house123", "house456")
    val mockQuery = mock(Query::class.java)

    `when`(mockCollection.whereIn(FieldPath.documentId(), householdIds)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenThrow(RuntimeException("Test Initialization Error"))

    // Act
    houseHoldRepository.initializeHouseholds(householdIds, null)

    // Assert
    val households = houseHoldRepository.households.value
    assertTrue("Households should remain empty after error", households.isEmpty())
    assertNull(
        "Selected household should remain null after error",
        houseHoldRepository.selectedHousehold.value)
  }
}
