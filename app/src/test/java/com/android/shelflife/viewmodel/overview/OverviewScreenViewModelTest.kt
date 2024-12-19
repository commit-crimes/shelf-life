package com.android.shelflife.viewmodel.overview

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
@ExperimentalCoroutinesApi
class OverviewScreenViewModelTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private val testDispatcher = StandardTestDispatcher()

  @MockK private lateinit var mockHouseHoldRepository: HouseHoldRepository

  @MockK private lateinit var mockFoodItemRepository: FoodItemRepository

  @MockK private lateinit var mockUserRepository: UserRepository

  @MockK private lateinit var mockContext: Context

  private lateinit var viewModel: OverviewScreenViewModel

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    Dispatchers.setMain(testDispatcher)

    val householdFlow =
        MutableStateFlow<HouseHold?>(
            HouseHold(
                uid = "test-household-uid",
                name = "Test Household",
                members = listOf("user1", "user2"),
                sharedRecipes = emptyList(),
                ratPoints = emptyMap(),
                stinkyPoints = emptyMap()))
    every { mockHouseHoldRepository.selectedHousehold } returns householdFlow

    every { mockHouseHoldRepository.households } returns MutableStateFlow(emptyList())

    // Mock food item repository
    val foodItemFlow =
        MutableStateFlow(
            listOf(
                createMockFoodItem("item1", "Milk", FoodCategory.DAIRY),
                createMockFoodItem("item2", "Bread", FoodCategory.GRAIN)))
    every { mockFoodItemRepository.foodItems } returns foodItemFlow

    viewModel =
        OverviewScreenViewModel(
            mockHouseHoldRepository, mockFoodItemRepository, mockUserRepository, mockContext)
  }

  private fun createMockFoodItem(
      uid: String,
      name: String,
      category: FoodCategory,
      status: FoodStatus = FoodStatus.OPENED
  ) =
      FoodItem(
          uid = uid,
          owner = "user1",
          foodFacts =
              FoodFacts(name = name, category = category, quantity = Quantity(1.0, FoodUnit.COUNT)),
          status = status,
          expiryDate = null,
          openDate = null)

  @Test
  fun `toggleFilter adds and removes filter correctly`() = runTest {
    // Initially no filters
    assertTrue(viewModel.selectedFilters.first().isEmpty())

    // Add a filter
    viewModel.toggleFilter("Dairy")
    assertTrue(viewModel.selectedFilters.first().contains("Dairy"))

    // Remove the filter
    viewModel.toggleFilter("Dairy")
    assertFalse(viewModel.selectedFilters.first().contains("Dairy"))
  }

  @Test
  fun `changeQuery updates query state`() = runTest {
    // Initial query is empty
    assertTrue(viewModel.query.first().isEmpty())

    // Change query
    viewModel.changeQuery("cheese")
    assertEquals("cheese", viewModel.query.first())
  }

  @Test
  fun `selectFoodItem updates selected food item`() {
    val testItem = createMockFoodItem("test-id", "Test Food", FoodCategory.FRUIT)

    // Mock the repository's selectFoodItem method
    coEvery { mockFoodItemRepository.selectFoodItem(any()) } returns Unit

    viewModel.selectFoodItem(testItem)
    // Verify that the repository's method was called with the correct item
  }

  @Test
  fun `selectMultipleFoodItems works correctly`() = runTest {
    val testItem1 = createMockFoodItem("test-id-1", "Food 1", FoodCategory.FRUIT)
    val testItem2 = createMockFoodItem("test-id-2", "Food 2", FoodCategory.VEGETABLE)

    // Initially empty
    assertTrue(viewModel.multipleSelectedFoodItems.first().isEmpty())

    // Select first item
    viewModel.selectMultipleFoodItems(testItem1)
    assertEquals(1, viewModel.multipleSelectedFoodItems.first().size)
    assertTrue(viewModel.multipleSelectedFoodItems.first().contains(testItem1))

    // Select second item
    viewModel.selectMultipleFoodItems(testItem2)
    assertEquals(2, viewModel.multipleSelectedFoodItems.first().size)
    assertTrue(viewModel.multipleSelectedFoodItems.first().contains(testItem2))

    // Deselect first item
    viewModel.selectMultipleFoodItems(testItem1)
    assertEquals(1, viewModel.multipleSelectedFoodItems.first().size)
    assertFalse(viewModel.multipleSelectedFoodItems.first().contains(testItem1))
  }

  @Test
  fun `clearMultipleSelectedFoodItems works correctly`() = runTest {
    val testItem = createMockFoodItem("test-id", "Food", FoodCategory.FRUIT)

    // Select an item
    viewModel.selectMultipleFoodItems(testItem)
    assertFalse(viewModel.multipleSelectedFoodItems.first().isEmpty())

    // Clear selections
    viewModel.clearMultipleSelectedFoodItems()
    assertTrue(viewModel.multipleSelectedFoodItems.first().isEmpty())
  }

  @Test
  fun `selectHouseholdToEdit calls repository method`() = runTest {
    val testHousehold =
        HouseHold(
            uid = "test-household-uid",
            name = "Test Household",
            members = listOf("user1", "user2"),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    coEvery { mockHouseHoldRepository.selectHouseholdToEdit(any()) } returns Unit

    viewModel.selectHouseholdToEdit(testHousehold)
    // Verify repository method was called
    io.mockk.coVerify(exactly = 1) { mockHouseHoldRepository.selectHouseholdToEdit(testHousehold) }
  }

  @Test
  fun `checkItemStatus updates expired items and stinkyPoints`() = runTest {
    // Simulate items that should be expired
    val expiredTimestamp = Timestamp.now() // For test, assume now is expiry time
    val freshItem =
        createMockFoodItem("fresh", "Fresh Food", FoodCategory.FRUIT, FoodStatus.UNOPENED)
    val expiredItem =
        createMockFoodItem("expired", "Old Food", FoodCategory.MEAT, FoodStatus.UNOPENED)
            .copy(expiryDate = expiredTimestamp)

    // Mock flow of these items
    val mockFlow = MutableStateFlow(listOf(freshItem, expiredItem))
    every { mockFoodItemRepository.foodItems } returns mockFlow

    coEvery { mockFoodItemRepository.updateFoodItem(any(), any()) } returns Unit
    coEvery { mockHouseHoldRepository.updateStinkyPoints(any(), any()) } returns Unit

    // Re-initialize the ViewModel to trigger checkItemStatus again with our mocked items
    viewModel =
        OverviewScreenViewModel(
            mockHouseHoldRepository, mockFoodItemRepository, mockUserRepository, mockContext)

    // Give some time for collector to run
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify that the expired item got updated to EXPIRED status
    io.mockk.coVerify {
      mockFoodItemRepository.updateFoodItem(
          "test-household-uid", match { it.uid == "expired" && it.status == FoodStatus.EXPIRED })
    }

    // Verify stinky points updated for the owner of the expired item
    io.mockk.coVerify { mockHouseHoldRepository.updateStinkyPoints("test-household-uid", any()) }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
}
