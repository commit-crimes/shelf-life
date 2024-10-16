package com.github.se.bootcamp.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.overview.AddFoodItemScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class AddFoodItemScreenTest {
    private lateinit var householdViewModel: HouseholdViewModel
    private lateinit var foodItemViewModel: ListFoodItemsViewModel
    private lateinit var navigationActions: NavigationActions

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        householdViewModel = mock(HouseholdViewModel::class.java)
        foodItemViewModel = mock(ListFoodItemsViewModel::class.java)
        navigationActions = mock(NavigationActions::class.java)

        `when`(navigationActions.currentRoute()).thenReturn(Screen.ADD_FOOD)
    }

    @Test
    fun displayAllComponents() {
        composeTestRule.setContent {
            AddFoodItemScreen(navigationActions, householdViewModel, foodItemViewModel)
        }

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

        composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("notadate")
        composeTestRule.onNodeWithTag("foodSave").performClick()

        verify(householdViewModel, never()).addFoodItem(any())
    }
}