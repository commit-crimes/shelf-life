package com.android.shelflife.model.newInvitations

import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.newInvitations.Invitation
import com.android.shelfLife.model.newInvitations.InvitationRepositoryFirestore
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.user.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class InvitationRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollection: CollectionReference
  @Mock private lateinit var mockDocument: DocumentReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var firebaseAuth: FirebaseAuth

  private lateinit var invitationRepository: InvitationRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

      // Initialize Firebase if necessary
      if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
          FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
      }

      // Mock FirebaseAuth and FirebaseUser
      firebaseAuth = mock(FirebaseAuth::class.java)
      val mockUser = mock(FirebaseUser::class.java)
      `when`(firebaseAuth.currentUser).thenReturn(mockUser)
      `when`(mockUser.uid).thenReturn("testInviterUserId")

      // Mock Firestore collection and document references
      `when`(mockFirestore.collection("invitations")).thenReturn(mockCollection)
      `when`(mockCollection.document(anyString())).thenReturn(mockDocument)

      // Initialize the InvitationRepositoryFirestore with mocks
      invitationRepository = InvitationRepositoryFirestore(mockFirestore, firebaseAuth)

      // Set the Dispatchers to use the TestCoroutineDispatcher
      Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `sendInvitation sends invitation to Firestore`() {
    val household =
        HouseHold(
            uid = "householdId",
            name = "Household Name",
            members = listOf("member1"),
            sharedRecipes = listOf("recipe1"))
    val invitedUser =
        User(
            uid = "invitedUserId",
            username = "InvitedUser",
            email = "inviteduser@example.com",
            householdUIDs = listOf(),
            recipeUIDs = listOf(),
            invitationUIDs = listOf())
    val invitationId = "invitationId123"
    val mockTask: Task<Void> = Tasks.forResult(null)

    // Mock the collection and document behavior
    `when`(mockCollection.document()).thenReturn(mockDocument)
    `when`(mockDocument.id).thenReturn(invitationId)
    `when`(mockDocument.set(any<Map<String, Any>>())).thenReturn(mockTask)

    invitationRepository.sendInvitation(household, invitedUser)

    val invitationDataCaptor = ArgumentCaptor.forClass(Map::class.java)
    verify(mockDocument).set(invitationDataCaptor.capture())

    val invitationData = invitationDataCaptor.value as Map<String, Any>
    assertEquals(invitationId, invitationData["invitationId"])
    assertEquals("householdId", invitationData["householdId"])
    assertEquals("Household Name", invitationData["householdName"])
    assertEquals("invitedUserId", invitationData["invitedUserId"])
  }

  @Test
  fun `getInvitations returns list of invitations`() = runTest {
    val invitationIds = listOf("inv1", "inv2")
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    val mockDocuments = listOf(mockDocumentSnapshot, mockDocumentSnapshot)

    `when`(mockCollection.whereIn(FieldPath.documentId(), invitationIds)).thenReturn(mockCollection)
    `when`(mockCollection.get()).thenReturn(mockTask)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocuments)
    `when`(mockDocumentSnapshot.getString("invitationId")).thenReturn("inv1", "inv2")
    `when`(mockDocumentSnapshot.getString("householdId")).thenReturn("house1", "house2")
    `when`(mockDocumentSnapshot.getString("householdName")).thenReturn("House 1", "House 2")
    `when`(mockDocumentSnapshot.getString("invitedUserId")).thenReturn("user1", "user2")
    `when`(mockDocumentSnapshot.getString("inviterUserId")).thenReturn("user3", "user4")
    `when`(mockDocumentSnapshot.getTimestamp("timestamp"))
        .thenReturn(Timestamp.now(), Timestamp.now())

    val invitations = invitationRepository.getInvitations(invitationIds)

    assertEquals(2, invitations.size)
    assertEquals("inv1", invitations[0].invitationId)
    assertEquals("House 1", invitations[0].householdName)
  }

  @Test
  fun `acceptInvitation updates household members and deletes invitation`() = runTest {
    val invitation =
        Invitation(
            invitationId = "inv1",
            householdId = "house1",
            householdName = "Household",
            invitedUserId = "user1",
            inviterUserId = "user2",
            timestamp = Timestamp.now())
    val householdCollection: CollectionReference = mock(CollectionReference::class.java)
    val householdDocument: DocumentReference = mock(DocumentReference::class.java)
    val deleteTask: Task<Void> = Tasks.forResult(null)
    val updateTask: Task<Void> = Tasks.forResult(null)

    // Mock Firestore collections and documents
    `when`(mockFirestore.collection("households")).thenReturn(householdCollection)
    `when`(householdCollection.document("house1")).thenReturn(householdDocument)
    `when`(mockDocument.delete()).thenReturn(deleteTask)
    `when`(householdDocument.update(eq("members"), any())).thenReturn(updateTask)

    // Call the method being tested
    invitationRepository.acceptInvitation(invitation)

    // Verify interactions
    verify(mockDocument).delete()
    verify(householdDocument).update(eq("members"), any())
  }

  @Test
  fun `declineInvitation deletes invitation from Firestore`() = runTest {
    val invitation =
        Invitation(
            invitationId = "inv1",
            householdId = "house1",
            householdName = "Household",
            invitedUserId = "user1",
            inviterUserId = "user2",
            timestamp = Timestamp.now())
    val deleteTask: Task<Void> = Tasks.forResult(null)

    `when`(mockDocument.delete()).thenReturn(deleteTask)

    invitationRepository.declineInvitation(invitation)

    verify(mockDocument).delete()
  }

  @Test
  fun `removeInvitationListener removes the listener if registered`() {
    val listenerRegistration: ListenerRegistration = mock(ListenerRegistration::class.java)
    invitationRepository.listenerRegistration = listenerRegistration

    invitationRepository.removeInvitationListener()

    verify(listenerRegistration).remove()
    assertNull(invitationRepository.listenerRegistration) // This should now pass
  }

  @Test
  fun `removeInvitationListener does nothing if listener is not registered`() {
    invitationRepository.listenerRegistration = null

    invitationRepository.removeInvitationListener()

    // No exception or verification required
  }
}
