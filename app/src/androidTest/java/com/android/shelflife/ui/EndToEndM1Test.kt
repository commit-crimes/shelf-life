package com.android.shelflife.ui

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.FoodSearchInput
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.ui.camera.BarcodeScannerScreen
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.overview.AddFoodItemScreen
import com.android.shelfLife.ui.overview.OverviewScreen
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.google.firebase.Timestamp
import io.mockk.every
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
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class EndToEndM1Test {

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var dataStore: DataStore<Preferences>
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var foodFactsViewModel: FoodFactsViewModel
  private lateinit var foodFactsRepository: FoodFactsRepository
  private lateinit var listRecipesViewModel: ListRecipesViewModel
  private lateinit var recipeRepository: RecipeRepository
  private lateinit var recipeGeneratorRepository: RecipeGeneratorRepository

  private lateinit var navController: NavHostController
  private lateinit var houseHold: HouseHold
  private lateinit var barcodeScannerViewModel: BarcodeScannerViewModel

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
    barcodeScannerViewModel = mockk(relaxed = true)
    foodItemRepository = mock(FoodItemRepository::class.java)
    listFoodItemsViewModel = ListFoodItemsViewModel(foodItemRepository)
    dataStore = org.mockito.kotlin.mock<DataStore<Preferences>>()

    recipeRepository = mock(RecipeRepository::class.java)
    recipeGeneratorRepository = mock(RecipeGeneratorRepository::class.java)
    listRecipesViewModel = ListRecipesViewModel(recipeRepository, recipeGeneratorRepository)

    houseHoldRepository = mock(HouseholdRepositoryFirestore::class.java)
    householdViewModel =
        HouseholdViewModel(
            houseHoldRepository as HouseholdRepositoryFirestore,
            listFoodItemsViewModel,
            invitationRepository = mockk<InvitationRepositoryFirestore>(),
            dataStore)

    foodFactsRepository = FakeFoodFactsRepository()
    foodFactsViewModel = FoodFactsViewModel(foodFactsRepository)

    `when`(foodItemRepository.getNewUid()).thenReturn("mockedUid")
    every { barcodeScannerViewModel.permissionGranted } returns true

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
  fun overviewScreenDisplayedCorrectly() {
    householdViewModel.setHouseholds(listOf(houseHold))
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      OverviewScreen(
          navigationActions = navigationActions,
          householdViewModel = householdViewModel,
          listFoodItemsViewModel = listFoodItemsViewModel)
    }

    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSearchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed()
  }

  @Test
  fun testEndToEndFlow() {
    householdViewModel.setHouseholds(listOf(houseHold))
    householdViewModel.selectHousehold(houseHold)
    composeTestRule.setContent {
      NavHost(navController = navController, startDestination = Route.OVERVIEW) {
        composable(Route.OVERVIEW) {
          OverviewScreen(navigationActions, householdViewModel, listFoodItemsViewModel)
        }
        composable(Screen.ADD_FOOD) {
          AddFoodItemScreen(
              navigationActions, householdViewModel, listFoodItemsViewModel, foodFactsViewModel)
        }
        composable(Route.SCANNER) {
          BarcodeScannerScreen(
              navigationActions = navigationActions,
              cameraViewModel = barcodeScannerViewModel,
              foodFactsViewModel = foodFactsViewModel,
              householdViewModel = householdViewModel,
              foodItemViewModel = listFoodItemsViewModel)
        }
        composable(Route.RECIPES) {
          RecipesScreen(navigationActions, listRecipesViewModel, householdViewModel)
        }
      }
    }

    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    // User is now on the overview Screen
    // User wants to add a new food item
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodFab").assertHasClickAction()
    composeTestRule.onNodeWithTag("addFoodFab").performClick()
    // Thread.sleep(1000)
    composeTestRule.onNodeWithTag("addFoodItemTitle").assertIsDisplayed()

    // Scroll to and interact with the input fields
    val scrollableNode = composeTestRule.onNodeWithTag("addFoodItemScreen")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodName"))
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodAmount"))
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("29102025")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodOpenDate"))
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01122025")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodBuyDate"))
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("30112025")

    // Scroll to and click the submit button
    scrollableNode.performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performClick()

    // Correct the expire date
    scrollableNode.performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("29122025")

    // Scroll to and click the submit button again
    scrollableNode.performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performClick()
    // Thread.sleep(1000)
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    // Thread.sleep(1000)
    // User now wants to use the scanner
    composeTestRule.onNodeWithTag("Scanner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Scanner").performClick()
    composeTestRule.onNodeWithTag("Scanner").performClick()
    // Thread.sleep(1000)
    composeTestRule.onNodeWithTag("barcodeScannerScreen").assertIsDisplayed()
    // User now want to check for the Recepie for Paella
    composeTestRule.onNodeWithTag("Recipes").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Recipes").performClick()
    // Thread.sleep(5000)
    composeTestRule.onNodeWithTag("recipesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Paella")
    composeTestRule.onAllNodesWithTag("recipesCards").assertCountEquals(1)
    composeTestRule
        .onNode(hasText("Paella") and hasAnyAncestor(hasTestTag("searchBar")))
        .assertIsDisplayed()
  }

  // Include the FakeFoodFactsRepository within the test class or as a nested class
  inner class FakeFoodFactsRepository : FoodFactsRepository {
    var shouldReturnError = false
    var foodFactsList = listOf<FoodFacts>()

    override fun searchFoodFacts(
      searchInput: FoodSearchInput,
      onSuccess: (List<FoodFacts>) -> Unit,
      onFailure: (Exception) -> Unit
    ) {
      if (shouldReturnError) {
        onFailure(Exception("Test exception"))
      } else {
        onSuccess(foodFactsList)
      }
    }
  }
}
