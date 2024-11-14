// Import statements

import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.household.*
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
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

  @Before
  fun setup() {
    // Initialize mocks
    MockitoAnnotations.openMocks(this)
    // Set the main dispatcher to the test dispatcher
    Dispatchers.setMain(testDispatcher)

    // Initialize Firebase
    FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    ShadowLog.clear() // to check Error Logs
  }

  @After
  fun tearDown() {
    // Reset the main dispatcher
    Dispatchers.resetMain()
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

    householdViewModel = HouseholdViewModel(repository, listFoodItemsViewModel)

    householdViewModel.setHouseholds(listOf(household))
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

    householdViewModel.setHouseholds(listOf(household))
    householdViewModel.selectHousehold(household)

    // Act
    householdViewModel.addFoodItem(foodItem)

    // Assert
    verify(repository).updateHousehold(eq(updatedHousehold), any(), any())
    assertEquals(households, householdViewModel.households.value)
  }

  @Test
  fun `editFoodItem should remove old food item and add new food item to selected household and update it`() =
      runTest {
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

        val newFoodItem =
            FoodItem(
                uid = "2",
                foodFacts = foodFacts,
                location = FoodStorageLocation.FRIDGE,
                expiryDate = Timestamp.now(),
                status = FoodStatus.CLOSED)

        val household = HouseHold("1", "Household 1", emptyList(), List(1) { foodItem })

        val updatedHousehold =
            household.copy(foodItems = household.foodItems - foodItem + newFoodItem)
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

        householdViewModel.selectHousehold(household)

        // Act
        householdViewModel.editFoodItem(newFoodItem, foodItem)

        // Assert
        verify(repository).updateHousehold(eq(updatedHousehold), any(), any())
        assertEquals(households, householdViewModel.households.value)
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
