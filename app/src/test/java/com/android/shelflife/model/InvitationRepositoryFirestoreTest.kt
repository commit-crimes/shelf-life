package com.android.shelflife.model

import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class InvitationRepositoryFirestoreTest {

  @get:Rule var hiltRule = HiltAndroidRule(this)

  @Inject lateinit var invitationRepository: InvitationRepositoryFirestore

  @Inject lateinit var mockFirestore: FirebaseFirestore

  @Inject lateinit var mockAuth: FirebaseAuth

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @Test
  fun sendInvitationAddsDocumentToFirestore() {
    // Given a household and a user to invite
    val household =
        HouseHold(
            uid = "house123",
            name = "Test Household",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())
    val invitedUserID = "invitedUser"

    // When we send an invitation
    invitationRepository.sendInvitation(household, invitedUserID)

    // Verify the first call to create the invitation
    val invitationsCollection = mockFirestore.collection("invitations")
    verify(invitationsCollection).document(eq("generatedMockDocId"))

    // Verify the second call to update the user's invitations
    val usersCollection = mockFirestore.collection("users")
    verify(usersCollection).document(eq("invitedUser"))
  }

  @Test
  fun acceptInvitationUpdatesHouseholdMembersAndDeletesInvitation(): Unit = runBlocking {
    val invitation =
        Invitation(
            invitationId = "inv1",
            householdId = "house1",
            householdName = "Household",
            invitedUserId = "user1",
            inviterUserId = "user2",
            timestamp = Timestamp.now())
    val mockCollection = mock(CollectionReference::class.java)
    val mockDocument = mock(DocumentReference::class.java)

    `when`(mockFirestore.collection("invitations")).thenReturn(mockCollection)
    `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
    val householdCollection = mock(CollectionReference::class.java)
    val householdDocument = mock(DocumentReference::class.java)

    `when`(mockFirestore.collection("households")).thenReturn(householdCollection)
    `when`(householdCollection.document("house1")).thenReturn(householdDocument)
    `when`(mockDocument.delete()).thenReturn(Tasks.forResult(null))
    `when`(householdDocument.update(eq("members"), any())).thenReturn(Tasks.forResult(null))

    invitationRepository.acceptInvitation(invitation)

    verify(mockDocument).delete()
    verify(householdDocument).update(eq("members"), any())
  }

  @Test
  fun declineInvitationDeletesInvitationFromFirestore(): Unit = runBlocking {
    val invitation =
        Invitation(
            invitationId = "inv1",
            householdId = "house1",
            householdName = "Household",
            invitedUserId = "user1",
            inviterUserId = "user2",
            timestamp = Timestamp.now())
    val mockCollection = mock(CollectionReference::class.java)
    val mockDocument = mock(DocumentReference::class.java)

    `when`(mockFirestore.collection("invitations")).thenReturn(mockCollection)
    `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
    `when`(mockDocument.delete()).thenReturn(Tasks.forResult(null))

    invitationRepository.declineInvitation(invitation)

    verify(mockDocument).delete()
  }

  @Test
  fun convertToInvitation_ReturnsInvitation_WhenDocumentIsValid() {
    // Mock a valid DocumentSnapshot
    val mockDocument = mock(DocumentSnapshot::class.java)
    `when`(mockDocument.getString("invitationId")).thenReturn("testInvitationId")
    `when`(mockDocument.getString("householdId")).thenReturn("testHouseholdId")
    `when`(mockDocument.getString("householdName")).thenReturn("Test Household")
    `when`(mockDocument.getString("invitedUserId")).thenReturn("testInvitedUserId")
    `when`(mockDocument.getString("inviterUserId")).thenReturn("testInviterUserId")
    `when`(mockDocument.getTimestamp("timestamp")).thenReturn(Timestamp.now())

    // Call the method
    val invitation = invitationRepository.convertToInvitation(mockDocument)

    // Assert the result
    assertNotNull("Invitation should not be null", invitation)
    assertEquals("testInvitationId", invitation?.invitationId)
    assertEquals("testHouseholdId", invitation?.householdId)
    assertEquals("Test Household", invitation?.householdName)
    assertEquals("testInvitedUserId", invitation?.invitedUserId)
    assertEquals("testInviterUserId", invitation?.inviterUserId)
    assertNotNull("Timestamp should not be null", invitation?.timestamp)
  }

  @Test
  fun convertToInvitation_ReturnsNull_WhenDocumentIsInvalid() {
    // Mock an invalid DocumentSnapshot (missing required fields)
    val mockDocument = mock(DocumentSnapshot::class.java)
    `when`(mockDocument.getString("invitationId")).thenReturn(null)

    // Call the method
    val invitation = invitationRepository.convertToInvitation(mockDocument)

    // Assert the result
    assertNull("Invitation should be null for invalid document", invitation)
  }
}
