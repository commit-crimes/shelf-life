package com.android.shelflife.ui.overview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
import com.android.shelfLife.ui.overview.EditFoodItemScreen
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.google.firebase.Timestamp
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class EditFoodItemScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
    foodItemRepository = mock(FoodItemRepository::class.java)
    foodItemViewModel = ListFoodItemsViewModel(foodItemRepository)

    foodItemViewModel.selectFoodItem(foodItem)
    `when`(foodItemRepository.getNewUid()).thenReturn("testUID")
    every { houseHoldViewModel.editFoodItem(any(), any()) } just runs
  }

  @Test
  fun testInitialUIComponentsDisplayed() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Verify that all input fields are displayed
    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodAmount"))
    composeTestRule.onNodeWithTag("editFoodAmount").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodUnit"))
    composeTestRule.onNodeWithTag("editFoodUnit").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodLocation"))
    composeTestRule.onNodeWithTag("editFoodLocation").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodExpireDate"))
    composeTestRule.onNodeWithTag("editFoodExpireDate").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodOpenDate"))
    composeTestRule.onNodeWithTag("editFoodOpenDate").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodBuyDate"))
    composeTestRule.onNodeWithTag("editFoodBuyDate").assertIsDisplayed()

    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("cancelButton"))
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
  }

  @Test
  fun testAmountFieldValidation() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Enter invalid amount (letters)
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("abc")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Amount must be a number.").assertIsDisplayed()

    // Enter invalid amount (negative number)
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("-5")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Amount must be positive.").assertIsDisplayed()

    // Enter valid amount
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("10")
    // Verify error messages are gone
    composeTestRule.onNodeWithText("Amount must be a number.").assertDoesNotExist()
    composeTestRule.onNodeWithText("Amount must be positive.").assertDoesNotExist()
  }

  @Test
  fun testLocationDropdownSelection() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Open the location dropdown
    composeTestRule.onNodeWithTag("editFoodLocation").performClick()
    // Select a location
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    // Verify the selected location
    composeTestRule.onNodeWithTag("dropdownMenu_Select location").assertTextContains("Pantry")
  }

  @Test
  fun testDateFieldsValidation() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Enter invalid expire date
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("31132023") // Invalid date
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Invalid date").assertIsDisplayed()

    // Enter valid expire date
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule
        .onNodeWithTag("editFoodExpireDate")
        .performTextInput("31122030") // Valid future date
    // Verify error message is gone
    composeTestRule.onNodeWithText("Invalid date").assertDoesNotExist()
  }

  @Test
  fun testSubmitButtonWithInvalidForm() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    // Scroll to the submit button
    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    // Click the submit button without filling the form
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Verify that error messages are displayed for required fields
    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodAmount"))
    composeTestRule.onNodeWithText("Amount cannot be empty.").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodExpireDate"))
    composeTestRule.onNodeWithText("Date cannot be empty").assertIsDisplayed()
  }

  @Test
  fun testSubmitButtonWithValidForm() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Fill in valid inputs
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("editFoodLocation").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("31122030") // Future date
    // Clear and re-enter buy date to ensure it's valid
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextClearance()
    composeTestRule
        .onNodeWithTag("editFoodBuyDate")
        .performTextInput(formatTimestampToDate(Timestamp.now()))

    // Scroll to the submit button
    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Verify that the editFoodItem function was called
    verify { houseHoldViewModel.editFoodItem(any(), any()) }

    // Verify that navigation action was called
    verify { navigationActions.goBack() }
  }

  @Test
  fun testOpenDateValidationAgainstBuyAndExpireDates() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Enter buy date
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextInput("01012026")

    // Enter expire date
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("31122026")

    // Enter invalid open date (before buy date)
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextInput("31122025")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Open Date cannot be before Buy Date").assertIsDisplayed()

    // Enter valid open date (after buy date)
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextInput("01022026")
    // Verify error message is gone
    composeTestRule.onNodeWithText("Open Date cannot be before Buy Date").assertDoesNotExist()

    // Enter invalid open date (after expire date)
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextInput("01012027")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Open Date cannot be after Expire Date").assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Click the back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Verify that the navigation action was called
    verify { navigationActions.goBack() }
  }

  @Test
  fun testNavigationCancelButton() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Click the back button
    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("cancelButton"))
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    // Verify that the navigation action was called
    verify { navigationActions.goBack() }
  }
}
