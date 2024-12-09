package com.android.shelfLife.ui.newoverview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newFoodItem.FoodStorageLocation
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.newnavigation.NavigationActions
import com.android.shelfLife.ui.newnavigation.Route
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class EditFoodItemScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var foodItemRepository: FoodItemRepository

  private val initialFoodItem =
      FoodItem(
          uid = "1",
          foodFacts =
              com.android.shelfLife.model.foodFacts.FoodFacts(
                  name = "Apple",
                  barcode = "123456789",
                  quantity = com.android.shelfLife.model.foodFacts.Quantity(1.0, FoodUnit.COUNT),
                  category = FoodCategory.FRUIT,
                  nutritionFacts =
                      com.android.shelfLife.model.foodFacts.NutritionFacts(energyKcal = 52)),
          location = FoodStorageLocation.PANTRY,
          expiryDate = Timestamp.now(),
          owner = "user1")

  @Before
  fun setUp() {
    navigationActions = mock()
    userRepository = mock()
    foodItemRepository = mock()

    // Mock user
    val user =
        User(
            uid = "user1",
            username = "User1",
            email = "user1@example.com",
            selectedHouseholdUID = "household1")
    whenever(userRepository.user).thenReturn(MutableStateFlow(user))

    // Mock selected food item and repository flows
    whenever(foodItemRepository.selectedFoodItem).thenReturn(MutableStateFlow(initialFoodItem))
    whenever(foodItemRepository.errorMessage).thenReturn(MutableStateFlow<String?>(null))

    // Mock repository methods
    runBlocking {
      whenever(foodItemRepository.updateFoodItem(any(), any())).thenReturn(Unit)
      whenever(foodItemRepository.deleteFoodItem(any(), any())).thenReturn(Unit)
    }
  }

  @Test
  fun testInitialUIComponentsDisplayed() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
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

    // Top bar delete icon
    composeTestRule.onNodeWithTag("deleteFoodItem").assertIsDisplayed()
  }

  @Test
  fun testAmountFieldValidation() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Invalid amount (letters)
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("abc")
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Amount must be a number").assertIsDisplayed()

    // Invalid amount (negative)
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("-5")
    composeTestRule.onNodeWithText("Amount must be positive").assertIsDisplayed()

    // Valid amount
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("10")
    // Ensure errors gone
    composeTestRule.onNodeWithText("Amount must be a number").assertDoesNotExist()
    composeTestRule.onNodeWithText("Amount must be positive").assertDoesNotExist()
  }

  @Test
  fun testLocationDropdownSelection() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Scroll and open location dropdown
    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("editFoodLocation"))
    composeTestRule.onNodeWithTag("editFoodLocation").performClick()

    // Select a location (e.g., Pantry)
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()

    // Verify selected location
    composeTestRule.onNodeWithTag("dropdownMenu_Select location").assertTextContains("Pantry")
  }

  @Test
  fun testDateFieldsValidation() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Invalid expire date
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("31132023")

    // Submit
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Check error
    composeTestRule.onNodeWithText("Invalid date").assertIsDisplayed()

    // Valid expire date
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("31122030")

    // Submit again
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Error gone
    composeTestRule.onNodeWithText("Invalid date").assertDoesNotExist()
  }

  @Test
  fun testSubmitButtonWithInvalidForm() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Clear amount and expire date to make form invalid
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()

    // Scroll and submit
    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Check required fields errors
    composeTestRule.onNodeWithText("Amount cannot be empty").assertIsDisplayed()
    composeTestRule.onNodeWithText("Date cannot be empty").assertIsDisplayed()
  }

  @Test
  fun testSubmitButtonWithValidForm() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Fill in valid inputs
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("5")

    // Set location
    composeTestRule.onNodeWithTag("editFoodLocation").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()

    // Valid expire date
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("31122030")

    // Valid buy date
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextClearance()
    composeTestRule
        .onNodeWithTag("editFoodBuyDate")
        .performTextInput(formatTimestampToDate(Timestamp.now()))

    // Submit
    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Verify that updateFoodItem is called
    runBlocking { verify(foodItemRepository).updateFoodItem(eq("household1"), any()) }

    // Navigation back
    verify(navigationActions).goBack()
  }

  @Test
  fun testOpenDateValidationAgainstBuyAndExpireDates() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Valid buy date
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextInput("01012026")

    // Valid expire date
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("31122026")

    // Invalid open date (before buy date)
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextInput("31122025")

    // Submit
    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.onNodeWithText("Open Date cannot be before Buy Date").assertIsDisplayed()

    // Valid open date (after buy date)
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextInput("01022026")

    // Submit again
    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.onNodeWithText("Open Date cannot be before Buy Date").assertDoesNotExist()

    // Invalid open date (after expire date)
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextInput("01012027")

    // Submit again
    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.onNodeWithText("Open Date cannot be after Expire Date").assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    composeTestRule.onNodeWithTag("goBackArrow").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testCancelButtonNavigatesBack() {
    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("cancelButton"))
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("cancelButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testDeleteFoodItem() {
    runBlocking {
      composeTestRule.setContent {
        EditFoodItemScreen(
            navigationActions = navigationActions,
            foodItemRepository = foodItemRepository,
            userRepository = userRepository)
      }

      // Click the delete icon
      composeTestRule.onNodeWithTag("deleteFoodItem").performClick()

      // Verify that deleteFoodItem is called
      verify(foodItemRepository).deleteFoodItem(eq("household1"), eq("1"))

      // Verify navigation to OVERVIEW
      verify(navigationActions).navigateTo(Route.OVERVIEW)
    }
  }
}
