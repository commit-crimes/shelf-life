package com.android.shelflife.model.household

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.android.shelfLife.model.invitations.InvitationViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HouseholdViewModelTest {

  @Mock private lateinit var householdViewModel: HouseholdViewModel

  @Mock private lateinit var repository: HouseHoldRepository

  @Mock private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel

  @Mock private lateinit var dataStore: DataStore<Preferences>

  // Use UnconfinedTestDispatcher for testing coroutines
  private val testDispatcher = UnconfinedTestDispatcher()

  @Mock private lateinit var firebaseAuth: FirebaseAuth

  @Mock private lateinit var firebaseUser: FirebaseUser

  private lateinit var firebaseAuthMock: MockedStatic<FirebaseAuth>
  private lateinit var invitationRepositoryFirestore: InvitationRepositoryFirestore
  private lateinit var invitationViewModel: InvitationViewModel

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    // Set the main dispatcher to the test dispatcher
    Dispatchers.setMain(testDispatcher)

    // Initialize Firebase
    FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

    // Mock FirebaseAuth.getInstance()
    firebaseAuthMock = mockStatic(FirebaseAuth::class.java)
    firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuth)
    invitationRepositoryFirestore = mock<InvitationRepositoryFirestore>()
    householdViewModel =
        HouseholdViewModel(
            repository, listFoodItemsViewModel, invitationRepositoryFirestore, dataStore)

    ShadowLog.clear() // to check Error Logs
  }

  @After
  fun tearDown() {
    // Reset the main dispatcher
    Dispatchers.resetMain()
    firebaseAuthMock.close()
  }

  // This test needs for the user to be logged in, no idea how to mock this
  /*
  @Test
  fun `init should load households`() = runTest {
    // Arrange
    val households = listOf(HouseHold("1", "Household 1", emptyList(), emptyList()))

    whenever(repository.getHouseholds(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<HouseHold>) -> Unit>(0)
      onSuccess(households)
      null
    }
    // Act
    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    // Assert
    assertEquals(households, householdViewModel.households.value)
    assertEquals(households.first(), householdViewModel.selectedHousehold.value)
    verify(listFoodItemsViewModel).setFoodItems(households.first().foodItems)
  }
   */

  @Test
  fun `selectHousehold should update selected household and food items`() = runTest {
    // Arrange
    val foodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(1.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT,
            nutritionFacts = NutritionFacts(energyKcal = 52))

    val foodItem =
        FoodItem(
            uid = "1",
            foodFacts = foodFacts,
            location = FoodStorageLocation.PANTRY,
            expiryDate = Timestamp.now(),
            status = FoodStatus.CLOSED)

    val household = HouseHold("1", "Household 1", emptyList(), listOf(foodItem))

    householdViewModel =
        HouseholdViewModel(
            repository, listFoodItemsViewModel, invitationRepositoryFirestore, dataStore)

    householdViewModel.setHouseholds(listOf(household))
    householdViewModel.selectHousehold(household)

    // Assert
    assertEquals(household, householdViewModel.selectedHousehold.value)
    verify(listFoodItemsViewModel).setFoodItems(household.foodItems)
  }

  /*@Test
  fun `addNewHousehold should add household and reload households`() = runTest {
    whenever(firebaseAuth.currentUser).thenReturn(mock(FirebaseUser::class.java))
    // Arrange
    val householdName = "New Household"
    val newUid = "uid"
    val newHousehold = HouseHold(newUid, householdName, emptyList(), emptyList())
    whenever(repository.getNewUid()).thenReturn(newUid)
    whenever(repository.addHousehold(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
      null
    }
    val households = listOf(newHousehold)
    whenever(repository.getHouseholds(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<HouseHold>) -> Unit>(0)
      onSuccess(households)
      null
    }

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    // Act
    householdViewModel.addNewHousehold(householdName)
    // Assert
    assertEquals(households, householdViewModel.households.value)
  }*/

  @Test
  fun `updateHousehold with new members should send invitations`() = runTest {
    // Arrange
    val oldHousehold = HouseHold("1", "Old Household", listOf("uid1"), emptyList())
    val updatedHousehold = oldHousehold.copy(members = listOf("uid1", "uid2"))

    householdViewModel.setHouseholds(listOf(oldHousehold))

    // Mock getUserEmails to return email for uid2
    whenever(repository.getUserEmails(eq(listOf("uid2")), any())).thenAnswer { invocation ->
      val callback = invocation.getArgument<(Map<String, String>) -> Unit>(1)
      callback(mapOf("uid2" to "user2@example.com"))
    }

    // Mock invitationRepository to confirm invitation is sent
    whenever(invitationRepositoryFirestore.sendInvitation(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    // Act
    householdViewModel.updateHousehold(updatedHousehold)

    // Assert
    // Verify that getUserEmails is called with the new member UID
    verify(repository).getUserEmails(eq(listOf("uid2")), any())

    // Verify that invitation is sent to the new member's email
    verify(invitationRepositoryFirestore)
        .sendInvitation(eq(updatedHousehold), eq("user2@example.com"), any(), any())
  }

  @Test
  fun `deleteHouseholdById should delete household and reload households`() = runTest {
    // Arrange
    val householdId = "1"

    whenever(repository.deleteHouseholdById(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
      null
    }
    val households = emptyList<HouseHold>()
    whenever(repository.getHouseholds(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<HouseHold>) -> Unit>(0)
      onSuccess(households)
      null
    }

    householdViewModel =
        HouseholdViewModel(
            repository, listFoodItemsViewModel, invitationRepositoryFirestore, dataStore)

    // Act
    householdViewModel.deleteHouseholdById(householdId)

    // Assert
    assertEquals(households, householdViewModel.households.value)
  }

  @Test
  fun `addFoodItem should call updateHousehold with updated household`() = runTest {
    // Arrange
    val foodItem = mock<FoodItem>()
    val household = HouseHold("1", "Household 1", emptyList(), emptyList())
    householdViewModel.setHouseholds(listOf(household))
    householdViewModel.selectHousehold(household)

    // Spy on householdViewModel to verify method calls
    val spyViewModel = spy(householdViewModel)

    // Act
    spyViewModel.addFoodItem(foodItem)

    // Assert
    val expectedHousehold = household.copy(foodItems = household.foodItems + foodItem)
    verify(spyViewModel).updateHousehold(expectedHousehold)
  }

  @Test
  fun `editFoodItem should call updateHousehold with updated household`() = runTest {
    // Arrange
    val oldFoodItem = mock<FoodItem>()
    val newFoodItem = mock<FoodItem>()
    val household = HouseHold("1", "Household 1", emptyList(), listOf(oldFoodItem))
    householdViewModel.setHouseholds(listOf(household))
    householdViewModel.selectHousehold(household)

    // Spy on householdViewModel
    val spyViewModel = spy(householdViewModel)

    // Act
    spyViewModel.editFoodItem(newFoodItem, oldFoodItem)

    // Assert
    val expectedFoodItems = household.foodItems - oldFoodItem + newFoodItem
    val expectedHousehold = household.copy(foodItems = expectedFoodItems)
    verify(spyViewModel).updateHousehold(expectedHousehold)
  }
  /*
    @Test
    fun `addNewHousehold logs error when user is not logged in`() = runTest {
      // Arrange
      val householdName = "Test Household"
      val friendEmails = emptyList<String>()

      whenever(firebaseAuth.currentUser).thenReturn(null)

      householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel, dataStore)

      // Act
      householdViewModel.addNewHousehold(householdName, friendEmails)

      // Assert
      // Verify that repository.addHousehold is not called
      verify(repository, never()).addHousehold(any(), any(), any())
      // Verify that loadHouseholds is called
      verify(repository).getHouseholds(any(), any())
      // Optionally, you can verify that an error is logged
    }
  */
  @Test
  fun `addNewHousehold adds household with current user when friendEmails is empty`() = runTest {
    // Arrange
    val householdName = "Test Household"
    val friendEmails = emptySet<String>()
    val userUid = "currentUserUid"
    val householdUid = "newHouseholdUid"

    whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
    whenever(firebaseUser.uid).thenReturn(userUid)
    whenever(repository.getNewUid()).thenReturn(householdUid)

    // Mock getUserIds to return empty map
    whenever(repository.getUserIds(eq(friendEmails), any())).thenAnswer { invocation ->
      val callback = invocation.getArgument<(Map<String, String>) -> Unit>(1)
      callback(emptyMap())
    }

    // Mock addHousehold
    whenever(repository.addHousehold(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Initialize the ViewModel
    householdViewModel =
        HouseholdViewModel(
            repository, listFoodItemsViewModel, invitationRepositoryFirestore, dataStore)

    // Act
    householdViewModel.addNewHousehold(householdName, friendEmails)

    // Assert
    // Capture the household passed to addHousehold
    val householdCaptor = argumentCaptor<HouseHold>()
    verify(repository).addHousehold(householdCaptor.capture(), any(), any())

    val expectedHousehold =
        HouseHold(
            uid = householdUid,
            name = householdName,
            members = listOf(userUid),
            foodItems = emptyList())

    assertEquals(expectedHousehold, householdCaptor.firstValue)
  }
  /*
    @Test
    fun `addNewHousehold adds household with friends when friendEmails are provided`() = runTest {
      // Arrange
      val householdName = "Test Household"
      val friendEmails = listOf("friend1@example.com", "friend2@example.com")
      val userUid = "currentUserUid"
      val householdUid = "newHouseholdUid"
      val friendUserIds =
          mapOf("friend1@example.com" to "friend1Uid", "friend2@example.com" to "friend2Uid")

      whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
      whenever(firebaseUser.uid).thenReturn(userUid)
      whenever(repository.getNewUid()).thenReturn(householdUid)

      // Mock getUserIds to return friendUserIds
      whenever(repository.getUserIds(eq(friendEmails), any())).thenAnswer { invocation ->
        val callback = invocation.getArgument<(Map<String, String>) -> Unit>(1)
        callback(friendUserIds)
      }

      // Mock addHousehold
      whenever(repository.addHousehold(any(), any(), any())).thenAnswer { invocation ->
        val onSuccess = invocation.getArgument<() -> Unit>(1)
        onSuccess()
      }

      // Initialize the ViewModel
      householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

      // Act
      householdViewModel.addNewHousehold(householdName, friendEmails)

      // Assert
      // Capture the household passed to addHousehold
      val householdCaptor = argumentCaptor<HouseHold>()
      verify(repository).addHousehold(householdCaptor.capture(), any(), any())

      val expectedMembers = listOf(userUid) + friendUserIds.values
      assertEquals(expectedMembers.sorted(), householdCaptor.firstValue.members.sorted())
    }

    @Test
    fun `addNewHousehold handles emails not found`() = runTest {
      // Arrange
      val householdName = "Test Household"
      val friendEmails = listOf("friend1@example.com", "friend2@example.com", "notfound@example.com")
      val userUid = "currentUserUid"
      val householdUid = "newHouseholdUid"
      val friendUserIds =
          mapOf("friend1@example.com" to "friend1Uid", "friend2@example.com" to "friend2Uid")

      whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
      whenever(firebaseUser.uid).thenReturn(userUid)
      whenever(repository.getNewUid()).thenReturn(householdUid)

      // Mock getUserIds to return friendUserIds (missing "notfound@example.com")
      whenever(repository.getUserIds(eq(friendEmails), any())).thenAnswer { invocation ->
        val callback = invocation.getArgument<(Map<String, String>) -> Unit>(1)
        callback(friendUserIds)
      }

      // Mock addHousehold
      whenever(repository.addHousehold(any(), any(), any())).thenAnswer { invocation ->
        val onSuccess = invocation.getArgument<() -> Unit>(1)
        onSuccess()
      }

      // Initialize the ViewModel
      householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

      // Act
      householdViewModel.addNewHousehold(householdName, friendEmails)

      // Assert
      // Capture the household passed to addHousehold
      val householdCaptor = argumentCaptor<HouseHold>()
      verify(repository).addHousehold(householdCaptor.capture(), any(), any())

      val expectedMembers = listOf(userUid) + friendUserIds.values
      assertEquals(expectedMembers.sorted(), householdCaptor.firstValue.members.sorted())
    }
  */
  @Test
  fun `addNewHousehold handles addHousehold failure`() = runTest {
    // Arrange
    val householdName = "Test Household"
    val friendEmails = emptySet<String>()
    val userUid = "currentUserUid"
    val householdUid = "newHouseholdUid"
    val exception = Exception("Add household failed")

    whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
    whenever(firebaseUser.uid).thenReturn(userUid)
    whenever(repository.getNewUid()).thenReturn(householdUid)

    // Mock getUserIds to return empty map
    whenever(repository.getUserIds(eq(friendEmails), any())).thenAnswer { invocation ->
      val callback = invocation.getArgument<(Map<String, String>) -> Unit>(1)
      callback(emptyMap())
    }

    // Mock addHousehold to fail
    whenever(repository.addHousehold(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
    }

    // Initialize the ViewModel
    householdViewModel =
        HouseholdViewModel(
            repository, listFoodItemsViewModel, invitationRepositoryFirestore, dataStore)

    // Act
    householdViewModel.addNewHousehold(householdName, friendEmails)
    // Optionally, check that an error is logged with the exception
  }

  @Test
  fun updateHousehold_shouldLogErrorWhenOldHouseholdNotFound() = runTest {
    // Arrange
    val household = HouseHold("non_existing_uid", "New Household", emptyList(), emptyList())
    householdViewModel.setHouseholds(emptyList()) // No households present

    // Act
    householdViewModel.updateHousehold(household)

    // Assert
    val logEntries = ShadowLog.getLogs()
    assertTrue(
        logEntries.any {
          it.tag == "HouseholdViewModel" &&
              it.msg == "Old household not found for UID: ${household.uid}"
        })
  }

  @Test
  fun deleteHousehold_shouldLogErrorWhenFails() = runTest {
    // Arrange
    val householdName = "household to delete"
    val exception = Exception("Test exception")

    whenever(repository.deleteHouseholdById(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
      null
    }
    // Act
    householdViewModel.deleteHouseholdById(householdName)

    // Verify that the error was logged
    val logEntries = ShadowLog.getLogs()
    assertTrue(
        logEntries.any {
          it.tag == "HouseholdViewModel" && it.msg == "Error deleting household: $exception"
        })
  }

  /**
   * Uses "hacky" reflection to test private method (Prof. Candea's suggestion:
   * https://edstem.org/eu/courses/1567/discussion/131808)
   * - Alex
   */
  @Test
  fun loadHouseholds_shouldLogErrorWhenLoadingFails() = runTest {
    // Arrange
    val exception = Exception("Test exception")

    whenever(repository.getHouseholds(any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(1)
      println("Triggering onFailure with exception: $exception")
      onFailure(exception)
      null
    }
    val method =
        HouseholdViewModel::class.java.declaredMethods.firstOrNull { it.name == "loadHouseholds" }
            ?: throw NoSuchMethodException("Method loadHouseholds not found")

    method.isAccessible = true

    method.invoke(householdViewModel)
    // Act

    // Assert
    val logEntries = ShadowLog.getLogs()
    assertTrue(
        logEntries.any {
          it.tag == "HouseholdViewModel" && it.msg == "Error loading households: $exception"
        })
  }

  @Test
  fun setHouseholds_shouldUpdateHouseholdsFlow() = runTest {
    val households = listOf(HouseHold("1", "Household 1", emptyList(), emptyList()))
    householdViewModel.setHouseholds(households)
    assertEquals(households, householdViewModel.households.value)
  }

  @Test
  fun setHouseholds_shouldHandleEmptyList() = runTest {
    val households = emptyList<HouseHold>()
    householdViewModel.setHouseholds(households)
    assertTrue(householdViewModel.households.value.isEmpty())
  }

  @Test
  fun checkIfHouseholdNameExists_shouldReturnTrueIfNameExists() = runTest {
    // Arrange
    val householdName = "Existing Household"
    val households = listOf(HouseHold("1", householdName, emptyList(), emptyList()))
    householdViewModel.setHouseholds(households)
    // Act
    val result = householdViewModel.checkIfHouseholdNameExists(householdName)
    // Assert
    assertTrue(result)
  }

  @Test
  fun checkIfHouseholdNameExists_shouldReturnFalseIfHouseholdsListIsEmpty() = runTest {
    // Arrange
    val householdName = "Any Household"
    householdViewModel.setHouseholds(emptyList())
    // Act
    val result = householdViewModel.checkIfHouseholdNameExists(householdName)
    // Assert
    assertFalse(result)
  }
}
