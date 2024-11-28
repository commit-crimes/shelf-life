package com.android.shelfLife.ui.overview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
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

@RunWith(AndroidJUnit4::class)
class AddFoodItemScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var navigationActions: NavigationActions
  private lateinit var houseHoldViewModel: HouseholdViewModel
  private lateinit var foodItemViewModel: ListFoodItemsViewModel
  private lateinit var foodFactViewModel: FoodFactsViewModel

  @Before
  fun setUp() {
    // Initialize MockK
    MockKAnnotations.init(this, relaxed = true)

    navigationActions = mockk(relaxed = true)
    houseHoldViewModel = mockk(relaxed = true)
    foodItemViewModel = mockk(relaxed = true)
    foodFactViewModel = mockk(relaxed = true)

    // Mock getUID() to return a valid UID
    every { foodItemViewModel.getUID() } returns "testUID"
    every { houseHoldViewModel.addFoodItem(any()) } just runs
  }

  @Test
  fun testInitialUIComponentsDisplayed() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Verify that all input fields are displayed
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodName"))
    composeTestRule.onNodeWithTag("inputFoodName").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodAmount"))
    composeTestRule.onNodeWithTag("inputFoodAmount").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodUnit"))
    composeTestRule.onNodeWithTag("inputFoodUnit").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodCategory"))
    composeTestRule.onNodeWithTag("inputFoodCategory").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodLocation"))
    composeTestRule.onNodeWithTag("inputFoodLocation").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithTag("inputFoodExpireDate").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodOpenDate"))
    composeTestRule.onNodeWithTag("inputFoodOpenDate").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodBuyDate"))
    composeTestRule.onNodeWithTag("inputFoodBuyDate").assertIsDisplayed()

    composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()
  }

  @Test
  fun testFoodNameFieldValidation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Enter invalid food name
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("@#$%^")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Food name contains invalid characters").assertIsDisplayed()

    // Enter valid food name
    composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apples")
    // Verify error message is gone
    composeTestRule.onNodeWithText("Food name contains invalid characters").assertDoesNotExist()
  }

  @Test
  fun testAmountFieldValidation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Enter invalid amount (letters)
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("abc")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Amount must be a number").assertIsDisplayed()

    // Enter invalid amount (negative number)
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("-5")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Amount must be positive").assertIsDisplayed()

    // Enter valid amount
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("10")
    // Verify error messages are gone
    composeTestRule.onNodeWithText("Amount must be a number").assertDoesNotExist()
    composeTestRule.onNodeWithText("Amount must be positive").assertDoesNotExist()
  }

  @Test
  fun testUnitDropdownSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Open the unit dropdown
    composeTestRule.onNodeWithTag("inputFoodUnit").performClick()
    // Select a unit
    composeTestRule.onNodeWithTag("dropDownItem_Ml").performClick()
    // Verify the selected unit
    composeTestRule.onNodeWithTag("dropdownMenu_Select unit").assertTextContains("Ml")
  }

  @Test
  fun testCategoryDropdownSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Open the category dropdown
    composeTestRule.onNodeWithTag("inputFoodCategory").performClick()
    // Select a category
    composeTestRule.onNodeWithTag("dropDownItem_Fruit").performClick()
    // Verify the selected category
    composeTestRule.onNodeWithTag("dropdownMenu_Select category").assertTextContains("Fruit")
  }

  @Test
  fun testLocationDropdownSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Open the location dropdown
    composeTestRule.onNodeWithTag("inputFoodLocation").performClick()
    // Select a location
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    // Verify the selected location
    composeTestRule.onNodeWithTag("dropdownMenu_Select location").assertTextContains("Pantry")
  }

  @Test
  fun testDateFieldsValidation() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Enter invalid expire date
    composeTestRule
        .onNodeWithTag("inputFoodExpireDate")
        .performTextInput("31132023") // Invalid date
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Invalid date").assertIsDisplayed()

    // Enter valid expire date
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule
        .onNodeWithTag("inputFoodExpireDate")
        .performTextInput("31122030") // Valid future date
    // Verify error message is gone
    composeTestRule.onNodeWithText("Invalid date").assertDoesNotExist()
  }

  @Test
  fun testSubmitButtonWithInvalidForm() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }
    // Scroll to the submit button
    composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    // Click the submit button without filling the form
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Verify that error messages are displayed for required fields
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodName"))
    composeTestRule.onNodeWithText("Food name cannot be empty").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodAmount"))
    composeTestRule.onNodeWithText("Amount cannot be empty").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithText("Date cannot be empty").assertIsDisplayed()
  }

  @Test
  fun testSubmitButtonWithValidForm() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Fill in valid inputs
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Bananas")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("inputFoodUnit").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Count").performClick()
    composeTestRule.onNodeWithTag("inputFoodCategory").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Fruit").performClick()
    composeTestRule.onNodeWithTag("inputFoodLocation").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122030") // Future date
    // Clear and re-enter buy date to ensure it's valid
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule
        .onNodeWithTag("inputFoodBuyDate")
        .performTextInput(formatTimestampToDate(Timestamp.now()))

    // Scroll to the submit button
    composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    // Click the submit button
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Verify that the addFoodItem function was called
    verify { houseHoldViewModel.addFoodItem(any()) }

    // Verify that navigation action was called
    verify { navigationActions.goBack() }
  }

  @Test
  fun testOpenDateValidationAgainstBuyAndExpireDates() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Enter buy date
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("01012026")

    // Enter expire date
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122026")

    // Enter invalid open date (before buy date)
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("31122025")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Open Date cannot be before Buy Date").assertIsDisplayed()

    // Enter valid open date (after buy date)
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01022026")
    // Verify error message is gone
    composeTestRule.onNodeWithText("Open Date cannot be before Buy Date").assertDoesNotExist()

    // Enter invalid open date (after expire date)
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01012027")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Open Date cannot be after Expire Date").assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Click the back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Verify that the navigation action was called
    verify { navigationActions.goBack() }
  }

  @Test
  fun verifyClearFoodFactsSuggestionsCalledOnDispose() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify { foodFactViewModel.clearFoodFactsSuggestions() }
  }

  @Test
  fun testImageSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Enter a food name to trigger image suggestions
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")

    // Wait for suggestions to appear
    composeTestRule.waitForIdle()

    // Select the first image suggestion
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("foodFactImage"))
    composeTestRule.onNodeWithTag("foodFactImage").performClick()

    // Verify that the selected image is displayed
    composeTestRule.onNodeWithTag("selectedImage").assertIsDisplayed()
  }

  @Test
  fun testNoImageOptionSelection() {
    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions,
          houseHoldViewModel = houseHoldViewModel,
          foodItemViewModel = foodItemViewModel,
          foodFactsViewModel = foodFactViewModel)
    }

    // Scroll to the "No Image" option
    composeTestRule
        .onNodeWithTag("addFoodItemScreen")
        .performScrollToNode(hasTestTag("noImageOption"))
    composeTestRule.onNodeWithTag("noImageOption").performClick()

    // Verify that the default image is displayed
    composeTestRule.onNodeWithTag("defaultImage").assertIsDisplayed()
  }
}
