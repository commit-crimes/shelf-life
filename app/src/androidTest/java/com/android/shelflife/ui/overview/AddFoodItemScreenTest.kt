package com.android.shelflife.ui.overview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.overview.AddFoodItemScreen
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddFoodItemScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var foodItemViewModel: ListFoodItemsViewModel
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    // Initialize MockK
    MockKAnnotations.init(this, relaxed = true)

    householdViewModel = mockk(relaxed = true)
    foodItemViewModel = mockk(relaxed = true)
    navigationActions = mockk(relaxed = true)
  }

  @Test
  fun submitFormWithEmptyFoodNameShowsErrorMessage() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Leave foodName empty
    // Fill in other required fields
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("100")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31/12/2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("30/12/2024")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("29/12/2024")

    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()
    // Check that the error dialog is shown with the correct message
    composeTestRule.onNodeWithText("Food name cannot be empty.").assertIsDisplayed()
  }

  @Test
  fun submitFormWithEmptyAmountShowsErrorMessage() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Fill in foodName
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    // Leave amount empty
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31/12/2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("30/12/2024")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("29/12/2024")

    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()
    // Check that the error dialog is shown with the correct message
    composeTestRule.onNodeWithText("Amount cannot be empty.").assertIsDisplayed()
  }

  @Test
  fun submitFormWithInvalidAmountShowsErrorMessage() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Fill in fields
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("abc")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31/12/2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("30/12/2024")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("29/12/2024")

    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()
    // Check that the error dialog is shown with the correct message
    composeTestRule.onNodeWithText("Amount must be a number.").assertIsDisplayed()
  }

  @Test
  fun submitFormWithInvalidDateFormatShowsErrorMessage() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Fill in fields with invalid date format
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("100")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31-12-2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("30-12-2024")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("29-12-2024")

    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()
    // Check that the error dialog is shown with the correct message
    composeTestRule
        .onNodeWithText("Invalid date format. Please use dd/mm/yyyy.")
        .assertIsDisplayed()
  }

  @Test
  fun submitFormWithExpireDateBeforeOpenDateShowsErrorMessage() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Fill in fields with expireDate before openDate
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("100")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("29/12/2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("30/12/2024")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("28/12/2024")

    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()
      // Check that the error dialog is shown with the correct message
    composeTestRule
        .onNodeWithText("Expiration date cannot be before the open date.")
        .assertIsDisplayed()
  }

  @Test
  fun submitFormWithBuyDateAfterOpenOrExpireDateShowsErrorMessage() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Fill in fields with buyDate after openDate and expireDate
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("100")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("30/12/2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("29/12/2024")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("31/12/2024")

    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Check that the error dialog is shown with the correct message
    composeTestRule
        .onNodeWithText("Buy date cannot be after the open date or expiration date.")
        .assertIsDisplayed()
  }

  @Test
  fun submitFormWithMultipleErrorsShowsAllErrorMessages() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Leave foodName empty and amount invalid
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("abc")
    // Invalid date format
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31-12-2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("30-12-2024")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("31-12-2025")

    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Check that all error messages are displayed
    composeTestRule.onNodeWithText("Food name cannot be empty.").assertIsDisplayed()
    composeTestRule.onNodeWithText("Amount must be a number.").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Invalid date format. Please use dd/mm/yyyy.")
        .assertIsDisplayed()
  }
}
