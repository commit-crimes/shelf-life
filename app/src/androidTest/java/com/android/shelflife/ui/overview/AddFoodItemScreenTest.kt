package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddFoodItemScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var foodItemViewModel: ListFoodItemsViewModel
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    householdViewModel = mockk(relaxed = true)
    foodItemViewModel = mockk(relaxed = true)
    navigationActions = mockk(relaxed = true)
  }

  @Test
  fun displayAllComponents() {
    // Set up the screen
    composeTestRule.setContent {
      AddFoodItemScreen(
        navigationActions = navigationActions,
        houseHoldViewModel = householdViewModel,
        foodItemViewModel = foodItemViewModel
      )
    }

    // Assert that all UI elements are displayed
    composeTestRule.onNodeWithTag("addScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodItemTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodAmount").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodUnit").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodCategory").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodLocation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").assertIsDisplayed()
  }

  @Test
  fun doesNotSubmitWithInvalidDate() {
    // Set up the screen
    composeTestRule.setContent {
      AddFoodItemScreen(
        navigationActions = navigationActions,
        houseHoldViewModel = householdViewModel,
        foodItemViewModel = foodItemViewModel
      )
    }

    // Input invalid expiration date
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("invalid-date")

    // Attempt to submit
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Assert that the error dialog is displayed
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()

    // Verify that addFoodItem was not called
    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun doesNotSubmitWithNoFoodName() {
    // Set up the screen
    composeTestRule.setContent {
      AddFoodItemScreen(
        navigationActions = navigationActions,
        houseHoldViewModel = householdViewModel,
        foodItemViewModel = foodItemViewModel
      )
    }

    // Clear the food name input
    composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()

    // Attempt to submit
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Assert that the error dialog is displayed
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()

    // Verify that addFoodItem was not called
    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun doesNotSubmitWithNoAmount() {
    // Set up the screen
    composeTestRule.setContent {
      AddFoodItemScreen(
        navigationActions = navigationActions,
        houseHoldViewModel = householdViewModel,
        foodItemViewModel = foodItemViewModel
      )
    }

    // Clear the amount input
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()

    // Attempt to submit
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Assert that the error dialog is displayed
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()

    // Verify that addFoodItem was not called
    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

//  @Test
//  fun submitWithValidData() {
//    // Mock getUID method if necessary
//    every { foodItemViewModel.getUID() } returns "testUID"
//
//    // Set up the screen
//    composeTestRule.setContent {
//      AddFoodItemScreen(
//        navigationActions = navigationActions,
//        houseHoldViewModel = householdViewModel,
//        foodItemViewModel = foodItemViewModel
//      )
//    }
//
//    // Input valid data
//    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
//    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
//    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31/12/2023")
//    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01/12/2023")
//    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("30/11/2023")
//
//    // Submit the form
//    composeTestRule.onNodeWithTag("foodSave").performClick()
//
//    // Assert that the error dialog is not displayed
//    composeTestRule.onAllNodesWithTag("errorDialog").assertCountEquals(0)
//
//    // Verify that addFoodItem was called
//    verify(exactly = 1) { householdViewModel.addFoodItem(any()) }
//
//    // Verify that navigationActions.goBack() was called
//    verify { navigationActions.goBack() }
//  }

  @Test
  fun showsErrorMessageWhenBuyDateIsAfterOpenDateOrExpireDate() {
    // Set up the screen
    composeTestRule.setContent {
      AddFoodItemScreen(
        navigationActions = navigationActions,
        houseHoldViewModel = householdViewModel,
        foodItemViewModel = foodItemViewModel
      )
    }

    // Input data where buy date is after open date and expire date
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("01/01/2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01/01/2023")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31/12/2023")

    // Submit the form
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Assert that the error dialog is displayed
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()

    // Verify that addFoodItem was not called
    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun showsErrorMessageOnInvalidInput() {
    // Set up the screen
    composeTestRule.setContent {
      AddFoodItemScreen(
        navigationActions = navigationActions,
        houseHoldViewModel = householdViewModel,
        foodItemViewModel = foodItemViewModel
      )
    }

    // Input invalid amount
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("invalid")

    // Attempt to submit
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Assert that the error dialog is displayed
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()

    // Verify that addFoodItem was not called
    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }
}
