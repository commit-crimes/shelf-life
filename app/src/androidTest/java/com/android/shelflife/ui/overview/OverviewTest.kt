package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class OverviewTest {

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var householdViewModel: HouseholdViewModel

  private lateinit var houseHold: HouseHold

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock()
    foodItemRepository = mock()
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)

    houseHoldRepository = mock()
    householdViewModel = HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel)

    whenever(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)

    // Create a FoodItem to be used in tests
    val foodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    val foodItem =
        FoodItem(
            uid = "foodItem1",
            foodFacts = foodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)) // Expires in 1 day
            )

    // Initialize the household with the food item
    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            foodItems = listOf(foodItem))

    householdViewModel.finishedLoading.value = true

    // Mock the repository to return the initial household
    mockHouseHoldRepositoryGetHouseholds(listOf(houseHold))
  }

  private fun mockHouseHoldRepositoryGetHouseholds(households: List<HouseHold>) {
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[0] as (List<HouseHold>) -> Unit
          onSuccess(households)
          null
        }
        .whenever(houseHoldRepository)
        .getHouseholds(any(), any())
  }

  // Test if the FirstTimeWelcomeScreen and all its elements are displayed correctly
  @Test
  fun firstTimeWelcomeScreenDisplayedCorrectly() {
    // Mock empty households to trigger the first-time screen
    mockHouseHoldRepositoryGetHouseholds(emptyList())

    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    composeTestRule.onNodeWithTag("firstTimeWelcomeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("householdNameSaveButton").assertIsDisplayed()
  }

  // Test if the OverviewScreen is displayed with all elements
  @Test
  fun overviewScreenDisplayedCorrectly() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSearchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed()
  }

  // Clicking on hamburger icon opens the household selection drawer
  @Test
  fun clickHamburgerIconOpensHouseholdSelectionDrawer() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
    composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
  }

  /*
  @Test
  fun clickEditInDrawerLaunchesEditSelection() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()
      composeTestRule.onNodeWithTag("editHouseholdIndicatorIcon").assertIsDisplayed()
  }

     */

  // Clicking on add icon in the drawer opens the add household popup
  @Test
  fun clickAddInDrawerOpensHouseholdCreationScreen() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
    composeTestRule.onNodeWithTag("addHouseholdIcon").performClick()
    verify(navigationActions).navigateTo(Screen.HOUSEHOLD_CREATION)
  }

  // Test that the food item list is displayed when food items exist
  @Test
  fun foodItemListIsDisplayedWhenFoodItemsExist() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Check that the food item list is displayed
    composeTestRule.onNodeWithTag("foodItemList").assertIsDisplayed()

    // Check that the food item card is displayed
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(1)
    composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
  }

  // Test that "No food available" message is displayed when no food items exist
  @Test
  fun noFoodAvailableMessageIsDisplayedWhenNoFoodItems() {
    val emptyHousehold = houseHold.copy(foodItems = emptyList())

    // Mock the repository to return the household with no food items
    mockHouseHoldRepositoryGetHouseholds(listOf(emptyHousehold))

    householdViewModel.selectHousehold(emptyHousehold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

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
                Timestamp(Date(System.currentTimeMillis() + 172800000)) // Expires in 2 days
            )

    val householdWithMultipleItems =
        houseHold.copy(
            members = listOf("Jane", "Doe"),
            foodItems = listOf(bananaFoodItem, houseHold.foodItems[0]))

    // Mock the repository to return the household with multiple food items
    mockHouseHoldRepositoryGetHouseholds(listOf(householdWithMultipleItems))

    householdViewModel.selectHousehold(householdWithMultipleItems)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Initially, both items should be displayed
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(2)

    // Activate the SearchBar
    composeTestRule.onNodeWithTag("foodSearchBar").performClick()
    composeTestRule.waitForIdle()

    // Enter search query "Banana"
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("foodSearchBar")))
        .performTextInput("Banana")

    // Only Banana should be displayed
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(1)

    // Assert that the displayed FoodItemCard contains the text "Banana"
    composeTestRule
        .onNode(hasText("Banana") and hasAnyAncestor(hasTestTag("foodSearchBar")))
        .assertIsDisplayed()
  }

  // Test that the floating action button navigates to the add food screen
  @Test
  fun clickAddFoodFabNavigatesToAddFoodScreen() {
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Click on the add food FAB
    composeTestRule.onNodeWithTag("addFoodFab").performClick()

    // Verify that navigateTo(Screen.ADD_FOOD) was called
    verify(navigationActions).navigateTo(com.android.shelfLife.ui.navigation.Screen.ADD_FOOD)
  }
}
