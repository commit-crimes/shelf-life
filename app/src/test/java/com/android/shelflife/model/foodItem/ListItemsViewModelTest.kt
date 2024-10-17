package com.android.shelflife.model.foodItem

import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

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
  }

  @Test
  fun `init should call repository init and getFoodItems`() = runTest {
    // Verify that init and getFoodItems are called during ViewModel initialization
    verify(mockRepository).init(any())
    verify(mockRepository).getFoodItems(any(), any())
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
    viewModel.getFoodItems()

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
}
