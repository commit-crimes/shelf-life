package com.android.shelfLife.ui.overview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
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
  // TODO
  //  @Test
  //  fun displayAllComponents() {
  //    composeTestRule.setContent {
  //      AddFoodItemScreen(
  //        navigationActions = navigationActions,
  //        houseHoldViewModel = householdViewModel,
  //        foodItemViewModel = foodItemViewModel
  //      )
  //    }
  //
  //    // Check that the main screen is displayed
  //    composeTestRule.onNodeWithTag("addScreen").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("addFoodItemTitle").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("inputFoodName").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("inputFoodAmount").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("inputFoodUnit").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("inputFoodCategory").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("inputFoodLocation").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("inputFoodExpireDate").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("inputFoodOpenDate").assertIsDisplayed()
  //    composeTestRule.onNodeWithTag("inputFoodBuyDate").assertIsDisplayed()
  //  }

  @Test
  fun clickingGoBackButtonCallsNavigation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Click the go back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Verify that navigationActions.goBack() was called
    verify { navigationActions.goBack() }
  }

  //  @Test
  //  fun submitWithValidData() {
  //    // Clear mocks
  //    clearMocks(householdViewModel, foodItemViewModel, navigationActions)
  //
  //    // Mock getUID method
  //    every { foodItemViewModel.getUID() } returns "testUID"
  //
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
  //    // Click submit
  //    composeTestRule.onNodeWithTag("foodSave").performClick()
  //
  //    // Wait for any UI updates
  //    composeTestRule.waitForIdle()
  //
  //    // Verify that addFoodItem was called
  //    verify(exactly = 1) { householdViewModel.addFoodItem(any()) }
  //
  //    // Verify that navigationActions.goBack() was called
  //    verify(exactly = 1) { navigationActions.goBack() }
  //  }

  @Test
  fun cancellingFormReturnsToPreviousScreen() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Click the go back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Verify that navigationActions.goBack() was called
    verify { navigationActions.goBack() }
  }

  // TODO
  //  @Test
  //  fun doesNotSubmitWhenRequiredFieldsAreEmpty() {
  //    composeTestRule.setContent {
  //      AddFoodItemScreen(
  //        navigationActions = navigationActions,
  //        houseHoldViewModel = householdViewModel,
  //        foodItemViewModel = foodItemViewModel
  //      )
  //    }
  //
  //    // Leave required fields empty
  //    composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
  //    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
  //
  //    // Click the submit button
  //    composeTestRule.onNodeWithTag("foodSave").performClick()
  //
  //    // Wait for UI updates
  //    composeTestRule.waitForIdle()
  //
  //    // Verify that the error dialog is displayed
  //    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
  //
  //    // Verify that addFoodItem was not called
  //    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  //  }
  // TODO
  //  @Test
  //  fun showsErrorWhenAmountIsNotANumber() {
  //    composeTestRule.setContent {
  //      AddFoodItemScreen(
  //        navigationActions = navigationActions,
  //        houseHoldViewModel = householdViewModel,
  //        foodItemViewModel = foodItemViewModel
  //      )
  //    }
  //
  //    // Input invalid amount
  //    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
  //    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("invalid")
  //
  //    // Click the submit button
  //    composeTestRule.onNodeWithTag("foodSave").performClick()
  //
  //    // Wait for UI updates
  //    composeTestRule.waitForIdle()
  //
  //    // Verify that the error dialog is displayed
  //    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
  //
  //    // Verify that addFoodItem was not called
  //    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  //  }
  // TODO
  //  @Test
  //  fun showsErrorWhenDatesAreInvalid() {
  //    composeTestRule.setContent {
  //      AddFoodItemScreen(
  //        navigationActions = navigationActions,
  //        houseHoldViewModel = householdViewModel,
  //        foodItemViewModel = foodItemViewModel
  //      )
  //    }
  //
  //    // Input valid food name and amount
  //    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
  //    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
  //
  //    // Input invalid dates
  //    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("invalid-date")
  //    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("another-invalid-date")
  //
  // composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("yet-another-invalid-date")
  //
  //    // Click the submit button
  //    composeTestRule.onNodeWithTag("foodSave").performClick()
  //
  //    // Wait for UI updates
  //    composeTestRule.waitForIdle()
  //
  //    // Verify that the error dialog is displayed
  //    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
  //
  //    // Verify that addFoodItem was not called
  //    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  //  }

  //  @Test
  //  fun submitWithMinimumRequiredData() {
  //    // Mock getUID method
  //    every { foodItemViewModel.getUID() } returns "testUID"
  //
  //    composeTestRule.setContent {
  //      AddFoodItemScreen(
  //        navigationActions = navigationActions,
  //        houseHoldViewModel = householdViewModel,
  //        foodItemViewModel = foodItemViewModel
  //      )
  //    }
  //
  //    // Input only required fields
  //    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Banana")
  //    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("3")
  //
  //    // Click the submit button
  //    composeTestRule.onNodeWithTag("foodSave").performClick()
  //
  //    // Wait for UI updates
  //    composeTestRule.waitForIdle()
  //
  //    // Verify that addFoodItem was called
  //    verify(exactly = 1) { householdViewModel.addFoodItem(any()) }
  //
  //    // Verify that navigationActions.goBack() was called
  //    verify(exactly = 1) { navigationActions.goBack() }
  //  }

}
