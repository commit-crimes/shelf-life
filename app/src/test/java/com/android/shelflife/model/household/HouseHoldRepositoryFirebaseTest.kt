package com.android.shelfLife.model.household

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HouseholdRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockUser: FirebaseUser

  private lateinit var householdRepository: HouseholdRepositoryFirestore

  private val household =
      HouseHold(
          uid = "1",
          name = "Test Household",
          members = listOf("testUserId"), // or whatever initial members you want
          foodItems = listOf() // Populate as needed
          )

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    // Initialize the mock instances
    mockAuth = mock(FirebaseAuth::class.java)
    mockUser = mock(FirebaseUser::class.java)

    // Mock the behavior of the FirebaseAuth instance
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn("testUserId")

    // Inject mockFirebaseAuth into your repository
    householdRepository = HouseholdRepositoryFirestore(mockFirestore)
    // If you need to set FirebaseAuth in your repository, you should add a constructor or setter
    // for that

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("newHouseholdId")
    val uid = householdRepository.getNewUid()
    assert(uid == "newHouseholdId")
  }

  @Test
  fun addHousehold_shouldCallOnFailureWhenUserIsNotLoggedIn() {
    `when`(mockAuth.currentUser).thenReturn(null)

    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    householdRepository.addHousehold(household, onSuccess, onFailure)

    verify(onFailure).invoke(any())
    verify(onSuccess, never()).invoke()
  }

  @Test
  fun deleteHouseholdById_shouldCallOnFailureWhenUserIsNotLoggedIn() {
    `when`(mockAuth.currentUser).thenReturn(null)

    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    householdRepository.deleteHouseholdById("householdId", onSuccess, onFailure)

    verify(onFailure).invoke(any())
    verify(onSuccess, never()).invoke()
  }

  @Test
  fun updateHousehold_shouldCallOnFailureWhenUserIsNotLoggedIn() {
    `when`(mockAuth.currentUser).thenReturn(null)

    val onSuccess: () -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    householdRepository.updateHousehold(household, onSuccess, onFailure)

    verify(onFailure).invoke(any())
    verify(onSuccess, never()).invoke()
  }
}
