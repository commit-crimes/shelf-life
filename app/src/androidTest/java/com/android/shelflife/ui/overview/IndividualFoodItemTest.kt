package com.android.shelfLife.ui.overview

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
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
import com.google.firebase.Timestamp
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class IndividualFoodItemScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var foodItem: FoodItem
  private lateinit var houseHold: HouseHold
  private lateinit var navController: NavHostController

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // Initialize the class-level navController
    navController = TestNavHostController(context)
    navController.navigatorProvider.addNavigator(ComposeNavigator())

    // Initialize NavigationActions with the properly initialized navController
    navigationActions = NavigationActions(navController)
    houseHoldRepository = mock()
    val foodItemRepository = mock<FoodItemRepository>()
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
    householdViewModel = HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel)

    // Create a sample FoodItem
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
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            openDate = Timestamp(Date(System.currentTimeMillis() - 86400000)),
            buyDate = Timestamp(Date(System.currentTimeMillis() - 172800000)))

    // Create a sample HouseHold with the food item
    houseHold =
        HouseHold(
            uid = "household1",
            name = "Test Household",
            members = listOf("User1"),
            foodItems = listOf(foodItem))

    householdViewModel.setHouseholds(listOf(houseHold))
    householdViewModel.selectHousehold(houseHold)
  }

  @Test
  fun testSetSelectedFoodItemById() = runTest {
    // Select the food item
    householdViewModel.setSelectedFoodItemById(foodItem)

    // Assert that the selected food item is set correctly
    val selectedFoodItem = householdViewModel.selectedFoodItem.value
    assert(selectedFoodItem?.uid == "foodItem1")
    assert(selectedFoodItem?.foodFacts?.name == "Apple")
  }

  @Test
  fun individualFoodItemScreenDisplaysCorrectly() = runTest {
    // Set the selected food item
    householdViewModel.setSelectedFoodItemById(foodItem)

    composeTestRule.setContent {
      IndividualFoodItemScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Check if the screen displays the correct food item details
    composeTestRule.onNodeWithTag("IndividualFoodItemName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("IndividualFoodItemName").assertTextEquals("Apple")
    composeTestRule.onNodeWithTag("IndividualFoodItemImage").assertIsDisplayed()
  }

  @Test
  fun individualFoodItemScreenShowsLoadingIndicatorWhenNoFoodItemSelected() = runTest {
    // Ensure no food item is selected
    householdViewModel.setSelectedFoodItemById(null)

    composeTestRule.setContent {
      IndividualFoodItemScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Verify that the loading indicator is displayed
    composeTestRule.onNodeWithTag("CircularProgressIndicator").assertIsDisplayed()
  }

  @Test
  fun testBackButtonNavigatesBack() = runTest {
    // Set the selected food item
    householdViewModel.setSelectedFoodItemById(foodItem)

    composeTestRule.setContent {
      IndividualFoodItemScreen(
          navigationActions = navigationActions, householdViewModel = householdViewModel)
    }

    // Perform click on the back button
    composeTestRule.onNodeWithTag("IndividualTestScreenGoBack").performClick()

    // Verify that the navigation action was triggered
    assert(navController.currentBackStackEntry == null)
  }
}
