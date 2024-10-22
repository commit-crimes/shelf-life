// Import statements
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.*
import com.android.shelfLife.model.household.*
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HouseholdViewModelTest {

  private lateinit var householdViewModel: HouseholdViewModel

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
  }

  @After
  fun tearDown() {
    // Reset the main dispatcher
    Dispatchers.resetMain()
  }

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
}
