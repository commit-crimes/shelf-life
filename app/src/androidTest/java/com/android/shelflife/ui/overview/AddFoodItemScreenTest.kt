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
    MockKAnnotations.init(this, relaxed = true)

    householdViewModel = mockk(relaxed = true)
    foodItemViewModel = mockk(relaxed = true)
    navigationActions = mockk(relaxed = true)
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("addFoodItemScreen").assertIsDisplayed()
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
  fun clickingGoBackButtonCallsNavigation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify { navigationActions.goBack() }
  }

  @Test
  fun submitWithValidData() {
    clearMocks(householdViewModel, foodItemViewModel, navigationActions)

    every { foodItemViewModel.getUID() } returns "testUID"

    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31/12/2023")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01/12/2023")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("30/11/2023")

    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.waitForIdle()

    verify(exactly = 1) { householdViewModel.addFoodItem(any()) }
    verify(exactly = 1) { navigationActions.goBack() }
  }

  @Test
  fun cancellingFormReturnsToPreviousScreen() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify { navigationActions.goBack() }
  }

  @Test
  fun doesNotSubmitWhenRequiredFieldsAreEmpty() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun showsErrorWhenAmountIsNotANumber() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("invalid")
    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()

    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun showsErrorWhenDatesAreInvalid() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("invalid-date")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("invalid-date")

    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("invalid-date")

    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()

    verify(exactly = 0) { householdViewModel.addFoodItem(any()) }
  }

  @Test
  fun submitWithMinimumRequiredData() {
    every { foodItemViewModel.getUID() } returns "testUID"

    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Banana")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("3")

    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31/12/2023")

    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01/12/2023")

    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("30/11/2023")

    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()

    verify(exactly = 1) { householdViewModel.addFoodItem(any()) }

    verify(exactly = 1) { navigationActions.goBack() }
  }

  @Test
  fun showsErrorWhenExpirationDateIsBeforeOpenDate() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("01/12/2023")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("02/12/2023")
    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Expiration date cannot be before the open date.")
        .assertIsDisplayed()
  }

  @Test
  fun showsErrorWhenBuyDateIsAfterOpenDate() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("03/12/2023")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("02/12/2023")

    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
  }

  @Test
  fun showsErrorWhenDateFormatIsInvalid() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("invalid-date")
    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Invalid date format. Please use dd/mm/yyyy.")
        .assertIsDisplayed()
  }

  @Test
  fun showsErrorWhenFoodNameIsEmpty() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Food name cannot be empty.").assertIsDisplayed()
  }

  @Test
  fun showsErrorWhenAmountIsEmpty() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Leave amount empty
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()

    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Wait for UI updates
    composeTestRule.waitForIdle()

    // Verify that the error dialog is displayed
    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()

    // Verify the specific error message
    composeTestRule.onNodeWithText("Amount cannot be empty.").assertIsDisplayed()
  }
}
