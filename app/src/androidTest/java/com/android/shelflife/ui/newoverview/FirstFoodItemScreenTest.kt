package com.android.shelfLife.ui.newoverview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.R
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class FirstFoodItemTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var foodItemRepository: FoodItemRepository

  @Before
  fun setUp() {

    navigationActions = mock()
    userRepository = mock()
    foodItemRepository = mock()
    whenever(foodItemRepository.getNewUid()).thenReturn("testUID")
    val user =
        User(
            uid = "user1",
            username = "User1",
            email = "user1@example.com",
            selectedHouseholdUID = "household1")
    whenever(userRepository.user).thenReturn(MutableStateFlow(user))

    whenever(foodItemRepository.foodItems).thenReturn(MutableStateFlow(emptyList()))
    whenever(foodItemRepository.selectedFoodItem).thenReturn(MutableStateFlow<FoodItem?>(null))
    whenever(foodItemRepository.errorMessage).thenReturn(MutableStateFlow<String?>(null))

    runBlocking {
      whenever(foodItemRepository.addFoodItem(any(), any())).thenReturn(Unit)
      whenever(foodItemRepository.updateFoodItem(any(), any())).thenReturn(Unit)
      whenever(foodItemRepository.deleteFoodItem(any(), any())).thenReturn(Unit)
    }
  }

  @Test
  fun testInitialUIComponentsDisplayed() {
    composeTestRule.setContent {
      FirstFoodItem(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Verify all UI components are displayed
    composeTestRule.onNodeWithTag("inputFoodName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("submitButton").assertIsDisplayed()
  }

  @Test
  fun testFoodNameValidation() {
    composeTestRule.setContent {
      FirstFoodItem(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Enter invalid food name
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("@#$%^")
    composeTestRule.onNodeWithTag("submitButton").performClick()

    // Verify error message is displayed
    composeTestRule
        .onNodeWithText(composeTestRule.activity.getString(R.string.food_name_invalid_error))
        .assertIsDisplayed()

    // Enter valid food name
    composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apples")
    composeTestRule.onNodeWithTag("submitButton").performClick()

    // Verify error message is gone
    composeTestRule
        .onNodeWithText(composeTestRule.activity.getString(R.string.food_name_invalid_error))
        .assertDoesNotExist()
  }

  @Test
  fun testCancelButtonNavigatesBack() {
    composeTestRule.setContent {
      FirstFoodItem(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Click cancel button
    composeTestRule.onNodeWithTag("cancelButton").performClick()
  }

  @Test
  fun testSubmitButtonWithValidFoodName() {
    composeTestRule.setContent {
      FirstFoodItem(
          navigationActions = navigationActions,
          foodItemRepository = foodItemRepository,
          userRepository = userRepository)
    }

    // Enter valid food name
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Bananas")
    composeTestRule.onNodeWithTag("submitButton").performClick()
  }
}
