package com.android.shelflife.model.foodItem

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.google.firebase.Timestamp
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

// Stub Log.e and other Log methods to do nothing

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ListFoodItemsViewModelTest {
  private lateinit var viewModel: ListFoodItemsViewModel
  private val mockRepository: FoodItemRepository = mock()

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  private val testFoodItem =
      FoodItem(
          uid = "test-uid",
          foodFacts =
              FoodFacts(
                  name = "Test Food",
                  barcode = "1234567890",
                  quantity = Quantity(500.0, FoodUnit.GRAM),
                  category = FoodCategory.SNACK,
                  nutritionFacts =
                      NutritionFacts(
                          energyKcal = 250,
                          fat = 10.0,
                          saturatedFat = 5.0,
                          carbohydrates = 30.0,
                          sugars = 15.0,
                          proteins = 5.0,
                          salt = 0.5)),
          location = FoodStorageLocation.PANTRY,
          expiryDate = Timestamp.now(),
          openDate = null,
          buyDate = Timestamp.now(),
          status = FoodStatus.CLOSED)

  @Before
  fun setUp() {
    // Mock the init method correctly (only one parameter)
    whenever(mockRepository.init(any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(0)
      onSuccess()
    }
    whenever(mockRepository.getNewUid()).thenReturn("test-uid")
    // Initialize the ViewModel
    viewModel = ListFoodItemsViewModel(mockRepository)
    ShadowLog.clear()
  }

  @Test
  fun `init should call repository init`() = runTest {
    // Verify that init is called during ViewModel initialization
    verify(mockRepository).init(any())
  }

  @Test
  fun `getUID should return repository getNewUid result`() {
    // Arrange
    val expectedUid = "unique-id-123"
    whenever(mockRepository.getNewUid()).thenReturn(expectedUid)

    // Act
    val uid = viewModel.getUID()

    // Assert
    assertEquals(expectedUid, uid)
  }

  @Test
  fun `getFoodItems should update foodItems StateFlow on success`() = runTest {
    // Arrange
    val foodItemList = listOf(testFoodItem)
    whenever(mockRepository.getFoodItems(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<FoodItem>) -> Unit>(0)
      onSuccess(foodItemList)
    }

    // Act
    viewModel.getAllFoodItems()

    // Assert
    val result = viewModel.foodItems.first()
    assertEquals(foodItemList, result)
  }

  @Test
  fun `setFoodItems should update foodItems StateFlow`() = runTest {
    // Arrange
    val foodItemList = listOf(testFoodItem)

    // Act
    viewModel.setFoodItems(foodItemList)

    // Assert
    val result = viewModel.foodItems.first()
    assertEquals(foodItemList, result)
  }

  @Test
  fun `addFoodItem should call repository addFoodItem and update foodItems on success`() = runTest {
    // Arrange
    val updatedFoodItemList = listOf(testFoodItem)
    whenever(mockRepository.addFoodItem(eq(testFoodItem), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
    whenever(mockRepository.getFoodItems(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<FoodItem>) -> Unit>(0)
      onSuccess(updatedFoodItemList)
    }

    // Act
    viewModel.addFoodItem(testFoodItem)

    // Assert
    verify(mockRepository).addFoodItem(eq(testFoodItem), any(), any())
    val result = viewModel.foodItems.first()
    assertEquals(updatedFoodItemList, result)
  }

  @Test
  fun `updateFoodItem should call repository updateFoodItem and refresh foodItems`() = runTest {
    // Arrange
    val updatedFoodItem = testFoodItem.copy(status = FoodStatus.OPEN)
    val updatedFoodItemList = listOf(updatedFoodItem)
    whenever(mockRepository.updateFoodItem(eq(updatedFoodItem), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
    whenever(mockRepository.getFoodItems(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<FoodItem>) -> Unit>(0)
      onSuccess(updatedFoodItemList)
    }

    // Act
    viewModel.updateFoodItem(updatedFoodItem)

    // Assert
    verify(mockRepository).updateFoodItem(eq(updatedFoodItem), any(), any())
    val result = viewModel.foodItems.first()
    assertEquals(updatedFoodItemList, result)
  }

  @Test
  fun `deleteFoodItemById should call repository deleteFoodItemById and refresh foodItems`() =
      runTest {
        // Arrange
        val foodItemId = testFoodItem.uid
        val updatedFoodItemList = emptyList<FoodItem>()
        whenever(mockRepository.deleteFoodItemById(eq(foodItemId), any(), any())).thenAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
        }
        whenever(mockRepository.getFoodItems(any(), any())).thenAnswer {
          val onSuccess = it.getArgument<(List<FoodItem>) -> Unit>(0)
          onSuccess(updatedFoodItemList)
        }

        // Act
        viewModel.deleteFoodItemById(foodItemId)

        // Assert
        verify(mockRepository).deleteFoodItemById(eq(foodItemId), any(), any())
        val result = viewModel.foodItems.first()
        assertEquals(updatedFoodItemList, result)
      }

  @Test
  fun `selectFoodItem should update selectedFoodItem StateFlow`() = runTest {
    // Act
    viewModel.selectFoodItem(testFoodItem)

    // Assert
    val result = viewModel.selectedFoodItem.first()
    assertEquals(testFoodItem, result)
  }

  @Test
  fun `getFoodItems calls _onFail when repository fails`() {
    // Arrange
    val exception = Exception("Test Exception")

    // Set up repository to trigger onFailure callback
    whenever(mockRepository.getFoodItems(any(), any())).thenAnswer {
      // Call the onFailure lambda directly with an exception
      it.getArgument<(Exception) -> Unit>(1).invoke(exception)
    }

    // Act
    viewModel.getAllFoodItems()

    // Assert that a log entry with Log.e was created
    val logEntries = ShadowLog.getLogs()
    assertTrue(
        logEntries.any {
          it.tag == "ListFoodItemsViewModel" &&
              it.type == Log.ERROR &&
              it.msg.contains("Error fetching FoodItems: $exception")
        })
  }

  @Test
  fun `Factory creates ListFoodItemsViewModel instance`() {
    val factory = ListFoodItemsViewModel.Factory(mockRepository)
    val viewModel = factory.create(ListFoodItemsViewModel::class.java)

    assertTrue(
        "Factory should create an instance of FoodFactsViewModel",
        viewModel is ListFoodItemsViewModel)
  }

  @Test
  fun `Factory throws exception for unknown ViewModel class`() {
    val factory = ListFoodItemsViewModel.Factory(mockRepository)

    try {
      // Attempt to create a ViewModel of a different class to trigger the exception
      factory.create(DifferentViewModel::class.java)
      TestCase.fail("Factory should throw IllegalArgumentException for unknown ViewModel class")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message?.contains("Unknown ViewModel class") == true)
    }
  }

  // A dummy ViewModel class to test the exception scenario
  class DifferentViewModel : ViewModel()
}
