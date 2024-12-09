package com.android.shelflife.viewmodel

import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.FoodSearchInput
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodFacts.SearchStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertTrue
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
    TestWatcher() {

  override fun starting(description: Description) {
    Dispatchers.setMain(dispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}

class FakeFoodFactsRepository : FoodFactsRepository {

  var shouldReturnError = false
  var foodFactsList = listOf<FoodFacts>()

  override fun searchFoodFacts(
      searchInput: FoodSearchInput,
      onSuccess: (List<FoodFacts>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (shouldReturnError) {
      onFailure(Exception("Test exception"))
    } else {
      onSuccess(foodFactsList)
    }
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FoodFactsViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModel: FoodFactsViewModel
  private lateinit var repository: FakeFoodFactsRepository

  @Before
  fun setup() {
    repository = FakeFoodFactsRepository()
    viewModel = FoodFactsViewModel(repository)
  }

  @Test
  fun `searchByBarcode success`() = runTest {
    // Given
    val barcode = 123456789L
    val foodFactsList =
        listOf(
            FoodFacts(
                name = "Apple",
                barcode = barcode.toString(),
                quantity = Quantity(1.0, FoodUnit.GRAM),
                category = FoodCategory.FRUIT,
                nutritionFacts =
                    NutritionFacts(
                        energyKcal = 52,
                        fat = 0.2,
                        carbohydrates = 14.0,
                        proteins = 0.3,
                        salt = 0.0)))
    repository.foodFactsList = foodFactsList
    repository.shouldReturnError = false

    // Act
    viewModel.searchByBarcode(barcode)

    // Assert
    assertEquals(SearchStatus.Success, viewModel.searchStatus.value)
    assertEquals(foodFactsList, viewModel.foodFactsSuggestions.value)
  }

  @Test
  fun `searchByBarcode failure`() = runTest {
    // Given
    val barcode = 123456789L
    repository.shouldReturnError = true

    // Act
    viewModel.searchByBarcode(barcode)

    // Assert
    assertEquals(SearchStatus.Failure, viewModel.searchStatus.value)
    assertEquals(emptyList<FoodFacts>(), viewModel.foodFactsSuggestions.value)
  }

  @Test
  fun `resetSearchStatus sets status to Idle`() {
    // Act
    viewModel.resetSearchStatus()

    // Assert
    assertEquals(SearchStatus.Idle, viewModel.searchStatus.value)
  }

  @Test
  fun `searchByQuery success`() = runTest {
    // Given
    val query = "Apple"
    val foodFactsList =
        listOf(
            FoodFacts(
                name = "Apple",
                barcode = "123456789",
                quantity = Quantity(1.0, FoodUnit.GRAM),
                category = FoodCategory.FRUIT,
                nutritionFacts =
                    NutritionFacts(
                        energyKcal = 52,
                        fat = 0.2,
                        carbohydrates = 14.0,
                        proteins = 0.3,
                        salt = 0.0)))
    repository.foodFactsList = foodFactsList
    repository.shouldReturnError = false

    // Act
    viewModel.searchByQuery(query)

    // Assert
    assertEquals(foodFactsList, viewModel.foodFactsSuggestions.value)
    assertEquals(query, viewModel.query.value)
  }

  @Test
  fun `searchByQuery failure`() = runTest {
    // Given
    val query = "Unknown"
    repository.shouldReturnError = true

    // Act
    viewModel.searchByQuery(query)

    // Assert
    assertEquals(emptyList<FoodFacts>(), viewModel.foodFactsSuggestions.value)
    assertEquals(query, viewModel.query.value)
  }

  @Test
  fun `Factory creates FoodFactsViewModel instance`() {
    val factory = FoodFactsViewModel.Factory(repository)
    val viewModel = factory.create(FoodFactsViewModel::class.java)

    assertTrue(
        "Factory should create an instance of FoodFactsViewModel", viewModel is FoodFactsViewModel)
  }

  @Test
  fun `Factory throws exception for unknown ViewModel class`() {
    val factory = FoodFactsViewModel.Factory(repository)

    try {
      // Attempt to create a ViewModel of a different class to trigger the exception
      factory.create(DifferentViewModel::class.java)
      fail("Factory should throw IllegalArgumentException for unknown ViewModel class")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message?.contains("Unknown ViewModel class") == true)
    }
  }

  // A dummy ViewModel class to test the exception scenario
  class DifferentViewModel : ViewModel()
}
