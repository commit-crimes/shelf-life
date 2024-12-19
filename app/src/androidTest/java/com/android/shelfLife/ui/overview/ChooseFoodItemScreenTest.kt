package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodItemRepositoryTestHelper
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class ChooseFoodItemScreenTest {
  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var navigationActions: NavigationActions

  @Inject lateinit var listFoodItemsRepository: FoodItemRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var foodFactsRepository: FoodFactsRepository

  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper

  private lateinit var foodItemViewModel: FoodItemViewModel

  private lateinit var instrumentationContext: android.content.Context

  // Mock for searchStatus and foodFactsSuggestions flows
  private val searchStatusFlow = MutableStateFlow<SearchStatus>(SearchStatus.Success)
  private val foodFactsSuggestionsFlow =
      MutableStateFlow<List<FoodFacts>>(
          listOf(
              FoodFacts(
                  name = "Apple", quantity = Quantity(1.0, FoodUnit.COUNT), imageUrl = "url1"),
              FoodFacts(
                  name = "Banana", quantity = Quantity(2.0, FoodUnit.COUNT), imageUrl = "url2")))

  private lateinit var houseHold: HouseHold
  private lateinit var foodItem: FoodItem

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
    navigationActions = mock()

    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(listFoodItemsRepository)
    instrumentationContext = InstrumentationRegistry.getInstrumentation().context

    whenever(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    // Mock repository flows
    whenever(foodFactsRepository.searchStatus).thenReturn(searchStatusFlow.asStateFlow())
    whenever(foodFactsRepository.foodFactsSuggestions)
        .thenReturn(foodFactsSuggestionsFlow.asStateFlow())

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
    foodItemViewModel =
        FoodItemViewModel(listFoodItemsRepository, userRepository, foodFactsRepository)
  }

  @Test
  fun testTitleDisplayed() {

    composeTestRule.setContent {
      ChooseFoodItem(navigationActions = navigationActions, foodItemViewModel)
    }

    composeTestRule.onNodeWithText("Choose Food Item Image").assertExists().assertIsDisplayed()
  }

  @Test
  fun testSelectImageTextDisplayed() {

    composeTestRule.setContent {
      ChooseFoodItem(navigationActions = navigationActions, foodItemViewModel)
    }

    composeTestRule.onNodeWithText("Select an Image:").assertExists().assertIsDisplayed()
  }

  @Test
  fun testCancelButtonClearsSelectionAndNavigatesBack() {
    composeTestRule.setContent {
      ChooseFoodItem(navigationActions = navigationActions, foodItemViewModel)
    }

    composeTestRule.onNodeWithTag("cancelButton").performClick()
    verify(navigationActions).navigateTo(Route.OVERVIEW)
  }

  @Test
  fun testSubmitButtonDisabledWhileLoading() {
    composeTestRule.setContent {
      ChooseFoodItem(navigationActions = navigationActions, foodItemViewModel)
    }

    searchStatusFlow.value = SearchStatus.Loading
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()

    searchStatusFlow.value = SearchStatus.Success
  }

  @Test
  fun testSubmitButtonNavigatesToEditFoodWhenImageSelected() {
    composeTestRule.setContent {
      ChooseFoodItem(navigationActions = navigationActions, foodItemViewModel)
    }
    composeTestRule.onNodeWithTag("foodSave").assertIsEnabled().performClick()
  }

  @Test
  fun testLazyGridDisplaysFoodItems() {
    composeTestRule.setContent {
      ChooseFoodItem(navigationActions = navigationActions, foodItemViewModel = foodItemViewModel)
    }

    // Verify items are displayed
    composeTestRule.onAllNodesWithTag("foodImage", useUnmergedTree = true).assertCountEquals(2)

    composeTestRule
        .onAllNodesWithTag("IndividualFoodItemImage", useUnmergedTree = true)
        .assertCountEquals(2)

    composeTestRule
        .onNodeWithTag("noImage", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("uploadImage", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun testNoImageOptionDisplayed() {
    composeTestRule.setContent {
      ChooseFoodItem(navigationActions = navigationActions, foodItemViewModel = foodItemViewModel)
    }
    // No need for Thread.sleep; ComposeTestRule waits for UI updates by default
    composeTestRule
        .onNodeWithTag("noImageText", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("noImage", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun testSubmitButtonSubmitsSelectedImage() {
    composeTestRule.setContent {
      ChooseFoodItem(navigationActions = navigationActions, foodItemViewModel = foodItemViewModel)
    }
    // Verify items are displayed
    composeTestRule.onAllNodesWithTag("foodImage", useUnmergedTree = true)[0].performClick()
    // Verify that the submit button is enabled
    composeTestRule
        .onNodeWithTag("foodSave", useUnmergedTree = true)
        .assertExists()
        .assertIsEnabled()

    // Perform click action on the "Submit" button
    composeTestRule.onNodeWithTag("foodSave", useUnmergedTree = true).performClick()

    // Verify the food item is set and navigation occurs
    verify(navigationActions).navigateTo(Screen.EDIT_FOOD)
  }
}
