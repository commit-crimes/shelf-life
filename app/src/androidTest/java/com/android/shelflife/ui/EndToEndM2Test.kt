package com.android.shelflife.ui

import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.idling.CountingIdlingResource
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
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.ui.camera.BarcodeScannerScreen
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.overview.AddFoodItemScreen
import com.android.shelfLife.ui.overview.EditFoodItemScreen
import com.android.shelfLife.ui.overview.HouseHoldCreationScreen
import com.android.shelfLife.ui.overview.IndividualFoodItemScreen
import com.android.shelfLife.ui.overview.OverviewScreen
import com.android.shelfLife.ui.profile.ProfileScreen
import com.android.shelfLife.ui.recipes.AddRecipeScreen
import com.android.shelfLife.ui.recipes.IndividualRecipeScreen
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
class EndToEndM2Test {

  private lateinit var foodItemRepository: FoodItemRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listFoodItemsViewModel: ListFoodItemsViewModel
  private lateinit var houseHoldRepository: HouseHoldRepository
  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var foodFactsViewModel: FoodFactsViewModel
  private lateinit var foodFactsRepository: FakeFoodFactsRepository
  private lateinit var listRecipesViewModel: ListRecipesViewModel

  private lateinit var navController: NavHostController
  private lateinit var houseHold: HouseHold
  private lateinit var barcodeScannerViewModel: BarcodeScannerViewModel
  private val idlingResource = CountingIdlingResource("DataLoader")

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
    houseHoldRepository = mock(HouseHoldRepository::class.java)
    householdViewModel = HouseholdViewModel(houseHoldRepository, listFoodItemsViewModel)
    listRecipesViewModel = ListRecipesViewModel()

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

    foodFactsRepository.foodFactsList = listOf(foodFacts)
    householdViewModel.finishedLoading.value = true
    doAnswer { invocation ->
          val callback = invocation.arguments[0] as (Map<String, String>) -> Unit
          callback(mapOf("dogwaterson@gmail.com" to "mockedUserId"))
          null
        }
        .whenever(houseHoldRepository)
        .getUserEmails(any(), any())
    var signOutCalled = false
    val signOutUser = { signOutCalled = true }
    val account =
        mockk<GoogleSignInAccount>(
            block = {
              every { email } returns "test@example.com"
              every { photoUrl } returns
                  Uri.parse(
                      "https://letsenhance.io/static/8f5e523ee6b2479e26ecc91b9c25261e/1015f/MainAfter.jpg")
            })
    composeTestRule.setContent {
      NavHost(navController = navController, startDestination = Route.OVERVIEW) {
        composable(Route.OVERVIEW) {
          OverviewScreen(navigationActions, householdViewModel, listFoodItemsViewModel)
        }
        composable(Screen.ADD_FOOD) {
          AddFoodItemScreen(navigationActions, householdViewModel, listFoodItemsViewModel)
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
        composable(Route.PROFILE) {
          ProfileScreen(
              navigationActions = navigationActions,
              account = account,
              signOutUser = signOutUser,
              householdViewModel = householdViewModel)
        }
        composable(Screen.HOUSEHOLD_CREATION) {
          HouseHoldCreationScreen(navigationActions, householdViewModel = householdViewModel)
        }
        composable(Screen.INDIVIDUAL_FOOD_ITEM) {
          IndividualFoodItemScreen(
              navigationActions = navigationActions, foodItemViewModel = listFoodItemsViewModel)
        }
        composable(Route.RECIPES) {
          RecipesScreen(navigationActions, listRecipesViewModel, householdViewModel)
        }
        composable(Screen.INDIVIDUAL_RECIPE) {
          IndividualRecipeScreen(navigationActions, listRecipesViewModel, householdViewModel)
        }
        composable(Screen.ADD_RECIPE) {
          AddRecipeScreen(navigationActions, listRecipesViewModel, householdViewModel)
        }
        composable(Screen.EDIT_FOOD) {
          EditFoodItemScreen(
              navigationActions = navigationActions,
              houseHoldViewModel = householdViewModel,
              foodItemViewModel = listFoodItemsViewModel)
        }
      }
    }

    // Mock the repository to return the initial household
    mockHouseHoldRepositoryGetHouseholds(listOf(houseHold))
    householdViewModel.selectHousehold(houseHold)
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

  // In this test an user tries to manually add a food item to their household and later not
  // satisfied with the manual approach tries rather to scan the item.
  @Test
  fun testEndToEnd_see_add_food_item() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    // User is now on the overview Screen
    // User wants to add a new food item
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("addFoodItemTitle").assertIsDisplayed()
    // Scroll to and interact with the input fields
    val scrollableNode = composeTestRule.onNodeWithTag("addFoodItemScreen")
    // Inputs all the relevant data about the food item
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
    // Verify error message is displayed
    composeTestRule.onNodeWithText("Expire Date cannot be before Buy Date").assertIsDisplayed()
    // Correct the expire date
    scrollableNode.performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("29122025")
    // Scroll to and click the submit button again
    scrollableNode.performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    // Goes and Scans the item
    composeTestRule.onNodeWithTag("Scanner").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("barcodeScannerScreen").assertIsDisplayed()
    composeTestRule.runOnUiThread { foodFactsViewModel.searchByBarcode(1234567890L) }
    composeTestRule.onNodeWithTag("locationDropdown").performClick()
    composeTestRule.onNodeWithTag("locationOption_PANTRY").performClick()
    composeTestRule.onNodeWithTag("locationTextField").assertTextContains("pantry")
    composeTestRule.onNodeWithTag("expireDateTextField").performTextInput("29122024")
    composeTestRule.onNodeWithTag("submitButton").performClick()
  }

  // In this test the user wants to first add a friend into a new household
  @Test
  fun testEndToEnd_add_friend() {
    // User goes and navigates to the Household drawer to create a new household
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("addHouseholdIcon").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextClearance()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("My House Rocks")
    composeTestRule.onNodeWithTag("EmailInputField").performTextClearance()
    composeTestRule.onNodeWithTag("EmailInputField").performTextInput("dogwaterson@gmail.com")
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()
  }

  // In this test the user searches for an food item, clicks on it to see all its fields and edits
  // some of them.
  @Test
  fun testEndToEnd_filter_and_individualFood_flow() {
    // Goes and searches for the food item
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSearchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodSearchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("foodSearchBar")))
        .performTextInput("Apple")
    composeTestRule.onAllNodesWithTag("foodItemCard").assertCountEquals(1)
    composeTestRule.onNodeWithTag("foodItemCard").performClick()
    // Goes into its individual food item page
    composeTestRule.onNodeWithTag("IndividualFoodItemScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodItemDetailsCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("IndividualFoodItemImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editFoodFab").assertIsDisplayed().performClick()
    // Goes into to edit page
    composeTestRule.onNodeWithTag("editFoodAmount").performTextInput("5")
    composeTestRule.onNodeWithTag("editFoodLocation").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("editFoodExpireDate").performTextInput("31122030") // Future date
    // Clear and re-enter buy date to ensure it's valid
    composeTestRule.onNodeWithTag("editFoodBuyDate").performTextClearance()
    composeTestRule
        .onNodeWithTag("editFoodBuyDate")
        .performTextInput(formatTimestampToDate(Timestamp.now()))
    composeTestRule.onNodeWithTag("editFoodItemScreen").performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performClick()
    composeTestRule.onNodeWithTag("IndividualTestScreenGoBack").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
  }

  // In this test the User wants to add a new recipe as well as searching for the recipe of Paella
  @Test
  fun testEndToEndAddNewRecipe() {
    // Start in the overview screen
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    // Navigate to the recipe screen
    composeTestRule.onNodeWithTag("Recipes").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Recipes").performClick()
    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed().performClick()
    // Fill in the recipe details
    composeTestRule.onNodeWithTag("addRecipeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRecipeTitle").performTextInput("Ham and Cheese")
    composeTestRule.onNodeWithTag("inputRecipeServings").performTextInput("4")
    composeTestRule.onNodeWithTag("inputRecipeTime").performTextInput("30")
    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("inputIngredientName").performTextInput("Ham")
    composeTestRule.onNodeWithTag("inputIngredientQuantity").performTextInput("6")
    composeTestRule.onNodeWithTag("addIngredientButton2").performClick()
    composeTestRule.onNodeWithTag("addInstructionButton").performClick()
    composeTestRule
        .onNodeWithTag("inputRecipeInstruction")
        .performTextInput("Add the salmon into the smoker")
    composeTestRule.onNodeWithTag("addButton").performClick()
    // Searches for a recipe
    composeTestRule.onNodeWithTag("recipesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("searchBar")))
        .performTextInput("Paella")
    composeTestRule.onAllNodesWithTag("recipesCards").assertCountEquals(1)
    composeTestRule
        .onNode(hasText("Paella") and hasAnyAncestor(hasTestTag("recipeSearchBar")))
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
