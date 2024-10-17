package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddFoodItemScreenTest {
  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var foodItemViewModel: ListFoodItemsViewModel
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    householdViewModel = mockk(relaxed = true)
    foodItemViewModel = mockk(relaxed = true)
    navigationActions = mockk(relaxed = true)

    every { navigationActions.currentRoute() } returns Screen.ADD_FOOD
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      AddFoodItemScreen(navigationActions, householdViewModel, foodItemViewModel)
    }

    composeTestRule.waitForIdle()  // Wait for the UI to be fully rendered

    composeTestRule.onNodeWithTag("addScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodItemTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodItemTitle").assertTextEquals("Add Food Item")
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSave").assertTextEquals("Submit")

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
    composeTestRule.setContent {
      AddFoodItemScreen(navigationActions, householdViewModel, foodItemViewModel)
    }

    composeTestRule.waitForIdle()  // Wait for the UI to be fully rendered

    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("notadate")
    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()  // Ensure the UI has settled before verification

    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun doesNotSubmitWithNoFoodName() {
    composeTestRule.setContent {
      AddFoodItemScreen(navigationActions, householdViewModel, foodItemViewModel)
    }

    composeTestRule.waitForIdle()  // Wait for the UI to be fully rendered

    composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()  // Ensure the UI has settled before verification

    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun doesNotSubmitWithNoAmount() {
    composeTestRule.setContent {
      AddFoodItemScreen(navigationActions, householdViewModel, foodItemViewModel)
    }

    composeTestRule.waitForIdle()  // Wait for the UI to be fully rendered

    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()  // Ensure the UI has settled before verification

    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun submitWithValidData() {
    composeTestRule.setContent {
      AddFoodItemScreen(navigationActions, householdViewModel, foodItemViewModel)
    }

    composeTestRule.waitForIdle()  // Wait for the UI to be fully rendered

    // Input valid food name, amount, and valid dates
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("01/01/2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("31/12/2023")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("30/12/2023")

    // Click the save button
    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()  // Ensure the UI has settled before verification

    // Verify that the addFoodItem method is called with the correct parameters
    verify(exactly = 1) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun showsErrorMessageWhenBuyDateIsAfterOpenDateOrExpireDate() {
    composeTestRule.setContent {
      AddFoodItemScreen(navigationActions, householdViewModel, foodItemViewModel)
    }

    composeTestRule.waitForIdle()  // Wait for the UI to be fully rendered

    // Input valid food name and amount
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")

    // Input invalid dates
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("01/01/2024")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01/01/2023")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("01/01/2023")

    // Click the save button
    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()  // Ensure the UI has settled before assertion

    // Verify that the error dialog is displayed
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
  }

  @Test
  fun showsErrorMessageOnInvalidInput() {
    composeTestRule.setContent {
      AddFoodItemScreen(navigationActions, householdViewModel, foodItemViewModel)
    }

    composeTestRule.waitForIdle()  // Wait for the UI to be fully rendered

    composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()  // Ensure the UI has settled before assertion

    // Verify that the error dialog is displayed
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
  }
}