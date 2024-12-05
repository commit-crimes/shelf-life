package com.android.shelfLife.ui.newoverview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.newFoodItem.FoodItem
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newFoodItem.FoodStorageLocation
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
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
class AddFoodItemScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navigationActions: NavigationActions
    private lateinit var userRepository: UserRepository
    private lateinit var foodItemRepository: FoodItemRepository

    @Before
    fun setUp() {
        // Initialize Mockito mocks
        navigationActions = mock()
        userRepository = mock()
        foodItemRepository = mock()

        // Mock getNewUid() to return a valid UID
        whenever(foodItemRepository.getNewUid()).thenReturn("testUID")

        // Mock userRepository.user to return a user with a selected household UID
        val user = User(
            uid = "user1",
            username = "User1",
            email = "user1@example.com",
            selectedHouseholdUID = "household1"
        )
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
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Verify that all input fields are displayed
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodName"))
        composeTestRule.onNodeWithTag("inputFoodName").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodAmount"))
        composeTestRule.onNodeWithTag("inputFoodAmount").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodUnit"))
        composeTestRule.onNodeWithTag("inputFoodUnit").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodCategory"))
        composeTestRule.onNodeWithTag("inputFoodCategory").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodLocation"))
        composeTestRule.onNodeWithTag("inputFoodLocation").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodExpireDate"))
        composeTestRule.onNodeWithTag("inputFoodExpireDate").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodOpenDate"))
        composeTestRule.onNodeWithTag("inputFoodOpenDate").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodBuyDate"))
        composeTestRule.onNodeWithTag("inputFoodBuyDate").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
        composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("cancelButton"))
        composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    }

    @Test
    fun testFoodNameFieldValidation() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Scroll to and enter invalid food name
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodName"))
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput("@#$%^")

        // Scroll to and click submit
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
        composeTestRule.onNodeWithTag("foodSave").performClick()

        // Verify error message is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.food_name_invalid_error)
        ).assertIsDisplayed()

        // Enter valid food name
        composeTestRule.onNodeWithTag("inputFoodName").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apples")

        // Try to submit again
        composeTestRule.onNodeWithTag("foodSave").performClick()

        // Verify error message is gone
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.food_name_invalid_error)
        ).assertDoesNotExist()
    }

    @Test
    fun testAmountFieldValidation() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Invalid amount (letters)
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodAmount"))
        composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("abc")

        // Scroll and submit
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
        composeTestRule.onNodeWithTag("foodSave").performClick()

        // Verify error message is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.amount_not_number_error)
        ).assertIsDisplayed()

        // Invalid amount (negative)
        composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("-5")

        // Submit again
        composeTestRule.onNodeWithTag("foodSave").performClick()
        // Verify negative error
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.amount_negative_error)
        ).assertIsDisplayed()

        // Valid amount
        composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("10")

        // Submit again
        composeTestRule.onNodeWithTag("foodSave").performClick()
        // Verify errors gone
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.amount_not_number_error)
        ).assertDoesNotExist()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.amount_negative_error)
        ).assertDoesNotExist()
    }

    @Test
    fun testUnitDropdownSelection() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Scroll and open the unit dropdown
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodUnit"))
        composeTestRule.onNodeWithTag("inputFoodUnit").performClick()

        // Select a unit (e.g., Ml)
        composeTestRule.onNodeWithTag("dropDownItem_Ml").performClick()

        // Verify the selected unit
        composeTestRule.onNodeWithTag("dropdownMenu_Select unit").assertTextContains("Ml")
    }

    @Test
    fun testCategoryDropdownSelection() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Scroll and open category dropdown
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodCategory"))
        composeTestRule.onNodeWithTag("inputFoodCategory").performClick()

        // Select a category (e.g., Fruit)
        composeTestRule.onNodeWithTag("dropDownItem_Fruit").performClick()

        // Verify the selected category
        composeTestRule.onNodeWithTag("dropdownMenu_Select category").assertTextContains("Fruit")
    }

    @Test
    fun testLocationDropdownSelection() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Scroll and open the location dropdown
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodLocation"))
        composeTestRule.onNodeWithTag("inputFoodLocation").performClick()

        // Select a location (e.g., Pantry)
        composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()

        // Verify the selected location
        composeTestRule.onNodeWithTag("dropdownMenu_Select location").assertTextContains("Pantry")
    }

    @Test
    fun testDateFieldsValidation() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Invalid expire date
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodExpireDate"))
        composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31132023")

        // Submit
        composeTestRule.onNodeWithTag("foodSave").performClick()

        // Check error
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.date_invalid_error)
        ).assertIsDisplayed()

        // Valid expire date
        composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122030")

        // Submit again
        composeTestRule.onNodeWithTag("foodSave").performClick()

        // Error gone
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.date_invalid_error)
        ).assertDoesNotExist()
    }

    @Test
    fun testSubmitButtonWithInvalidForm() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }
        // Scroll to submit button
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
        // Click submit without filling
        composeTestRule.onNodeWithTag("foodSave").performClick()

        // Check required fields errors
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodName"))
        composeTestRule.onNodeWithText("Food name cannot be empty").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodAmount"))
        composeTestRule.onNodeWithText("Amount cannot be empty").assertIsDisplayed()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodExpireDate"))
        composeTestRule.onNodeWithText("Date cannot be empty").assertIsDisplayed()
    }

    @Test
    fun testSubmitButtonWithValidForm() {
        // Capture the FoodItem
        val foodItemCaptor = argumentCaptor<FoodItem>()

        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Fill in valid inputs
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodName"))
        composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Bananas")

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodAmount"))
        composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodUnit"))
        composeTestRule.onNodeWithTag("inputFoodUnit").performClick()
        composeTestRule.onNodeWithTag("dropDownItem_Count").performClick()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodCategory"))
        composeTestRule.onNodeWithTag("inputFoodCategory").performClick()
        composeTestRule.onNodeWithTag("dropDownItem_Fruit").performClick()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodLocation"))
        composeTestRule.onNodeWithTag("inputFoodLocation").performClick()
        composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodExpireDate"))
        composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122030")

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodBuyDate"))
        composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput(formatTimestampToDate(Timestamp.now()))

        // Submit
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
        composeTestRule.onNodeWithTag("foodSave").performClick()

        runBlocking {
            verify(foodItemRepository).addFoodItem(eq("household1"), foodItemCaptor.capture())
        }

        val addedFoodItem = foodItemCaptor.firstValue
        assert(addedFoodItem.foodFacts.name == "Bananas")
        assert(addedFoodItem.foodFacts.quantity.amount == 5.0)
        assert(addedFoodItem.foodFacts.quantity.unit == FoodUnit.COUNT)
        assert(addedFoodItem.foodFacts.category == FoodCategory.FRUIT)
        assert(addedFoodItem.location == FoodStorageLocation.PANTRY)

        verify(navigationActions).goBack()
    }

    @Test
    fun testOpenDateValidationAgainstBuyAndExpireDates() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Enter buy date
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodBuyDate"))
        composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("01012026")

        // Enter expire date
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodExpireDate"))
        composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122026")

        // Enter invalid open date (before buy date)
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodOpenDate"))
        composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("31122025")

        // Submit
        composeTestRule.onNodeWithTag("foodSave").performClick()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.open_date_before_buy_date_error)
        ).assertIsDisplayed()

        // Valid open date (after buy date)
        composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01022026")

        // Submit again
        composeTestRule.onNodeWithTag("foodSave").performClick()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.open_date_before_buy_date_error)
        ).assertDoesNotExist()

        // Invalid open date (after expire date)
        composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01012027")

        // Submit again
        composeTestRule.onNodeWithTag("foodSave").performClick()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.open_date_after_expire_date_error)
        ).assertIsDisplayed()
    }

    @Test
    fun testNavigationBackButton() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        composeTestRule.onNodeWithTag("goBackArrow").performClick()

        verify(navigationActions).goBack()
    }

    @Test
    fun testCancelButtonNavigatesBack() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("cancelButton"))
        composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()

        composeTestRule.onNodeWithTag("cancelButton").performClick()

        verify(navigationActions).goBack()
    }

    @Test
    fun testDateReValidation() {
        composeTestRule.setContent {
            AddFoodItemScreen(
                navigationActions = navigationActions,
                foodItemRepository = foodItemRepository,
                userRepository = userRepository
            )
        }

        // Valid buy date
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodBuyDate"))
        composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("01012026")

        // Valid expire date
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodExpireDate"))
        composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("31122026")

        // Valid open date
        composeTestRule.onNodeWithTag("addFoodItemScreen").performScrollToNode(hasTestTag("inputFoodOpenDate"))
        composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01022026")

        // Change buy date to invalid
        composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
        composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("31132023")

        // Submit
        composeTestRule.onNodeWithTag("foodSave").performClick()

        // Check error
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.date_invalid_error)
        ).assertIsDisplayed()
    }
}
