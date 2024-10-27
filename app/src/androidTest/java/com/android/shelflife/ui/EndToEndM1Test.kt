package com.android.shelfLife.ui.overview

import android.content.Context
import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.google.firebase.Timestamp
import io.mockk.mockk
import java.util.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class EndToEndM1Test {

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var householdViewModel: HouseholdViewModel

  private lateinit var navController: NavHostController
  private lateinit var houseHold: HouseHold

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Initialize the class-level navController
    navController = TestNavHostController(context)
    navController.navigatorProvider.addNavigator(ComposeNavigator())

    // Initialize NavigationActions with the properly initialized navController
    navigationActions = NavigationActions(navController)

    // Initialize repositories and view models
    foodItemRepository = mock(FoodItemRepository::class.java)
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
    houseHoldRepository = mock(HouseHoldRepository::class.java)
    householdViewModel = HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel)

    // Set up test data (household and food item)
    val foodFacts = FoodFacts(
      name = "Apple",
      barcode = "123456789",
      quantity = Quantity(5.0, FoodUnit.COUNT),
      category = FoodCategory.FRUIT
    )
    val foodItem = FoodItem(
      uid = "foodItem1",
      foodFacts = foodFacts,
      expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000))  // Expires in 1 day
    )

    houseHold = HouseHold(
      uid = "1",
      name = "Test Household",
      members = listOf("John", "Doe"),
      foodItems = listOf(foodItem)
    )

    // Mock the household repository to return the initial household
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


//  @Test
//  fun notGoingCrazy(){
//    householdViewModel.selectHousehold(houseHold)
//    composeTestRule.setContent {
//      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
//    }
//
//    // Click on the add food FAB
//    composeTestRule.onNodeWithTag("addFoodFab").performClick()
//
//    // Verify that navigateTo(Screen.ADD_FOOD) was called
//    //verify(navigationActions).navigateTo(com.android.shelfLife.ui.navigation.Screen.ADD_FOOD)
//  }


  @Test
  fun testEndToEndFlow() {
    //Second as a new user a new household is created


    householdViewModel.selectHousehold(houseHold)
//    composeTestRule.setContent {
//      OverviewScreen(navigationActions = navigationActions, householdViewModel = householdViewModel)
//    }
    composeTestRule.setContent {
      NavHost(
        navController = navController,
        startDestination = Route.OVERVIEW
      ) {
        composable(Route.OVERVIEW) { OverviewScreen(navigationActions, householdViewModel) }
        composable(Screen.ADD_FOOD) { AddFoodItemScreen(navigationActions, householdViewModel, listFoodItemsViewModel) }
      }
    }
    //User is now on the overview Screen
    //composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    //User wants to see the amount households they have access to
//    composeTestRule.onNodeWithTag("hamburgerIcon").performClick()
//    composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()

    //User wants to add a new food item

    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodFab").assertHasClickAction()
    composeTestRule.onNodeWithTag("addFoodFab").performClick()
    //verify(navigationActions).navigateTo(com.android.shelfLife.ui.navigation.Screen.ADD_FOOD)

    var found = false
    repeat(5) {
      try {
        composeTestRule.onNodeWithTag("addFoodItemTitle").assertExists()
        found = true
        return
      } catch (e: AssertionError) {
        Thread.sleep(500)  // Wait a bit and retry
        composeTestRule.waitForIdle()
      }
    }
    if (!found) throw AssertionError("addFoodItemTitle not found")

    composeTestRule.onNodeWithTag("addFoodItemTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("29/12/2023")
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01/12/2023")
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("30/11/2023")

    composeTestRule.onNodeWithTag("foodSave").performClick()

    //composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
  }

//  @Test
//  fun testDirectAddFoodItemScreenRender() {
//    composeTestRule.setContent {
//      AddFoodItemScreen(navigationActions, householdViewModel, listFoodItemsViewModel)
//    }
//
//    // Verify if the tag is displayed directly
//    composeTestRule.onNodeWithTag("addFoodItemTitle").assertIsDisplayed()
//  }



}
