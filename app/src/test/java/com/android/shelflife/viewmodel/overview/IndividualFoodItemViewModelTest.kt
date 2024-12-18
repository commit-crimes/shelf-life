package com.android.shelflife.viewmodel.overview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.overview.IndividualFoodItemViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class IndividualFoodItemViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  // Use a TestCoroutineDispatcher in older code, or StandardTestDispatcher with newer versions.
  private val testDispatcher = StandardTestDispatcher()

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var userRepository: UserRepository
  private lateinit var viewModel: IndividualFoodItemViewModel

  // State flows to represent repository states
  private val selectedFoodItemFlow = MutableStateFlow<FoodItem?>(null)
  private val userFlow = MutableStateFlow<User?>(null)

  @Before
  fun setUp() {
    // Set the main dispatcher to a test dispatcher
    Dispatchers.setMain(testDispatcher)

    foodItemRepository = mock { on { selectedFoodItem } doReturn selectedFoodItemFlow }

    userRepository = mock { on { user } doReturn userFlow }

    // Initialize the viewModel
    viewModel =
        IndividualFoodItemViewModel(
            foodItemRepository = foodItemRepository, userRepository = userRepository)
  }

  @Test
  fun `init sets selectedFood from repository`() {
    // Initially, selectedFood should be null
    assertNull(viewModel.selectedFood)

    // Now update the repository's selectedFoodItem flow
    val testFoodItem =
        FoodItem(
            uid = "food123",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "12345",
                    quantity = Quantity(100.0, FoodUnit.GRAM),
                    category = FoodCategory.FRUIT,
                    nutritionFacts = NutritionFacts(),
                    imageUrl = "test_url"),
            location = FoodStorageLocation.FRIDGE,
            expiryDate = Timestamp.now(),
            openDate = Timestamp.now(),
            buyDate = Timestamp.now(),
            status = FoodStatus.UNOPENED,
            owner = "userId")
    selectedFoodItemFlow.value = testFoodItem

    // Recreate the viewModel to trigger init block
    viewModel =
        IndividualFoodItemViewModel(
            foodItemRepository = foodItemRepository, userRepository = userRepository)

    // After init, selectedFood should match repository's value
    assertEquals(testFoodItem, viewModel.selectedFood)
  }

  @Test
  fun `deleteFoodItem with valid household and selectedFood calls repository delete`() = runTest {
    val testFoodItem =
        FoodItem(
            uid = "food123",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "12345",
                    quantity = Quantity(100.0, FoodUnit.GRAM),
                    category = FoodCategory.FRUIT,
                    nutritionFacts = NutritionFacts(),
                    imageUrl = "test_url"),
            location = FoodStorageLocation.FRIDGE,
            expiryDate = Timestamp.now(),
            openDate = Timestamp.now(),
            buyDate = Timestamp.now(),
            status = FoodStatus.UNOPENED,
            owner = "userUid")
    val testUser =
        User(
            uid = "userUid",
            username = "User",
            email = "user@example.com",
            selectedHouseholdUID = "household123")

    // Set up initial conditions
    selectedFoodItemFlow.value = testFoodItem
    userFlow.value = testUser

    // Recreate viewModel after flows have updated
    viewModel = IndividualFoodItemViewModel(foodItemRepository, userRepository)

    // Call deleteFoodItem
    viewModel.deleteFoodItem()

    // Advance coroutines
    advanceUntilIdle()

    // Verify that deleteFoodItem was called with correct arguments
    verify(foodItemRepository, times(1)).deleteFoodItem("household123", "food123")

    // Verify that the selectedFoodItem is set to null after deletion
    verify(foodItemRepository, times(1)).selectFoodItem(null)
  }

  @Test
  fun `deleteFoodItem does nothing if user has no selectedHouseholdUID`() = runTest {
    val testFoodItem =
        FoodItem(
            uid = "food123",
            foodFacts =
                FoodFacts(
                    name = "Apple",
                    barcode = "12345",
                    quantity = Quantity(100.0, FoodUnit.GRAM),
                    category = FoodCategory.FRUIT,
                    nutritionFacts = NutritionFacts(),
                    imageUrl = "test_url"),
            location = FoodStorageLocation.FRIDGE,
            expiryDate = Timestamp.now(),
            openDate = Timestamp.now(),
            buyDate = Timestamp.now(),
            status = FoodStatus.UNOPENED,
            owner = "userId")
    // User has null selectedHouseholdUID
    val testUser =
        User(
            uid = "userUid",
            username = "User",
            email = "user@example.com",
            selectedHouseholdUID = null)

    selectedFoodItemFlow.value = testFoodItem
    userFlow.value = testUser

    viewModel = IndividualFoodItemViewModel(foodItemRepository, userRepository)

    // Call deleteFoodItem
    viewModel.deleteFoodItem()

    advanceUntilIdle()

    // Verify that deleteFoodItem and selectFoodItem were NOT called
    verify(foodItemRepository, never()).deleteFoodItem(any(), any())
    verify(foodItemRepository, never()).selectFoodItem(any())
  }

  @Test
  fun `deleteFoodItem does nothing if selectedFood is null`() = runTest {
    val testUser =
        User(
            uid = "userUid",
            username = "User",
            email = "user@example.com",
            selectedHouseholdUID = "household123")

    selectedFoodItemFlow.value = null
    userFlow.value = testUser

    viewModel = IndividualFoodItemViewModel(foodItemRepository, userRepository)

    // Call deleteFoodItem
    viewModel.deleteFoodItem()

    advanceUntilIdle()

    // Verify that deleteFoodItem and selectFoodItem were NOT called
    verify(foodItemRepository, never()).deleteFoodItem(any(), any())
    verify(foodItemRepository, never()).selectFoodItem(any())
  }
}
