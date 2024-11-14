package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class IndividualFoodItemScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var houseHoldViewModel: HouseholdViewModel
  private lateinit var foodItemViewModel: ListFoodItemsViewModel
  private lateinit var foodItemRepository: FoodItemRepository

  private val foodItem =
      FoodItem(
          uid = "1",
          foodFacts =
              FoodFacts(
                  name = "Apple",
                  barcode = "123456789",
                  quantity = Quantity(1.0, FoodUnit.COUNT),
                  category = FoodCategory.FRUIT,
                  nutritionFacts = NutritionFacts(energyKcal = 52)),
          location = FoodStorageLocation.PANTRY,
          expiryDate = Timestamp.now(),
          status = FoodStatus.CLOSED)

  @Before
  fun setUp() {
    // Initialize MockK
    MockKAnnotations.init(this, relaxed = true)

    navigationActions = mockk(relaxed = true)
    houseHoldViewModel = mockk(relaxed = true)
    foodItemRepository = Mockito.mock(FoodItemRepository::class.java)
    foodItemViewModel = ListFoodItemsViewModel(foodItemRepository)

    foodItemViewModel.selectFoodItem(foodItem)
    `when`(foodItemRepository.getNewUid()).thenReturn("testUID")
    every { houseHoldViewModel.editFoodItem(any(), any()) } just runs
  }

  @Test
  fun testSetSelectedFoodItemById() = runTest {
    // Assert that the selected food item is set correctly
    val selectedFoodItem = foodItemViewModel.selectedFoodItem.value
    assert(selectedFoodItem!!.uid == "1")
    assert(selectedFoodItem.foodFacts.name == "Apple")
  }

  @Test
  fun individualFoodItemScreenDisplaysCorrectly() = runTest {
    composeTestRule.setContent {
      IndividualFoodItemScreen(
          navigationActions = navigationActions, foodItemViewModel = foodItemViewModel)
    }

    // Check if the screen displays the correct food item details
    composeTestRule.onNodeWithTag("IndividualFoodItemName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("IndividualFoodItemName").assertTextEquals("Apple")
    composeTestRule.onNodeWithTag("IndividualFoodItemImage").assertIsDisplayed()
  }

  @Test
  fun individualFoodItemScreenShowsLoadingIndicatorWhenNoFoodItemSelected() = runTest {
    // Ensure no food item is selected
    foodItemViewModel.selectFoodItem(null)

    composeTestRule.setContent {
      IndividualFoodItemScreen(
          navigationActions = navigationActions, foodItemViewModel = foodItemViewModel)
    }

    // Verify that the loading indicator is displayed
    composeTestRule.onNodeWithTag("CircularProgressIndicator").assertIsDisplayed()
  }

  @Test
  fun testBackButtonNavigatesBack() = runTest {
    composeTestRule.setContent {
      IndividualFoodItemScreen(
          navigationActions = navigationActions, foodItemViewModel = foodItemViewModel)
    }

    // Perform click on the back button
    composeTestRule.onNodeWithTag("IndividualTestScreenGoBack").performClick()

    // Verify that the navigation action was triggered
    io.mockk.verify { navigationActions.goBack() }
  }
}
