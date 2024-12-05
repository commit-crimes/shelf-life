package com.android.shelfLife.model.user

import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import java.lang.reflect.Field
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockUserCollection: CollectionReference
  @Mock private lateinit var mockUserDocument: DocumentReference
  @Mock private lateinit var mockFirebaseUser: FirebaseUser
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  private lateinit var userRepository: UserRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    // Mock FirebaseAuth and FirebaseUser
    `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.uid).thenReturn("testUserId")

    // Mock FirebaseFirestore
    `when`(mockFirestore.collection("users")).thenReturn(mockUserCollection)
    `when`(mockUserCollection.document("testUserId")).thenReturn(mockUserDocument)

    // Initialize the UserRepositoryFirestore with mocks
    userRepository = UserRepositoryFirestore(mockFirestore)
    val authField: Field = userRepository.javaClass.getDeclaredField("auth")
    authField.isAccessible = true
    authField.set(userRepository, mockAuth)

    // Set the Dispatchers to use the TestCoroutineDispatcher
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `getNewUid returns new document ID`() {
    `when`(mockUserCollection.document()).thenReturn(mockUserDocument)
    `when`(mockUserDocument.id).thenReturn("newUserId")

    val uid = userRepository.getNewUid()

    assertEquals("newUserId", uid)
  }

  @Test
  fun `initializeUserData successfully initializes user data`() = runTest {
    val testUser =
        User(
            uid = "testUserId",
            username = "TestUser",
            email = "test@example.com",
            selectedHouseholdUID = "house1",
            householdUIDs = listOf("house1", "house2"),
            recipeUIDs = listOf("recipe1"),
            invitationUIDs = listOf("invite1"))

    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
    `when`(mockUserDocument.get()).thenReturn(mockTask)
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockDocumentSnapshot.id).thenReturn("testUserId")
    `when`(mockDocumentSnapshot.getString("username")).thenReturn("TestUser")
    `when`(mockDocumentSnapshot.getString("email")).thenReturn("test@example.com")
    `when`(mockDocumentSnapshot.getString("selectedHouseholdUID")).thenReturn("house1")
    `when`(mockDocumentSnapshot.get("householdUIDs")).thenReturn(listOf("house1", "house2"))
    `when`(mockDocumentSnapshot.get("recipeUIDs")).thenReturn(listOf("recipe1"))
    `when`(mockDocumentSnapshot.get("invitationUIDs")).thenReturn(listOf("invite1"))

    userRepository.initializeUserData()

    // Since initializeUserData updates _user, we need to access it
    val user = userRepository.user.value

    assertNotNull(user)
    assertEquals(testUser, user)
  }

  @Test
  fun `initializeUserData sets user to null when snapshot does not exist`() = runTest {
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
    `when`(mockUserDocument.get()).thenReturn(mockTask)
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)

    userRepository.initializeUserData()

    val user = userRepository.user.value
    assertNull(user)
  }

  @Test(expected = Exception::class)
  fun `initializeUserData throws exception when not logged in`() = runTest {
    `when`(mockAuth.currentUser).thenReturn(null)

    userRepository.initializeUserData()
  }

  @Test
  fun `addHouseholdUID adds household UID to Firestore and updates local user`() = runTest {
    val initialUser =
        User(
            uid = "testUserId",
            username = "TestUser",
            email = "test@example.com",
            selectedHouseholdUID = "house1",
            householdUIDs = mutableListOf("house1"))
    // Access the private _user variable via reflection or adjust visibility
    val userField = userRepository.javaClass.getDeclaredField("_user")
    userField.isAccessible = true
    val _user = userField.get(userRepository) as MutableStateFlow<User?>
    _user.value = initialUser

    val mockTask: Task<Void> = Tasks.forResult(null)
    // Use anyString() and any() to match any parameters
    `when`(mockUserDocument.update(anyString(), any())).thenReturn(mockTask)

    userRepository.addHouseholdUID("house2")

    val user = userRepository.user.value

    assertNotNull(user)
    assertEquals(listOf("house1", "house2"), user?.householdUIDs)
  }

  @Test
  fun `deleteHouseholdUID removes household UID from Firestore and updates local user`() = runTest {
    val initialUser =
        User(
            uid = "testUserId",
            username = "TestUser",
            email = "test@example.com",
            selectedHouseholdUID = "house1",
            householdUIDs = mutableListOf("house1", "house2"))
    // Access the private _user variable via reflection or adjust visibility
    val userField = userRepository.javaClass.getDeclaredField("_user")
    userField.isAccessible = true
    val _user = userField.get(userRepository) as MutableStateFlow<User?>
    _user.value = initialUser

    val mockTask: Task<Void> = Tasks.forResult(null)
    // Use anyString() and any() to match any parameters
    `when`(mockUserDocument.update(anyString(), any())).thenReturn(mockTask)

    userRepository.deleteHouseholdUID("house1")

    val user = userRepository.user.value

    assertNotNull(user)
    assertEquals(listOf("house2"), user?.householdUIDs)
  }

  @Test
  fun `updateUsername updates username in Firestore and local user`() = runTest {
    val initialUser =
        User(
            uid = "testUserId",
            username = "OldUsername",
            email = "test@example.com",
            selectedHouseholdUID = "")
    // Access the private _user variable via reflection or adjust visibility
    val userField = userRepository.javaClass.getDeclaredField("_user")
    userField.isAccessible = true
    val _user = userField.get(userRepository) as MutableStateFlow<User?>
    _user.value = initialUser

    val mockTask: Task<Void> = Tasks.forResult(null)
    `when`(mockUserDocument.update("username", "NewUsername")).thenReturn(mockTask)

    userRepository.updateUsername("NewUsername")

    val user = userRepository.user.value

    assertNotNull(user)
    assertEquals("NewUsername", user?.username)
  }

  @Test
  fun `updateEmail updates email in FirebaseAuth and Firestore and local user`() = runTest {
    val initialUser =
        User(
            uid = "testUserId",
            username = "TestUser",
            email = "old@example.com",
            selectedHouseholdUID = "")
    // Access the private _user variable via reflection or adjust visibility
    val userField = userRepository.javaClass.getDeclaredField("_user")
    userField.isAccessible = true
    val _user = userField.get(userRepository) as MutableStateFlow<User?>
    _user.value = initialUser

    val mockTaskAuth: Task<Void> = Tasks.forResult(null)
    val mockTaskFirestore: Task<Void> = Tasks.forResult(null)
    `when`(mockFirebaseUser.updateEmail("new@example.com")).thenReturn(mockTaskAuth)
    `when`(mockUserDocument.update("email", "new@example.com")).thenReturn(mockTaskFirestore)

    userRepository.updateEmail("new@example.com")

    val user = userRepository.user.value

    assertNotNull(user)
    assertEquals("new@example.com", user?.email)
  }

  @Test(expected = Exception::class)
  fun `updateEmail throws exception when not logged in`() = runTest {
    `when`(mockAuth.currentUser).thenReturn(null)

    userRepository.updateEmail("new@example.com")
  }

  // Additional tests for other methods...

}
