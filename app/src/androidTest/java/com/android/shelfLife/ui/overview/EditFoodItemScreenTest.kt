package com.android.shelflife.ui.overview

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.R
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.overview.EditFoodItemScreen
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
import org.mockito.kotlin.*

@HiltAndroidTest
class EditFoodItemScreenTest {

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

    // Initialize instrumentationContext here
    instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext

    navigationActions = mock()

    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(listFoodItemsRepository)
    userRepositoryTestHelper = UserRepositoryTestHelper(userRepository)
    foodFactsRepositoryTestHelper = FoodFactsRepositoryTestHelper(foodFactsRepository)

    // Create your test FoodItem and set it as selected
    val testFoodItem =
        FoodItem(
            uid = "test",
            foodFacts = FoodFacts(name = "test", quantity = Quantity(1.0, FoodUnit.GRAM)),
            owner = "testUser")
    foodItemRepositoryTestHelper.setSelectedFoodItem(testFoodItem)

    // Make sure isQuickAdd is not null
    foodItemRepositoryTestHelper.setIsQuickAdd(false)

    // Now create the ViewModel
    foodItemViewModel =
        FoodItemViewModel(
            foodItemRepository = listFoodItemsRepository,
            userRepository = userRepository,
            foodFactsRepository = foodFactsRepository)

    composeTestRule.setContent {
      EditFoodItemScreen(
          navigationActions = navigationActions, foodItemViewModel = foodItemViewModel)
    }
  }

  @Test
  fun screen_initialElementsDisplayed() {
    if (!foodItemViewModel.getIsQuickAdd()) {
      composeTestRule
          .onNodeWithText(instrumentationContext.getString(R.string.edit_food_item_title))
          .assertIsDisplayed()
    } else {
      composeTestRule
          .onNodeWithText(instrumentationContext.getString(R.string.finalize_food_item_title))
          .assertIsDisplayed()
    }

    composeTestRule.onNodeWithTag("editFoodItemScreen").assertIsDisplayed()

    // Amount and unit fields
    composeTestRule.onNodeWithTag("editFoodAmount").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editFoodUnit").assertIsDisplayed()

    // Location field
    composeTestRule.onNodeWithTag("editFoodLocation").assertIsDisplayed()

    // Dates fields
    composeTestRule.onNodeWithTag("editFoodExpireDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editFoodOpenDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editFoodBuyDate").assertIsDisplayed()

    // Buttons
    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSave").performScrollTo().assertIsDisplayed()

    // Delete Icon
    composeTestRule.onNodeWithTag("deleteFoodItem").assertIsDisplayed()

    // Since we have a selected item, selected image should be displayed
    composeTestRule.onNodeWithTag("selectedImageText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("selectedImage").assertIsDisplayed()
  }

  @Test
  fun cancelButtonNavigatesToOverview() {
    composeTestRule
        .onNodeWithTag("editFoodItemScreen")
        .performScrollToNode(hasTestTag("cancelButton"))
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    verify(navigationActions).navigateTo(eq(com.android.shelfLife.ui.navigation.Route.OVERVIEW))
  }

  @Test
  fun deleteButtonDeletesItemAndNavigatesToOverview() = runBlocking {
    composeTestRule.onNodeWithTag("deleteFoodItem").performClick()
    verify(navigationActions).navigateTo(eq(com.android.shelfLife.ui.navigation.Route.OVERVIEW))
  }

  @Test
  fun submitEmptyFieldsShowsErrorAndDoesNotNavigate() {
    // Clear amount field and input invalid data
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("abc") // invalid amount

    // Submit
    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performScrollTo().performClick()

    // Check error state in the ViewModel
    runBlocking { assert(foodItemViewModel.amountErrorResId != null) }

    // Verify navigation not called
    verify(navigationActions, never()).navigateTo(Screen.OVERVIEW)
  }

  @Test
  fun changeFieldsAndSubmitValidDataNavigatesBack() {
    // Input valid amount
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("100")

    // Change location
    composeTestRule.onNodeWithTag("editFoodLocation").performClick()
    composeTestRule.onNodeWithText("Fridge").performClick()

    // Enter valid dates
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextInput("20122024")

    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("20122026")

    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextInput("20122025")

    // Submit
    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performScrollTo().performClick()

    verify(navigationActions).navigateTo(eq(com.android.shelfLife.ui.navigation.Route.OVERVIEW))
  }

  @Test
  fun testUnitIsDisplayedAsNonEditable() {
    composeTestRule.onNodeWithTag("editFoodUnit").assertIsDisplayed()
    composeTestRule.onNodeWithText(foodItemViewModel.unit.name).assertIsDisplayed()
  }

  @Test
  fun testChangingFieldsUpdatesViewModelState() = runBlocking {
    // Change amount
    composeTestRule.onNodeWithTag("editFoodAmount").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("250")
    assert(foodItemViewModel.amount == "250")

    // Change Dates
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("20241231")
    assert(foodItemViewModel.expireDate == "20241231")

    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodOpenDate").performTextInput("20241230")
    assert(foodItemViewModel.openDate == "20241230")

    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextInput("20240101")
    assert(foodItemViewModel.buyDate == "20240101")
  }

  @Test
  fun testLocationDropdownExpansionAndSelection() {
    assert(foodItemViewModel.location != null)

    composeTestRule.onNodeWithTag("editFoodLocation").performClick()

    // Select a different location, e.g., "Pantry"
    composeTestRule.onNodeWithText("Pantry").performClick()
    assert(foodItemViewModel.location.name == "PANTRY")
  }

  @Test
  fun testInvalidDateShowsError() = runBlocking {
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextInput("00000000") // invalid

    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performScrollTo().performClick()

    assert(foodItemViewModel.buyDateErrorResId != null)
  }

  @Test
  fun testSelectedImageDisplayedIfSelectedFoodNotNull() {
    // Since we set a selectedFoodItem, isSelected should be true and selectedImage not null
    // Check if image related nodes appear
    composeTestRule.onNodeWithTag("selectedImageText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("selectedImage").assertIsDisplayed()
  }
}
