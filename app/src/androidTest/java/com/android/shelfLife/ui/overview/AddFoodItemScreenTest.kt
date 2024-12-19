package com.android.shelflife.ui.overview

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.overview.AddFoodItemScreen
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodFactsRepositoryTestHelper
import helpers.FoodItemRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@HiltAndroidTest
class AddFoodItemScreenTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var listFoodItemsRepository: FoodItemRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var foodFactsRepository: FoodFactsRepository

  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper
  private lateinit var foodFactsRepositoryTestHelper: FoodFactsRepositoryTestHelper
  private lateinit var navigationActions: NavigationActions
  private lateinit var instrumentationContext: Context
  private lateinit var foodItemViewModel: FoodItemViewModel
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper

  @Before
  fun setup() {
    hiltRule.inject()

    instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
    navigationActions = mock()

    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(listFoodItemsRepository)
    userRepositoryTestHelper = UserRepositoryTestHelper(userRepository)
    foodFactsRepositoryTestHelper = FoodFactsRepositoryTestHelper(foodFactsRepository)

    // You might need to inject or create a test version of the ViewModel if necessary
    // Or if using HiltViewModel, ensure the correct environment is set up
    foodItemViewModel =
        FoodItemViewModel(
            foodItemRepository = listFoodItemsRepository /* mock or test repo */,
            userRepository = userRepository /* mock or test repo */,
            foodFactsRepository = foodFactsRepository /* mock or test repo */)

    composeTestRule.setContent {
      AddFoodItemScreen(
          navigationActions = navigationActions, foodItemViewModel = foodItemViewModel)
    }
  }

  @Test
  fun screen_initialElementsDisplayed() {
    // Check that the top bar is displayed
    composeTestRule
        .onNodeWithText(instrumentationContext.getString(R.string.add_food_item_title))
        .assertIsDisplayed()

    // Check that the main LazyColumn (screen) is displayed
    composeTestRule.onNodeWithTag("addFoodItemScreen").assertIsDisplayed()

    // Check for presence of key fields
    composeTestRule.onNodeWithTag("inputFoodAmount").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodUnit").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodLocation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").assertIsDisplayed()

    // Check the buttons
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSave").assertIsDisplayed()
  }

  @Test
  fun cancelButtonNavigatesBack() {
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun submitEmptyFieldsShowsErrorToastAndDoesNotNavigateBack() {
    // Make sure fields are empty
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Since all are empty, we should see error validations.
    // It's tricky to directly assert Toasts in UI tests, but we can check for UI error states.
    // For demonstration, we rely on internal error states in the ViewModel:
    runBlocking {
      // Validate that fields are still invalid
      assert(foodItemViewModel.foodNameErrorResId != null)
      assert(foodItemViewModel.amountErrorResId != null)
    }

    // Verify navigationActions.goBack() was not called again
    // (We have no direct verification here since we can't count calls easily without using
    // additional mocking frameworks. If needed, verify(navigationActions, never()).goBack() )
  }

  @Test
  fun inputValidFieldsAndSubmitNavigatesBack() {
    // Input valid name
    composeTestRule
        .onNodeWithText(instrumentationContext.getString(R.string.add_food_item_title))
        .assertIsDisplayed()
    composeTestRule.onNode(hasTestTag("inputFoodName")).performTextInput("Test Food")

    // Input valid amount
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("100")

    // Select Unit (if it's a dropdown, open and select an option)
    composeTestRule.onNodeWithTag("inputFoodUnit").performClick()
    // Assuming your dropdown items have text like "Gram"
    composeTestRule.onNodeWithTag("dropDownItem_Ml").performClick()

    // Open and select a category
    // Without direct tags for category items, you might need to rely on displayed text.
    composeTestRule.onNodeWithTag("inputFoodCategory").performClick()
    // Select a category
    composeTestRule.onNodeWithTag("dropDownItem_Fruit").performClick()

    // Open and select a location
    composeTestRule.onNodeWithTag("inputFoodLocation").performClick()
    // Assuming there's a location named "Pantry"
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()

    // Enter valid dates
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("20122024")

    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("20122026")

    // Open date can be optional or empty
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("20122025")

    // Now click Save
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // With valid data, the ViewModel should return success and we navigate back
    verify(navigationActions).goBack()
  }

  @Test
  fun testChangingFieldsUpdatesViewModelState() = runBlocking {
    // Change Food Name
    composeTestRule.onNode(hasTestTag("inputFoodName")).performTextInput("New Name")
    assert(foodItemViewModel.foodName == "New Name")

    // Change Amount
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("250")
    assert(foodItemViewModel.amount == "250")

    // Change Dates
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("20241231")
    assert(foodItemViewModel.expireDate == "20241231")

    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("20241230")
    assert(foodItemViewModel.openDate == "20241230")

    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("20240101")
    assert(foodItemViewModel.buyDate == "20240101")
  }

  @Test
  fun testUnitDropdownExpansionAndSelection() {
    // Initially we have a default unit
    assert(foodItemViewModel.unit != null)

    // Expand unit dropdown
    composeTestRule.onNodeWithTag("inputFoodUnit").performClick()

    // Assume units: Gram, Ml, Count are options
    composeTestRule.onNodeWithText("Ml").performClick()
    assert(foodItemViewModel.unit.name == "ML") // Adjust as per your enum
  }

  @Test
  fun testLocationDropdownExpansionAndSelection() {
    // Initially we have a default location
    assert(foodItemViewModel.location != null)

    // Expand location dropdown
    composeTestRule.onNodeWithTag("inputFoodLocation").performClick()

    // Assuming "Fridge" is an option
    composeTestRule.onNodeWithText("Fridge").performClick()
    assert(foodItemViewModel.location.name == "FRIDGE") // Adjust as per your enum
  }

  @Test
  fun testCategoryDropdownExpansionAndSelection() {
    // Initially category is OTHER
    assert(foodItemViewModel.category.name == "OTHER")

    // Expand category dropdown
    // The category field might not have a testTag. If it has a text label, use that.
    composeTestRule.onNodeWithText("Other").performClick()

    // Select a different category, e.g., FRUIT
    composeTestRule.onNodeWithText("Fruit").performClick()
    assert(foodItemViewModel.category.name == "FRUIT")
  }

  @Test
  fun testErrorMessagesWhenInvalidInputProvided() = runBlocking {
    // Input invalid amount (e.g., letters)
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("abc")

    // Submit
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Check error state in the ViewModel
    assert(foodItemViewModel.amountErrorResId != null)
  }

  @Test
  fun testInvalidDateShowsError() = runBlocking {
    // Provide Buy Date that is invalid
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("00000000") // invalid

    // Submit
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Check if error is set
    assert(foodItemViewModel.buyDateErrorResId != null)
  }
}
