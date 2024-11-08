// Import statements

import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.household.*
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HouseholdViewModelTest {

  @Mock private lateinit var householdViewModel: HouseholdViewModel

  @Mock private lateinit var repository: HouseHoldRepository

  @Mock private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel

  // Use UnconfinedTestDispatcher for testing coroutines
  private val testDispatcher = UnconfinedTestDispatcher()

  @Mock private lateinit var firebaseAuth: FirebaseAuth

  @Mock private lateinit var firebaseUser: FirebaseUser

  private lateinit var firebaseAuthMock: MockedStatic<FirebaseAuth>

  @Before
  fun setup() {
    // Initialize mocks
    MockitoAnnotations.openMocks(this)
    // Set the main dispatcher to the test dispatcher
    Dispatchers.setMain(testDispatcher)

    // Initialize Firebase
    FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

    // Mock FirebaseAuth.getInstance()
    firebaseAuthMock = mockStatic(FirebaseAuth::class.java)
    firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuth)

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    ShadowLog.clear() // to check Error Logs
  }

  @After
  fun tearDown() {
    // Reset the main dispatcher
    Dispatchers.resetMain()
    firebaseAuthMock.close()
  }

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

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    // Act
    householdViewModel.selectHousehold(household)

    // Assert
    assertEquals(household, householdViewModel.selectedHousehold.value)
    verify(listFoodItemsViewModel).setFoodItems(household.foodItems)
  }

  @Test
  fun `addNewHousehold should add household and reload households`() = runTest {
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
  }

  @Test
  fun `updateHousehold should update household and reload households`() = runTest {
    // Arrange
    val household = HouseHold("1", "Updated Household", emptyList(), emptyList())

    whenever(repository.updateHousehold(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
      null
    }
    val households = listOf(household)
    whenever(repository.getHouseholds(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<HouseHold>) -> Unit>(0)
      onSuccess(households)
      null
    }

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    // Act
    householdViewModel.updateHousehold(household)

    // Assert
    assertEquals(households, householdViewModel.households.value)
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

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    // Act
    householdViewModel.deleteHouseholdById(householdId)

    // Assert
    assertEquals(households, householdViewModel.households.value)
  }

  @Test
  fun `addFoodItem should add food item to selected household and update it`() = runTest {
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

    val household = HouseHold("1", "Household 1", emptyList(), emptyList())

    val updatedHousehold = household.copy(foodItems = household.foodItems + foodItem)
    val households = listOf(updatedHousehold)

    whenever(repository.updateHousehold(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
      null
    }
    whenever(repository.getHouseholds(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<HouseHold>) -> Unit>(0)
      onSuccess(households)
      null
    }

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)
    householdViewModel.selectHousehold(household)

    // Act
    householdViewModel.addFoodItem(foodItem)

    // Assert
    verify(repository).updateHousehold(eq(updatedHousehold), any(), any())
    assertEquals(households, householdViewModel.households.value)
  }

  @Test
  fun `addNewHousehold logs error when user is not logged in`() = runTest {
    // Arrange
    val householdName = "Test Household"
    val friendEmails = emptyList<String>()

    whenever(firebaseAuth.currentUser).thenReturn(null)

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    // Act
    householdViewModel.addNewHousehold(householdName, friendEmails)

    // Assert
    // Verify that repository.addHousehold is not called
    verify(repository, never()).addHousehold(any(), any(), any())
    // Verify that loadHouseholds is called
    verify(repository).getHouseholds(any(), any())
    // Optionally, you can verify that an error is logged
  }

  @Test
  fun `addNewHousehold adds household with current user when friendEmails is empty`() = runTest {
    // Arrange
    val householdName = "Test Household"
    val friendEmails = emptyList<String>()
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
    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

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

    // Verify that loadHouseholds is called
    verify(repository, atLeastOnce()).getHouseholds(any(), any())
  }

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

    // Verify that loadHouseholds is called
    verify(repository, atLeastOnce()).getHouseholds(any(), any())
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

    // Verify that loadHouseholds is called
    verify(repository, atLeastOnce()).getHouseholds(any(), any())
    // Optionally, check that a warning is logged about emails not found
  }

  @Test
  fun `addNewHousehold handles addHousehold failure`() = runTest {
    // Arrange
    val householdName = "Test Household"
    val friendEmails = emptyList<String>()
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
    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    // Act
    householdViewModel.addNewHousehold(householdName, friendEmails)

    // Assert
    // Verify that loadHouseholds is called
    verify(repository, atLeastOnce()).getHouseholds(any(), any())
    // Optionally, check that an error is logged with the exception
  }

  @Test
  fun addNewHousehold_shouldLogErrorWhenAddingFails() = runTest {
    // Arrange
    val householdName = "New Household"
    val exception = Exception("Test exception")

    whenever(repository.getNewUid()).thenReturn("uid")
    whenever(repository.addHousehold(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
      null
    }

    // Act
    householdViewModel.addNewHousehold(householdName)

    // Assert
    assertTrue(householdViewModel.households.value.isEmpty())
    // Verify that the error was logged
    val logEntries = ShadowLog.getLogs()
    assertTrue(
        logEntries.any {
          it.tag == "HouseholdViewModel" && it.msg == "Error adding household: $exception"
        })
  }

  @Test
  fun updateHousehold_shouldLogErrorWhenFails() = runTest {
    // Arrange
    val household = HouseHold("1", "New household", emptyList(), emptyList())
    val exception = Exception("Test exception")

    whenever(repository.updateHousehold(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
      null
    }
    // Act
    householdViewModel.updateHousehold(household)

    // Verify that the error was logged
    val logEntries = ShadowLog.getLogs()
    assertTrue(
        logEntries.any {
          it.tag == "HouseholdViewModel" && it.msg == "Error updating household: $exception"
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

  @Test
  fun factory_shouldCreateHouseholdViewModel() {
    val modelClass = HouseholdViewModel::class.java
    val viewModel = HouseholdViewModel.Factory.create(modelClass)
    assertTrue(viewModel is HouseholdViewModel)
  }
}
