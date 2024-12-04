package com.android.shelfLife.model.newhousehold

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import java.lang.reflect.Field
import junit.framework.TestCase.*
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
class HouseholdRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollection: CollectionReference
  @Mock private lateinit var mockDocument: DocumentReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var dataStore: DataStore<Preferences>
  @Mock private lateinit var listFoodItemRepository: FoodItemRepository
  @Mock private lateinit var userRepository: UserRepository

  private lateinit var householdRepository: HouseholdRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    // Mock Firestore collection and document references
    `when`(mockFirestore.collection("households")).thenReturn(mockCollection)
    `when`(mockCollection.document(anyString())).thenReturn(mockDocument)

    // Initialize the HouseholdRepositoryFirestore with mocks
    householdRepository =
        HouseholdRepositoryFirestore(
            mockFirestore, dataStore, listFoodItemRepository, userRepository)

    // Set the Dispatchers to use the TestCoroutineDispatcher
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `getNewUid returns new document ID`() {
    `when`(mockCollection.document()).thenReturn(mockDocument)
    `when`(mockDocument.id).thenReturn("newHouseholdId")

    val uid = householdRepository.getNewUid()

    assertEquals("newHouseholdId", uid)
  }

  @Test
  fun `getHouseholds returns list of households`() = runTest {
    val householdIds = listOf("house1", "house2")
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    val mockDocuments = listOf(mockDocumentSnapshot, mockDocumentSnapshot)

    `when`(mockCollection.whereIn(FieldPath.documentId(), householdIds)).thenReturn(mockCollection)
    `when`(mockCollection.get()).thenReturn(mockTask)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocuments)
    `when`(mockDocumentSnapshot.id).thenReturn("house1", "house2")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Household 1", "Household 2")
    `when`(mockDocumentSnapshot.get("members")).thenReturn(listOf("user1", "user2"))
    `when`(mockDocumentSnapshot.get("sharedRecipes")).thenReturn(listOf("recipe1"))

    val households = householdRepository.getHouseholds(householdIds)

    assertEquals(2, households.size)
    assertEquals("house1", households[0].uid)
    assertEquals("Household 1", households[0].name)
    assertEquals(listOf("user1", "user2"), households[0].members)
    assertEquals(listOf("recipe1"), households[0].sharedRecipes)
  }

  @Test
  fun `addHousehold adds household to Firestore and updates local cache`() = runTest {
    val newHousehold =
        HouseHold(
            uid = "house1",
            name = "New Household",
            members = listOf("user1"),
            sharedRecipes = listOf("recipe1"))

    val mockTask: Task<Void> = Tasks.forResult(null)
    `when`(mockDocument.set(any<Map<String, Any>>())).thenReturn(mockTask)

    // Access the private _households variable via reflection
    val householdsField: Field = householdRepository.javaClass.getDeclaredField("_households")
    householdsField.isAccessible = true
    val _households = householdsField.get(householdRepository) as MutableStateFlow<List<HouseHold>>
    _households.value = emptyList()

    householdRepository.addHousehold(newHousehold)

    val households = householdRepository.households.value

    assertEquals(1, households.size)
    assertEquals(newHousehold, households[0])
  }

  @Test
  fun `updateHousehold updates household in Firestore and local cache`() = runTest {
    val existingHousehold =
        HouseHold(
            uid = "house1",
            name = "Old Household",
            members = listOf("user1"),
            sharedRecipes = listOf("recipe1"))

    val updatedHousehold = existingHousehold.copy(name = "Updated Household")

    val mockTask: Task<Void> = Tasks.forResult(null)
    `when`(mockDocument.set(any<Map<String, Any>>())).thenReturn(mockTask)

    // Set initial households in cache
    val householdsField: Field = householdRepository.javaClass.getDeclaredField("_households")
    householdsField.isAccessible = true
    val _households = householdsField.get(householdRepository) as MutableStateFlow<List<HouseHold>>
    _households.value = listOf(existingHousehold)

    householdRepository.updateHousehold(updatedHousehold)

    val households = householdRepository.households.value

    assertEquals(1, households.size)
    assertEquals(updatedHousehold, households[0])
  }

  @Test
  fun `deleteHouseholdById deletes household from Firestore and local cache`() = runTest {
    val existingHousehold =
        HouseHold(
            uid = "house1",
            name = "Household",
            members = listOf("user1"),
            sharedRecipes = listOf("recipe1"))

    val mockTask: Task<Void> = Tasks.forResult(null)
    `when`(mockDocument.delete()).thenReturn(mockTask)

    // Set initial households in cache
    val householdsField: Field = householdRepository.javaClass.getDeclaredField("_households")
    householdsField.isAccessible = true
    val _households = householdsField.get(householdRepository) as MutableStateFlow<List<HouseHold>>
    _households.value = listOf(existingHousehold)

    householdRepository.deleteHouseholdById("house1")

    val households = householdRepository.households.value

    assertEquals(0, households.size)
  }

  @Test
  fun `getHouseholdMembers returns list of member IDs`() = runTest {
    val existingHousehold =
        HouseHold(
            uid = "house1",
            name = "Household",
            members = listOf("user1", "user2"),
            sharedRecipes = listOf("recipe1"))

    // Set initial households in cache
    val householdsField: Field = householdRepository.javaClass.getDeclaredField("_households")
    householdsField.isAccessible = true
    val _households = householdsField.get(householdRepository) as MutableStateFlow<List<HouseHold>>
    _households.value = listOf(existingHousehold)

    val members = householdRepository.getHouseholdMembers("house1")

    assertEquals(listOf("user1", "user2"), members)
  }

  @Test
  fun `initializeHouseholds updates local cache with fetched households`() = runTest {
    val householdIds = listOf("house1")
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    val mockDocuments = listOf(mockDocumentSnapshot)

    `when`(mockCollection.whereIn(FieldPath.documentId(), householdIds)).thenReturn(mockCollection)
    `when`(mockCollection.get()).thenReturn(mockTask)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocuments)
    `when`(mockDocumentSnapshot.id).thenReturn("house1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Household 1")
    `when`(mockDocumentSnapshot.get("members")).thenReturn(listOf("user1"))
    `when`(mockDocumentSnapshot.get("sharedRecipes")).thenReturn(listOf("recipe1"))

    // Access the private _households variable via reflection
    val householdsField: Field = householdRepository.javaClass.getDeclaredField("_households")
    householdsField.isAccessible = true
    val _households = householdsField.get(householdRepository) as MutableStateFlow<List<HouseHold>>
    _households.value = emptyList()

    householdRepository.initializeHouseholds(householdIds)

    val households = householdRepository.households.value

    assertEquals(1, households.size)
    assertEquals("house1", households[0].uid)
  }

  // Additional tests can be added for error cases and edge conditions

}
