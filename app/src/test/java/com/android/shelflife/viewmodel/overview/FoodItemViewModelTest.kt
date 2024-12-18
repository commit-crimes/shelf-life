package com.android.shelflife.viewmodel.overview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class FoodItemViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  @Mock private lateinit var foodItemRepository: FoodItemRepository

  @Mock private lateinit var userRepository: UserRepository

  @Mock private lateinit var foodFactsRepository: FoodFactsRepository

  private lateinit var viewModel: FoodItemViewModel

  private val userFlow =
      MutableStateFlow<User?>(
          User(
              uid = "userId",
              username = "TestUser",
              email = "test@example.com",
              photoUrl = "",
              selectedHouseholdUID = "householdId",
              householdUIDs = listOf("householdId"),
              recipeUIDs = listOf(),
              invitationUIDs = listOf()))

  private val selectedFoodFlow = MutableStateFlow<FoodItem?>(null)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    `when`(userRepository.user).thenReturn(userFlow)
    `when`(foodItemRepository.selectedFoodItem).thenReturn(selectedFoodFlow)
    viewModel = FoodItemViewModel(foodItemRepository, userRepository, foodFactsRepository)
  }

  @Test
  fun `validate fields when submit button clicked`() {
    viewModel.foodName = ""
    viewModel.amount = "abc"
    viewModel.buyDate = "2023-01-01"
    viewModel.expireDate = "2022-12-31"

    viewModel.validateAllFieldsWhenSubmitButton()

    assertNotNull(viewModel.foodNameErrorResId)
    assertNotNull(viewModel.amountErrorResId)
    assertNotNull(viewModel.expireDateErrorResId)
    assertNull(viewModel.openDateErrorResId)
  }

  @Test
  fun `change food name triggers validation`() {
    viewModel.changeFoodName("New Name")
    assertEquals("New Name", viewModel.foodName)
    assertNull(viewModel.foodNameErrorResId)
  }

  @Test
  fun `change amount triggers validation`() {
    viewModel.changeAmount("123")
    assertEquals("123", viewModel.amount)
    assertNull(viewModel.amountErrorResId)
  }

  @Test
  fun `reset for scanner`() {
    viewModel.resetForScanner()

    assertEquals(FoodStorageLocation.PANTRY, viewModel.location)
    assertEquals("", viewModel.expireDate)
    assertEquals("", viewModel.openDate)
    assertNotNull(viewModel.buyDate)
    assertNull(viewModel.expireDateErrorResId)
    assertNull(viewModel.openDateErrorResId)
    assertNull(viewModel.buyDateErrorResId)
  }

  @Test
  fun `submit food item fails validation`() = runTest {
    viewModel.foodName = ""
    viewModel.amount = "abc"
    viewModel.buyDate = "2023-01-01"
    viewModel.expireDate = "2022-12-31"

    val result = viewModel.submitFoodItem()

    assertFalse(result)
  }

  @Test
  fun `init block sets fields if selectedFood is not null`() {
    viewModel = FoodItemViewModel(foodItemRepository, userRepository, foodFactsRepository)
    val foodItem =
        FoodItem(
            uid = "food1",
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
    selectedFoodFlow.value = foodItem
    // Recreate viewModel to trigger init block again
    viewModel = FoodItemViewModel(foodItemRepository, userRepository, foodFactsRepository)

    assertTrue(viewModel.isSelected)
    assertEquals("Apple", viewModel.foodName)
    assertEquals("100.0", viewModel.amount)
    assertEquals(FoodUnit.GRAM, viewModel.unit)
    assertEquals(FoodCategory.FRUIT, viewModel.category)
    assertEquals(FoodStorageLocation.FRIDGE, viewModel.location)
    assertTrue(viewModel.expireDate.isNotEmpty())
    assertTrue(viewModel.openDate.isNotEmpty())
    assertTrue(viewModel.buyDate.isNotEmpty())
  }

  @Test
  fun `isScanned sets isScanned to true`() {
    viewModel = FoodItemViewModel(foodItemRepository, userRepository, foodFactsRepository)
    assertFalse(viewModel.isScanned)
    viewModel.isScanned()
    assertTrue(viewModel.isScanned)
  }

  @Test
  fun `changeBuyDate updates buyDate and revalidates dependent fields`() {
    viewModel.buyDate = "20220101" // Valid buy date
    viewModel.expireDate = "20221231" // Expire after buy date
    viewModel.openDate = "20220501" // Open date between buy and expire date

    viewModel.changeBuyDate("2022-01-01")
    assertEquals("20220101", viewModel.buyDate)
  }
}
