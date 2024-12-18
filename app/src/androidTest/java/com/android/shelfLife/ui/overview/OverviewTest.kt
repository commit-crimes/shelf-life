package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
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
class OverviewTest {

  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var navigationActions: NavigationActions

  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var listFoodItemsRepository: FoodItemRepository
  @Inject lateinit var userRepository: UserRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper

  private lateinit var overviewScreenViewModel: OverviewScreenViewModel

  private lateinit var instrumentationContext: android.content.Context

  private lateinit var houseHold: HouseHold
  private lateinit var foodItem: FoodItem

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
    navigationActions = mock()

    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)
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

    householdRepositoryTestHelper.selectHousehold(houseHold)

    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))
    overviewScreenViewModel =
        OverviewScreenViewModel(
            houseHoldRepository, listFoodItemsRepository, userRepository, instrumentationContext)
  }

  @Test
  fun foodItemWithGramsDisplaysCorrectQuantity() {
    val gramsFoodFacts =
        FoodFacts(
            name = "Flour",
            barcode = "111222333",
            quantity = Quantity(500.0, FoodUnit.GRAM),
            category = FoodCategory.GRAIN)
    val gramsFoodItem =
        FoodItem(
            uid = "foodItemGram",
            foodFacts = gramsFoodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            owner = "testOwner")

    foodItemRepositoryTestHelper.setFoodItems(listOf(gramsFoodItem))

    composeTestRule.setContent {
      OverviewScreen(
          navigationActions = navigationActions,
      )
    }

    // Check that the quantity text displays "500g"
    composeTestRule.onNodeWithText("500g").assertIsDisplayed()
  }

  // Test that the quantity in milliliters is displayed correctly
  @Test
  fun foodItemWithMillilitersDisplaysCorrectQuantity() {
    val mlFoodFacts =
        FoodFacts(
            name = "Milk",
            barcode = "444555666",
            quantity = Quantity(1000.0, FoodUnit.ML),
            category = FoodCategory.DAIRY)
    val mlFoodItem =
        FoodItem(
            uid = "foodItemMl",
            foodFacts = mlFoodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            owner = "testOwner")

    foodItemRepositoryTestHelper.setFoodItems(listOf(mlFoodItem))
    householdRepositoryTestHelper.selectHousehold(houseHold)

    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Check that the quantity text displays "1000ml"
    composeTestRule.onNodeWithText("1000ml").assertIsDisplayed()
  }

  // Test that "No Expiry Date" is displayed when expiry date is null
  @Test
  fun foodItemWithoutExpiryDateDoesNotDisplayNoExpiryDate() {
    val noExpiryFoodFacts =
        FoodFacts(
            name = "Canned Beans",
            barcode = "777888999",
            quantity = Quantity(2.0, FoodUnit.COUNT),
            category = FoodCategory.OTHER)
    val noExpiryFoodItem =
        FoodItem(
            uid = "foodItemNoExpiry",
            foodFacts = noExpiryFoodFacts,
            expiryDate = null, // No expiry date,
            owner = "testOwner")

    foodItemRepositoryTestHelper.setFoodItems(listOf(noExpiryFoodItem))
    householdRepositoryTestHelper.selectHousehold(houseHold)

    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Check that the text "No Expiry Date" is displayed
    composeTestRule.onNodeWithText("No Expiry Date").assertIsNotDisplayed()
  }

  // Additional test to check that quantity in COUNT is displayed correctly
  @Test
  fun foodItemWithCountDisplaysCorrectQuantity() {
    val countFoodFacts =
        FoodFacts(
            name = "Eggs",
            barcode = "123123123",
            quantity = Quantity(12.0, FoodUnit.COUNT),
            category = FoodCategory.DAIRY)
    val countFoodItem =
        FoodItem(
            uid = "foodItemCount",
            foodFacts = countFoodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            owner = "testOwner")

    foodItemRepositoryTestHelper.setFoodItems(listOf(countFoodItem))
    householdRepositoryTestHelper.selectHousehold(houseHold)

    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Check that the quantity text displays "12 in stock"
    composeTestRule.onNodeWithText("12 in stock").assertIsDisplayed()
  }

  @Test
  fun clickingDeleteTextInSearchBarDeletesText() {
    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Click on the search bar
    composeTestRule.onNodeWithTag("foodSearchBar").performClick()
    composeTestRule.waitForIdle()

    // Enter search query "Apple"
    composeTestRule.onNodeWithTag("searchBarInputField").performTextInput("Apple")

    // Check that the search query is "Apple"
    composeTestRule.onNodeWithTag("searchBarInputField").assertTextEquals("Apple")

    // Click on the delete text icon
    composeTestRule.onNodeWithTag("deleteTextButton").performClick()
    composeTestRule.waitForIdle()

    // Check that the search query is empty
    composeTestRule
        .onNode(hasSetTextAction() and hasTestTag("searchBarInputField") and hasText(""))
        .assertExists()
  }

  // Test that the quantity in grams is displayed correctly
  @Test
  fun firstTimeWelcomeScreenNavigationTriggeredWhenHouseholdsAreEmpty() {
    // Mock empty households to trigger the first-time screen
    householdRepositoryTestHelper.selectHousehold(null)

    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    verify(navigationActions).navigateTo(Screen.FIRST_TIME_USER)
  }

  // Test if the OverviewScreen is displayed with all elements
  // Additional test to check that quantity in COUNT is displayed correctly
  @Test
  fun overviewScreenDisplayedCorrectly() {
    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSearchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed()
  }

  // Clicking on hamburger icon opens the household selection drawer
  @Test
  fun clickHamburgerIconOpensHouseholdSelectionDrawer() {
    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
    composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
  }

  // Clicking on add icon in the drawer opens the add household popup
  @Test
  fun clickAddInDrawerOpensHouseholdCreationScreen() {
    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
    composeTestRule.onNodeWithTag("addHouseholdIcon").performClick()
    verify(navigationActions).navigateTo(Screen.HOUSEHOLD_CREATION)
  }

  // Test that the food item list is displayed when food items exist
  @Test
  fun foodItemListIsDisplayedWhenFoodItemsExist() {
    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Check that the food item list is displayed
    composeTestRule.onNodeWithTag("foodItemList").assertIsDisplayed()

    // Check that the food item card is displayed
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(1)
    composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
  }

  @Test
  fun foodItemWithNullFoodFactsDisplaysPlaceholder() {
    val nullFoodFactsItem =
        FoodItem(
            uid = "foodItemNullFacts",
            foodFacts =
                FoodFacts(
                    name = "",
                    barcode = "",
                    quantity = Quantity(0.0, FoodUnit.COUNT),
                    category = FoodCategory.OTHER),
            expiryDate = null,
            owner = "testOwner")

    foodItemRepositoryTestHelper.setFoodItems(listOf(nullFoodFactsItem))

    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Check that the placeholder text or image is displayed
    composeTestRule
        .onNodeWithText("No Name")
        .assertDoesNotExist() // Assuming "No Name" is not displayed
    // Additional assertions can be added based on how the UI handles null or empty values
  }

  // Test that "No food available" message is displayed when no food items exist
  @Test
  fun noFoodAvailableMessageIsDisplayedWhenNoFoodItems() {
    foodItemRepositoryTestHelper.setFoodItems(emptyList())

    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Check that the "No food available" message is displayed
    composeTestRule.onNodeWithTag("NoFoodItems").assertIsDisplayed()
    composeTestRule.onNodeWithText("No food available").assertIsDisplayed()
  }

  @Test
  fun searchFiltersFoodItemList() {
    // Add a second food item
    val bananaFoodFacts =
        FoodFacts(
            name = "Banana",
            barcode = "987654321",
            quantity = Quantity(3.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    val bananaFoodItem =
        FoodItem(
            uid = "foodItem2",
            foodFacts = bananaFoodFacts,
            expiryDate =
                Timestamp(Date(System.currentTimeMillis() + 172800000)), // Expires in 2 days
            owner = "testOwner")

    val householdWithMultipleItems = houseHold.copy(members = listOf("Jane", "Doe"))
    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem, bananaFoodItem))

    householdRepositoryTestHelper.selectHousehold(householdWithMultipleItems)
    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Initially, both items should be displayed
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(2)

    // Activate the SearchBar
    composeTestRule.onNodeWithTag("foodSearchBar").performClick()
    composeTestRule.waitForIdle()

    // Enter search query "Banana"
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("foodSearchBar")))
        .performTextInput("banana")

    // Only Banana should be displayed
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(1)

    // Assert that the displayed FoodItemCard contains the text "Banana"
    composeTestRule.onNodeWithText("Banana").assertIsDisplayed()
  }

  // Test that the app handles an empty FoodItem list gracefully
  @Test
  fun emptyFoodItemListDisplaysNoFoodAvailable() {
    foodItemRepositoryTestHelper.setFoodItems(emptyList())

    householdRepositoryTestHelper.selectHousehold(houseHold)

    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Check that the "No food available" message is displayed
    composeTestRule.onNodeWithTag("NoFoodItems").assertIsDisplayed()
    composeTestRule.onNodeWithText("No food available").assertIsDisplayed()
  }

  // Test that the floating action button navigates to the add food screen
  @Test
  fun clickAddFoodFabNavigatesToAddFoodScreen() {
    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.setContent { OverviewScreen(navigationActions = navigationActions) }

    // Click on the add food FAB
    composeTestRule.onNodeWithTag("addFoodFab").performClick()
      composeTestRule.onNodeWithTag("addFoodFab").performClick()

    // Verify that navigateTo(Screen.ADD_FOOD) was called
    verify(navigationActions).navigateTo(Screen.ADD_FOOD)
  }

  @Test
  fun selectAndDeleteFoodItems() {

    // Create multiple food items
    val appleFoodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    val appleFoodItem =
        FoodItem(
            uid = "foodItem1",
            foodFacts = appleFoodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            owner = "testOwner")

    val bananaFoodFacts =
        FoodFacts(
            name = "Banana",
            barcode = "987654321",
            quantity = Quantity(3.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    val bananaFoodItem =
        FoodItem(
            uid = "foodItem2",
            foodFacts = bananaFoodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            owner = "testOwner")

    foodItemRepositoryTestHelper.setFoodItems(listOf(appleFoodItem, bananaFoodItem))

    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, overviewScreenViewModel)
    }

    // Initially, both items should be displayed
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(2)

    // Long press to select the first item
    composeTestRule.onAllNodesWithTag("foodItemCard")[0].performTouchInput { longClick() }
    composeTestRule.onNodeWithTag("deleteFoodItems").assertIsDisplayed()
    assertEquals(1, overviewScreenViewModel.multipleSelectedFoodItems.value.size)
    // Long press to select the second item
    composeTestRule.onAllNodesWithTag("foodItemCard")[1].performTouchInput { longClick() }
    assertEquals(2, overviewScreenViewModel.multipleSelectedFoodItems.value.size)
    composeTestRule.onNodeWithTag("deleteFoodItems").assertIsEnabled().performClick()
    assertEquals(0, overviewScreenViewModel.multipleSelectedFoodItems.value.size)
  }

  @Test
  fun selectAndDeselectFoodItems() {
    // Create multiple food items
    val appleFoodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    val appleFoodItem =
        FoodItem(
            uid = "foodItem1",
            foodFacts = appleFoodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            owner = "testOwner")

    val bananaFoodFacts =
        FoodFacts(
            name = "Banana",
            barcode = "987654321",
            quantity = Quantity(3.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    val bananaFoodItem =
        FoodItem(
            uid = "foodItem2",
            foodFacts = bananaFoodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            owner = "testOwner")

    foodItemRepositoryTestHelper.setFoodItems(listOf(appleFoodItem, bananaFoodItem))

    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, overviewScreenViewModel)
    }

    // Initially, both items should be displayed
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(2)

    // Long press to select the first item
    composeTestRule.onAllNodesWithTag("foodItemCard")[0].performTouchInput { longClick() }
    assertEquals(1, overviewScreenViewModel.multipleSelectedFoodItems.value.size)

    // Long press to select the second item
    composeTestRule.onAllNodesWithTag("foodItemCard")[1].performTouchInput { longClick() }
    assertEquals(2, overviewScreenViewModel.multipleSelectedFoodItems.value.size)

    // Long press again to deselect the first item
    composeTestRule.onAllNodesWithTag("foodItemCard")[0].performTouchInput { longClick() }
    assertEquals(1, overviewScreenViewModel.multipleSelectedFoodItems.value.size)

    // Long press again to deselect the second item
    composeTestRule.onAllNodesWithTag("foodItemCard")[1].performTouchInput { longClick() }
    assertEquals(0, overviewScreenViewModel.multipleSelectedFoodItems.value.size)
  }
}
