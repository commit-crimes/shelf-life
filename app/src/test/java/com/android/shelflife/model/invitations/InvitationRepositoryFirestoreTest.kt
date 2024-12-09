package com.android.shelfLife.model.invitations

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class InvitationRepositoryFirestoreTest {

  @get:Rule
  var hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var invitationRepository: InvitationRepositoryFirestore

  @Inject
  lateinit var mockFirestore: FirebaseFirestore

  @Inject
  lateinit var mockAuth: FirebaseAuth

  private lateinit var mockCollection: CollectionReference
  private lateinit var mockDocument: DocumentReference

  @Before
  fun setUp() {
    hiltRule.inject()

    mockCollection = mock(CollectionReference::class.java)
    mockDocument = mock(DocumentReference::class.java)

    `when`(mockFirestore.collection("invitations")).thenReturn(mockCollection)
    `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
  }

  @Test
  fun `getInvitations returns list of invitations`() = runBlocking {
    val invitationIds = listOf("inv1", "inv2")
    val mockSnapshot = mock(QuerySnapshot::class.java)
    val mockDocument1 = mock(DocumentSnapshot::class.java)
    val mockDocument2 = mock(DocumentSnapshot::class.java)

    `when`(mockDocument1.getString("invitationId")).thenReturn("inv1")
    `when`(mockDocument1.getString("householdId")).thenReturn("house1")
    `when`(mockDocument1.getString("householdName")).thenReturn("House 1")
    `when`(mockDocument1.getString("invitedUserId")).thenReturn("user1")
    `when`(mockDocument1.getString("inviterUserId")).thenReturn("user2")
    `when`(mockDocument1.getTimestamp("timestamp")).thenReturn(Timestamp.now())

    `when`(mockDocument2.getString("invitationId")).thenReturn("inv2")
    `when`(mockDocument2.getString("householdId")).thenReturn("house2")
    `when`(mockDocument2.getString("householdName")).thenReturn("House 2")
    `when`(mockDocument2.getString("invitedUserId")).thenReturn("user3")
    `when`(mockDocument2.getString("inviterUserId")).thenReturn("user4")
    `when`(mockDocument2.getTimestamp("timestamp")).thenReturn(Timestamp.now())

    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument1, mockDocument2))
    `when`(mockCollection.whereIn(FieldPath.documentId(), invitationIds).get().await()).thenReturn(mockSnapshot)

    val invitations = invitationRepository.getInvitations(invitationIds)

    assertEquals(2, invitations.size)
    assertEquals("inv1", invitations[0].invitationId)
    assertEquals("House 1", invitations[0].householdName)
  }

  @Test
  fun `acceptInvitation updates household members and deletes invitation`() = runBlocking {
    val invitation = Invitation(
      invitationId = "inv1",
      householdId = "house1",
      householdName = "Household",
      invitedUserId = "user1",
      inviterUserId = "user2",
      timestamp = Timestamp.now()
    )

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
  fun `declineInvitation deletes invitation from Firestore`() = runBlocking {
    val invitation = Invitation(
      invitationId = "inv1",
      householdId = "house1",
      householdName = "Household",
      invitedUserId = "user1",
      inviterUserId = "user2",
      timestamp = Timestamp.now()
    )

    `when`(mockDocument.delete()).thenReturn(Tasks.forResult(null))

    invitationRepository.declineInvitation(invitation)

    verify(mockDocument).delete()
  }

  @Test
  fun `removeInvitationListener removes the listener if registered`() {
    val listenerRegistration = mock(ListenerRegistration::class.java)
    invitationRepository.listenerRegistration = listenerRegistration

    invitationRepository.removeInvitationListener()

    verify(listenerRegistration).remove()
    assertNull(invitationRepository.listenerRegistration)
  }

  @Test
  fun `removeInvitationListener does nothing if listener is not registered`() {
    invitationRepository.listenerRegistration = null

    invitationRepository.removeInvitationListener()

    // Nothing to verify as the listener is null
  }
}