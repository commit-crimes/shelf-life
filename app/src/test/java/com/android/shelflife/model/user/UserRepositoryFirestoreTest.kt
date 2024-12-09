package com.android.shelfLife.model.user

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {

  @get:Rule
  var hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var userRepository: UserRepositoryFirestore

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

    `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
    `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
  }

  @Test
  fun `initializeUserData fetches user data from Firestore`() = runBlocking {
    // Arrange
    val mockSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockAuth.currentUser?.uid).thenReturn("testUserId")
    `when`(mockDocument.get().await()).thenReturn(mockSnapshot)
    `when`(mockSnapshot.getString("username")).thenReturn("Test User")
    `when`(mockSnapshot.getString("email")).thenReturn("test@example.com")

    // Act
    userRepository.initializeUserData(mock())

    // Assert
    assertEquals("Test User", userRepository.user.value?.username)
    assertEquals("test@example.com", userRepository.user.value?.email)
    verify(mockDocument).get()
  }

  @Test
  fun `initializeUserData handles missing user gracefully`() = runBlocking {
    // Arrange
    `when`(mockAuth.currentUser?.uid).thenReturn(null)

    // Act
    val exception = Assert.assertThrows(Exception::class.java) {
      runBlocking {
        userRepository.initializeUserData(mock())
      }
    }

    // Assert
    assertEquals("User not logged in", exception.message)
  }

  @Test
  fun `updateUsername updates username in Firestore`() = runBlocking {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null) // Explicitly specify Task<Void>
    `when`(mockDocument.update("username", "New Username")).thenReturn(mockTask)

    // Act
    userRepository.updateUsername("New Username")

    // Assert
    verify(mockDocument).update("username", "New Username")
  }

  @Test
  fun `addHouseholdUID adds household UID to Firestore`() = runBlocking {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null) // Specify Task<Void>
    `when`(mockDocument.update("householdUIDs", FieldValue.arrayUnion("house1"))).thenReturn(mockTask)

    // Act
    userRepository.addHouseholdUID("house1")

    // Assert
    verify(mockDocument).update("householdUIDs", FieldValue.arrayUnion("house1"))
  }

  @Test
  fun `deleteHouseholdUID removes household UID from Firestore`() = runBlocking {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null) // Specify Task<Void>
    `when`(mockDocument.update("householdUIDs", FieldValue.arrayRemove("house1"))).thenReturn(mockTask)

    // Act
    userRepository.deleteHouseholdUID("house1")

    // Assert
    verify(mockDocument).update("householdUIDs", FieldValue.arrayRemove("house1"))
  }

  @Test
  fun `getUserIds fetches user IDs by email`() = runBlocking {
    // Arrange
    val emails = setOf("test1@example.com", "test2@example.com")
    val mockSnapshot = mock(QuerySnapshot::class.java)
    val mockDoc1 = mock(DocumentSnapshot::class.java)
    val mockDoc2 = mock(DocumentSnapshot::class.java)

    `when`(mockDoc1.getString("email")).thenReturn("test1@example.com")
    `when`(mockDoc1.id).thenReturn("user1")
    `when`(mockDoc2.getString("email")).thenReturn("test2@example.com")
    `when`(mockDoc2.id).thenReturn("user2")
    `when`(mockSnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))
    `when`(mockCollection.whereIn("email", emails.toList()).get().await()).thenReturn(mockSnapshot)

    // Act
    val userIds = mutableMapOf<String, String>()
    userRepository.getUserIds(emails) { result -> userIds.putAll(result) }

    // Assert
    assertEquals(2, userIds.size)
    assertEquals("user1", userIds["test1@example.com"])
    assertEquals("user2", userIds["test2@example.com"])
  }
}