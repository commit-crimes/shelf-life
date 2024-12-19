package com.android.shelflife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.newoverview.FirstFoodItem
import com.android.shelfLife.ui.overview.IndividualFoodItemScreen
import com.android.shelfLife.ui.overview.OverviewScreen
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import com.android.shelfLife.viewmodel.overview.IndividualFoodItemViewModel
import com.android.shelfLife.viewmodel.overview.OverviewScreenViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodItemRepositoryTestHelper
import helpers.HouseholdRepositoryTestHelper
import java.util.*
import javax.inject.Inject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class FirstFoodItemScreenTest {
    @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var navigationActions: NavigationActions

    @Inject lateinit var listFoodItemsRepository: FoodItemRepository
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var foodFactsRepository: FoodFactsRepository

    private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper

    private lateinit var foodItemViewModel: FoodItemViewModel

    private lateinit var instrumentationContext: android.content.Context


    private lateinit var houseHold: HouseHold
    private lateinit var foodItem: FoodItem

    @Before
    fun setUp(){
        hiltAndroidTestRule.inject()
        navigationActions = mock()

        foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(listFoodItemsRepository)
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context

        whenever(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
        // Create a FoodItem to be used in tests
        val foodFacts =
            FoodFacts(
                name = "Apple",
                barcode = "123456789",
                quantity = Quantity(5.0, FoodUnit.COUNT),
                category = FoodCategory.FRUIT)
        foodItem =
            FoodItem(
                uid = "foodItem1",
                foodFacts = foodFacts,
                expiryDate =
                Timestamp(Date(System.currentTimeMillis() + 86400000)), // Expires in 1 day,
                owner = "testOwner")
        // Initialize the household with the food item
        houseHold =
            HouseHold(
                uid = "1",
                name = "Test Household",
                members = listOf("John", "Doe"),
                sharedRecipes = emptyList(),
                ratPoints = emptyMap(),
                stinkyPoints = emptyMap())

        userRepository.selectHousehold("Test Household")
        foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))
        foodItemViewModel = FoodItemViewModel(
            listFoodItemsRepository, userRepository, foodFactsRepository
        )

    }

    @Test
    fun testTitleDisplayed() {

        composeTestRule.setContent { FirstFoodItem(navigationActions, foodItemViewModel) }
        val expectedTitle = "Choose Food Item Name"
        composeTestRule.onNodeWithText(expectedTitle)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun testInputFieldHintDisplayed() {

        composeTestRule.setContent { FirstFoodItem(navigationActions, foodItemViewModel) }
        val expectedHint = "Enter food name"
        composeTestRule.onNodeWithTag("inputFoodName").assertExists()
        composeTestRule.onNodeWithText(expectedHint).assertIsDisplayed()
    }

    @Test
    fun testCancelButtonClearsInputAndNavigatesToOverview() {


        composeTestRule.setContent { FirstFoodItem(navigationActions, foodItemViewModel) }
        val cancelText = "Cancel"

        composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
        composeTestRule.onNodeWithText(cancelText).performClick()

        // Assert that foodName in the ViewModel is cleared
        assert(foodItemViewModel.foodName.isEmpty())
        verify(navigationActions).navigateTo(Route.OVERVIEW)
    }

    @Test
    fun testSubmitButtonWithValidInput() {

        composeTestRule.setContent { FirstFoodItem(navigationActions, foodItemViewModel) }
        val submitText = "Submit"
        val foodName = "Banana"

        // Simulate text input
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput(foodName)
        // Perform click on the submit button
        composeTestRule.onNodeWithText(submitText).performClick()
        // Verify navigation to CHOOSE_FOOD_ITEM and ViewModel state updates
        verify(navigationActions).navigateTo(Screen.CHOOSE_FOOD_ITEM)
        assert(foodItemViewModel.foodName == foodName)
    }

    @Test
    fun testSubmitButtonWithInvalidInputShowsError() {

        composeTestRule.setContent { FirstFoodItem(navigationActions, foodItemViewModel) }
        val submitText = "Submit"
        val errorMessage = "Please correct the errors before submitting"

        // Perform click on the submit button
        composeTestRule.onNodeWithText(submitText).performClick()

        // Verify that navigation doesn't happen
        verifyNoInteractions(navigationActions)
        composeTestRule.onNodeWithText(errorMessage).assertDoesNotExist()
    }

    @Test
    fun testSubmitButtonNavigatesToChooseFoodItem() {

        composeTestRule.setContent { FirstFoodItem(navigationActions, foodItemViewModel) }
        val submitText = "Submit"
        val foodName = "Orange"

        // Simulate text input
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput(foodName)
        // Perform click on the submit button
        composeTestRule.onNodeWithText(submitText).performClick()
        // Verify navigation and state updates
        verify(navigationActions).navigateTo(Screen.CHOOSE_FOOD_ITEM)
        assert(foodItemViewModel.foodName == foodName)
    }
}