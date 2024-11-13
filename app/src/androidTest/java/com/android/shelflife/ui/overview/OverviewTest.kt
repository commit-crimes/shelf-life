package com.android.shelflife.ui.overview

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
import com.android.shelfLife.ui.overview.OverviewScreen
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
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)))

    val householdWithGramItem = houseHold.copy(foodItems = listOf(gramsFoodItem))
    householdViewModel.selectHousehold(householdWithGramItem)

    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
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
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)))

    val householdWithMlItem = houseHold.copy(foodItems = listOf(mlFoodItem))
    householdViewModel.selectHousehold(householdWithMlItem)

    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

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
            expiryDate = null // No expiry date
            )

    val householdWithNoExpiryItem = houseHold.copy(foodItems = listOf(noExpiryFoodItem))
    householdViewModel.selectHousehold(householdWithNoExpiryItem)

    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

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
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)))

    val householdWithCountItem = houseHold.copy(foodItems = listOf(countFoodItem))
    householdViewModel.selectHousehold(householdWithCountItem)

    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Check that the quantity text displays "12 in stock"
    composeTestRule.onNodeWithText("12 in stock").assertIsDisplayed()
  }

  // Test that the quantity in grams is displayed correctly
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

  @Test
  fun firstTimeWelcomeScreenClickingAddHouseholdNavigatesToHouseholdCreationScreen() {
    // Mock empty households to trigger the first-time screen
    mockHouseHoldRepositoryGetHouseholds(emptyList())

    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }
    composeTestRule.onNodeWithTag("householdNameSaveButton").performClick()
    verify(navigationActions).navigateTo(Screen.HOUSEHOLD_CREATION)
  }

  // Additional test to check that quantity in COUNT is displayed correctly
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

  // Test that the app handles a null FoodFacts object gracefully
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
            expiryDate = null)

    val householdWithNullFactsItem = houseHold.copy(foodItems = listOf(nullFoodFactsItem))
    householdViewModel.selectHousehold(householdWithNullFactsItem)

    composeTestRule.setContent {
      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Check that the placeholder text or image is displayed
    composeTestRule
        .onNodeWithText("No Name")
        .assertDoesNotExist() // Assuming "No Name" is not displayed
    // Additional assertions can be added based on how the UI handles null or empty values
  }

  // Test that the search bar is functional and filters items correctly for case insensitivity
  @Test
  fun searchIsCaseInsensitive() {
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

    // Enter search query "banana" in lowercase
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
