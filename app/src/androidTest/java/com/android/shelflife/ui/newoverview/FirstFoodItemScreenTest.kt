package com.android.shelfLife.ui.newoverview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
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
class FirstFoodItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navigationActions: NavigationActions
    private lateinit var foodItemViewModel: ListFoodItemsViewModel
    private lateinit var foodFactsViewModel: FoodFactsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        // Initialize MockK
        navigationActions = mockk()
        foodItemViewModel = mockk()
        foodFactsViewModel = mockk()

        // Mock ViewModel methods
        every { navigationActions.goBack() } just runs
        every { navigationActions.navigateTo(Screen.CHOOSE_FOOD_ITEM) } just runs
        every { foodItemViewModel.setNewFoodItemName(any()) } just runs
        every { foodFactsViewModel.searchByQuery(any()) } just runs
    }

    @Test
    fun testInitialUIComponentsDisplayed() {
        composeTestRule.setContent {
            FirstFoodItem(
                navigationActions = navigationActions,
                foodItemViewModel = foodItemViewModel,
                foodFactsViewModel = foodFactsViewModel
            )
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
                foodItemViewModel = foodItemViewModel,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Enter invalid food name
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput("@#$%^")
        composeTestRule.onNodeWithTag("submitButton").performClick()

        // Verify error message is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.food_name_invalid_error)
        ).assertIsDisplayed()

        // Enter valid food name
        composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apples")
        composeTestRule.onNodeWithTag("submitButton").performClick()

        // Verify error message is gone
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.food_name_invalid_error)
        ).assertDoesNotExist()
    }

    @Test
    fun testCancelButtonNavigatesBack() {
        composeTestRule.setContent {
            FirstFoodItem(
                navigationActions = navigationActions,
                foodItemViewModel = foodItemViewModel,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Click cancel button
        composeTestRule.onNodeWithTag("cancelButton").performClick()

        // Verify navigation back action is triggered
        verify { navigationActions.goBack() }
    }

    @Test
    fun testSubmitButtonWithValidFoodName() {
        composeTestRule.setContent {
            FirstFoodItem(
                navigationActions = navigationActions,
                foodItemViewModel = foodItemViewModel,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Enter valid food name
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Bananas")
        composeTestRule.onNodeWithTag("submitButton").performClick()

        // Verify food item is set and navigation to the next screen is triggered
        verify { foodItemViewModel.setNewFoodItemName("Bananas") }
        verify { foodFactsViewModel.searchByQuery("Bananas") }
        verify { navigationActions.navigateTo(Screen.CHOOSE_FOOD_ITEM) }
    }

    @Test
    fun testSubmitButtonWithInvalidFoodName() {
        composeTestRule.setContent {
            FirstFoodItem(
                navigationActions = navigationActions,
                foodItemViewModel = foodItemViewModel,
                foodFactsViewModel = foodFactsViewModel
            )
        }

        // Enter invalid food name
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput("@#$%^")
        composeTestRule.onNodeWithTag("submitButton").performClick()

        // Verify no actions are triggered for invalid input
        verify(exactly = 0) { foodItemViewModel.setNewFoodItemName(any()) }
        verify(exactly = 0) { foodFactsViewModel.searchByQuery(any()) }
    }
}
