package com.android.shelflife.model

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.user.UserRepositoryFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class UserRepositoryFirestoreTest {

  @get:Rule var hiltRule = HiltAndroidRule(this)

  @Inject lateinit var userRepository: UserRepositoryFirestore

  @Inject lateinit var mockFirestore: FirebaseFirestore

  @Inject lateinit var mockAuth: FirebaseAuth

  private var googleSignInMockedStatic: MockedStatic<GoogleSignIn>? = null

  @Before
  fun setUp() {
    hiltRule.inject()
    googleSignInMockedStatic?.close()
    googleSignInMockedStatic = mockStatic(GoogleSignIn::class.java)
    val mockDocumentReference = mock(DocumentReference::class.java)
    `when`(mockDocumentReference.id).thenReturn("mockUid")
    val mockUserCollection = mockFirestore.collection("users")
    `when`(mockUserCollection.document()).thenReturn(mockDocumentReference)
    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    val mockGoogleSignInAccount = mock(GoogleSignInAccount::class.java)
    `when`(GoogleSignIn.getLastSignedInAccount(mockContext)).thenReturn(mockGoogleSignInAccount)

    `when`(mockGoogleSignInAccount.displayName).thenReturn("Test User")
    `when`(mockGoogleSignInAccount.email).thenReturn("test@example.com")
    `when`(mockGoogleSignInAccount.photoUrl).thenReturn(Uri.parse("https://example.com/photo.jpg"))
  }

  @After
  fun tearDown() {
    googleSignInMockedStatic?.close()
  }

  @Test
  public fun initializeUserDataFetchesUserDataFromFirestore(): Unit = runBlocking {
    val mockFirebaseUser = mock(FirebaseUser::class.java)
    `when`(mockFirebaseUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)

    // Setup Firestore mock to return a non-existing document
    val mockUserCollection = mockFirestore.collection("users")
    val mockUserDocument = mockUserCollection.document("testUserId")

    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(false)

    val mockTask = Tasks.forResult(mockSnapshot)
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    // Perform initialization
    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    // Wait for async operations
    delay(200)

    // Assertions
    val user = userRepository.user.value
    assertNotNull("User should not be null after initialization", user)
    assertEquals("Username should match", "Test User", user?.username)
    assertEquals("Email should match", "test@example.com", user?.email)
    assertEquals("UID should match", "testUserId", user?.uid)
  }

  @Test(expected = Exception::class)
  fun initializeUserDataThrowsExceptionWhenNoUserLoggedIn() = runBlocking {
    // Setup auth to return null current user
    `when`(mockAuth.currentUser).thenReturn(null)

    // Prepare context
    val mockContext = ApplicationProvider.getApplicationContext<Context>()

    // This should throw an exception
    userRepository.initializeUserData(mockContext)
  }

  companion object {
    @BeforeClass
    @JvmStatic
    fun setup() {
      System.setProperty("org.powermock.module.junit4.runner.listener.executor.timeout", "0")
    }
  }

  @Test
  fun getNewUidReturnsNonEmptyString() {
    val newUid = userRepository.getNewUid()
    assertNotNull("New UID should not be null", newUid)
    assert(newUid.isNotEmpty()) { "New UID should not be empty" }
  }

  @Test
  fun getUserIdsReturnsCorrectMappings() = runBlocking {
    val emails = setOf("test1@example.com", "test2@example.com")

    // Mock Firestore for getUserIds
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockDoc1 = mock(DocumentSnapshot::class.java)
    val mockDoc2 = mock(DocumentSnapshot::class.java)

    `when`(mockDoc1.id).thenReturn("userId1")
    `when`(mockDoc2.id).thenReturn("userId2")
    `when`(mockDoc1.getString("email")).thenReturn("test1@example.com")
    `when`(mockDoc2.getString("email")).thenReturn("test2@example.com")

    val docs = listOf(mockDoc1, mockDoc2)
    `when`(mockQuerySnapshot.documents).thenReturn(docs)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    val mockCollection = mockFirestore.collection("users")
    `when`(mockCollection.whereIn("email", listOf("test1@example.com", "test2@example.com")))
        .thenReturn(mockCollection)
    `when`(mockCollection.get()).thenReturn(mockTask)

    val result = userRepository.getUserIds(emails)
    assertEquals("userId1", result["test1@example.com"])
    assertEquals("userId2", result["test2@example.com"])
  }

  @Test
  fun getUserEmailsReturnsCorrectMappings() = runBlocking {
    val userIds = listOf("userId1", "userId2")

    // Mock Firestore for getUserEmails
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockDoc1 = mock(DocumentSnapshot::class.java)
    val mockDoc2 = mock(DocumentSnapshot::class.java)

    `when`(mockDoc1.id).thenReturn("userId1")
    `when`(mockDoc2.id).thenReturn("userId2")
    `when`(mockDoc1.getString("email")).thenReturn("test1@example.com")
    `when`(mockDoc2.getString("email")).thenReturn("test2@example.com")

    val docs = listOf(mockDoc1, mockDoc2)
    `when`(mockQuerySnapshot.documents).thenReturn(docs)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    val mockCollection = mockFirestore.collection("users")
    `when`(mockCollection.whereIn(FieldPath.documentId(), userIds)).thenReturn(mockCollection)
    `when`(mockCollection.get()).thenReturn(mockTask)

    val result = userRepository.getUserEmails(userIds)
    assertEquals("test1@example.com", result["userId1"])
    assertEquals("test2@example.com", result["userId2"])
  }

  @Test
  fun addHouseholdUIDUpdatesUserData() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Initialize user data so we have a baseline user
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    // Add household UID
    userRepository.addHouseholdUID("household123")

    val user = userRepository.user.value
    assertNotNull(user)
    assert(user!!.householdUIDs.contains("household123"))
  }

  @Test
  fun deleteHouseholdUIDRemovesFromUserData() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Start with a user who already has a household
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(listOf("household123"))
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    // Delete the household UID
    userRepository.deleteHouseholdUID("household123")

    val user = userRepository.user.value
    assertNotNull(user)
    assert(!user!!.householdUIDs.contains("household123"))
  }

  @Test
  fun updateSelectedHouseholdUIDChangesValue() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Initialize user data
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    userRepository.updateSelectedHouseholdUID("householdXYZ")

    val user = userRepository.user.value
    assertNotNull(user)
    assertEquals("householdXYZ", user?.selectedHouseholdUID)
  }

  @Test
  fun addRecipeUIDUpdatesUserData() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Initialize user data
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    userRepository.addRecipeUID("recipe123")

    val user = userRepository.user.value
    assertNotNull(user)
    assert(user!!.recipeUIDs.contains("recipe123"))
  }

  @Test
  fun deleteRecipeUIDRemovesFromUserData() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Start with a user who already has a recipe
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(listOf("recipe123"))
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    // Remove recipe
    userRepository.deleteRecipeUID("recipe123")

    val user = userRepository.user.value
    assertNotNull(user)
    assert(!user!!.recipeUIDs.contains("recipe123"))
  }

  @Test
  fun deleteInvitationUIDRemovesFromUserData() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Mock initial user data
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(listOf("invite1", "invite2"))

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    // Mock update operation
    `when`(mockUserDocument.update(eq("invitationUIDs"), any())).thenReturn(Tasks.forResult(null))

    // Initialize user data
    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    // Simulate snapshot update after deletion
    val updatedSnapshot = mock(DocumentSnapshot::class.java)
    `when`(updatedSnapshot.exists()).thenReturn(true)
    `when`(updatedSnapshot.id).thenReturn("testUserId")
    `when`(updatedSnapshot.get("invitationUIDs")).thenReturn(listOf("invite2"))
    `when`(mockUserDocument.get()).thenReturn(Tasks.forResult(updatedSnapshot))

    // Perform deletion
    userRepository.deleteInvitationUID("invite1")

    // Force update to userRepository's local user
    userRepository.initializeUserData(mockContext)

    // Assert the updated state
    val user = userRepository.user.value
    assertNotNull(user)
    assert(!user!!.invitationUIDs.contains("invite1")) // This should now pass
    assert(user.invitationUIDs.contains("invite2"))
  }

  @Test
  fun updateUsernameChangesUserValue() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Initialize user data
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Old User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    userRepository.updateUsername("New Username")

    val user = userRepository.user.value
    assertNotNull(user)
    assertEquals("New Username", user?.username)
  }

  @Test
  fun updateImageChangesUserValue() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Initialize user data
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("https://oldimage.com/photo.jpg")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    userRepository.updateImage("https://newimage.com/photo.jpg")

    val user = userRepository.user.value
    assertNotNull(user)
    assertEquals("https://newimage.com/photo.jpg", user?.photoUrl)
  }

  @Test
  fun updateEmailChangesUserValue() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Mock the updateEmail call on FirebaseUser
    doReturn(Tasks.forResult<Void>(null))
        .`when`(mockCurrentUser)
        .updateEmail("newemail@example.com")

    // Initialize user data
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("oldemail@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    userRepository.updateEmail("newemail@example.com")

    val user = userRepository.user.value
    assertNotNull(user)
    assertEquals("newemail@example.com", user?.email)
  }

  @Test
  fun updateSelectedHouseholdChangesUserValue() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Initialize user data
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("oldHousehold")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    userRepository.updateSelectedHousehold("householdABC")

    val user = userRepository.user.value
    assertNotNull(user)
    assertEquals("householdABC", user?.selectedHouseholdUID)
  }

  @Test
  fun selectHouseholdUpdatesValueWhenNotNull() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Initialize user data
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    userRepository.selectHousehold("householdXYZ")

    val user = userRepository.user.value
    assertNotNull(user)
    assertEquals("householdXYZ", user?.selectedHouseholdUID)
  }

  @Test
  fun selectHouseholdDoesNothingWhenNull() = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    // Initialize user with a known selectedHouseholdUID
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockSnapshot.exists()).thenReturn(true)
    `when`(mockSnapshot.id).thenReturn("testUserId")
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockSnapshot.getString("photoURL")).thenReturn("")
    `when`(mockSnapshot.getString("selectedHouseholdUID")).thenReturn("initialHousehold")
    `when`(mockSnapshot.get("householdUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("recipeUIDs")).thenReturn(emptyList<String>())
    `when`(mockSnapshot.get("invitationUIDs")).thenReturn(emptyList<String>())

    val mockTask = Tasks.forResult(mockSnapshot)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    val mockContext = ApplicationProvider.getApplicationContext<Context>()
    userRepository.initializeUserData(mockContext)

    // Attempt to select a null household
    userRepository.selectHousehold(null)

    val user = userRepository.user.value
    assertNotNull(user)
    assertEquals("initialHousehold", user?.selectedHouseholdUID)
  }

  @Test
  fun addCurrentUserToHouseHoldAddsUIDToFirestore(): Unit = runBlocking {
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockCurrentUser.uid).thenReturn("testUserId")
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)

    val householdUID = "household123"
    val mockUpdateTask = Tasks.forResult<Void>(null)
    val mockUserDocument = mockFirestore.collection("users").document("testUserId")
    `when`(mockUserDocument.update(eq("householdUIDs"), any(FieldValue::class.java)))
        .thenReturn(mockUpdateTask)

    userRepository.addCurrentUserToHouseHold(householdUID, "testUserId")

    verify(mockUserDocument).update(eq("householdUIDs"), any(FieldValue::class.java))
  }

  @Test
  fun getUserNamesReturnsCorrectUsernames() = runBlocking {
    val userIds = listOf("userId1", "userId2")

    // Mock Firestore query results
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockDoc1 = mock(DocumentSnapshot::class.java)
    val mockDoc2 = mock(DocumentSnapshot::class.java)

    `when`(mockDoc1.id).thenReturn("userId1")
    `when`(mockDoc2.id).thenReturn("userId2")
    `when`(mockDoc1.getString("username")).thenReturn("User One")
    `when`(mockDoc2.getString("username")).thenReturn("User Two")

    val docs = listOf(mockDoc1, mockDoc2)
    `when`(mockQuerySnapshot.documents).thenReturn(docs)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    val mockCollection = mockFirestore.collection("users")
    `when`(mockCollection.whereIn(FieldPath.documentId(), userIds)).thenReturn(mockCollection)
    `when`(mockCollection.get()).thenReturn(mockTask)

    // Call getUserNames
    val result = userRepository.getUserNames(userIds)

    // Assertions
    assertEquals("User One", result["userId1"])
    assertEquals("User Two", result["userId2"])
  }
}
