package com.android.shelflife.model.household

import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseholdRepositoryFirestore
import com.google.firebase.firestore.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

@HiltAndroidTest
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
  fun `addHousehold adds household to Firestore`(): Unit = runBlocking {
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
  fun `deleteHousehold removes household from Firestore`(): Unit = runBlocking {
    val householdId = "house123"

    houseHoldRepository.deleteHouseholdById(householdId)

    verify(mockCollection).document(householdId)
    verify(mockDocument).delete()
  }

  @Test
  fun `getHouseholds fetches households from Firestore`(): Unit = runBlocking {
    // Arrange
    val householdIds = listOf("house123", "house456")
    val mockSnapshot = mock(QuerySnapshot::class.java)
    val mockDocument1 = mock(DocumentSnapshot::class.java)
    val mockDocument2 = mock(DocumentSnapshot::class.java)

    `when`(mockDocument1.id).thenReturn("house123")
    `when`(mockDocument1.getString("name")).thenReturn("Household 1")
    `when`(mockDocument1.get("members")).thenReturn(listOf("user1", "user2"))
    `when`(mockDocument1.get("sharedRecipes")).thenReturn(emptyList<String>())

    `when`(mockDocument2.id).thenReturn("house456")
    `when`(mockDocument2.getString("name")).thenReturn("Household 2")
    `when`(mockDocument2.get("members")).thenReturn(listOf("user3"))
    `when`(mockDocument2.get("sharedRecipes")).thenReturn(listOf("recipe1"))

    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument1, mockDocument2))
    `when`(mockCollection.whereIn(FieldPath.documentId(), householdIds).get().await())
        .thenReturn(mockSnapshot)

    // Act
    val households = houseHoldRepository.getHouseholds(householdIds)

    // Assert
    assertNotNull(households)
    assertEquals(2, households.size)
    assertEquals("Household 1", households[0].name)
    assertEquals("Household 2", households[1].name)
    verify(mockCollection).whereIn(FieldPath.documentId(), householdIds)
  }

  @Test
  fun `getHouseholds handles Firestore errors`(): Unit = runBlocking {
    // Arrange
    val householdIds = listOf("house123", "house456")
    `when`(mockCollection.whereIn(FieldPath.documentId(), householdIds).get())
        .thenThrow(RuntimeException("Firestore error"))

    // Act
    val households = houseHoldRepository.getHouseholds(householdIds)

    // Assert
    assertNotNull(households)
    assertTrue(households.isEmpty()) // Should return an empty list on error
    verify(mockCollection).whereIn(FieldPath.documentId(), householdIds)
  }

  @Test
  fun `updateHousehold updates an existing household in Firestore`(): Unit = runBlocking {
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
  fun `startListeningForHouseholds sets up Firestore listener`() {
    val mockListenerRegistration = mock(ListenerRegistration::class.java)
    `when`(
            mockDocument
                .collection("members")
                .addSnapshotListener(any<EventListener<QuerySnapshot>>()))
        .thenReturn(mockListenerRegistration)

    houseHoldRepository.startListeningForHouseholds(listOf("house123"))

    verify(mockDocument.collection("members"))
        .addSnapshotListener(any<EventListener<QuerySnapshot>>())
  }

  @Test
  fun `stopListeningForHouseholds removes Firestore listener`() {
    val mockListenerRegistration = mock(ListenerRegistration::class.java)

    val field =
        HouseholdRepositoryFirestore::class.java.getDeclaredField("householdsListenerRegistration")
    field.isAccessible = true
    field.set(houseHoldRepository, mockListenerRegistration)

    houseHoldRepository.stopListeningForHouseholds()

    verify(mockListenerRegistration).remove()
  }
}
