package com.android.shelfLife.ui

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.shelfLife.MainActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodFactsRepositoryTestHelper
import helpers.FoodItemRepositoryTestHelper
import helpers.HouseholdRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import io.mockk.every
import io.mockk.mockk
import java.util.*
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class EndToEndM2Test {
  @Inject lateinit var foodItemRepository: FoodItemRepository
  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var foodFactsRepository: FoodFactsRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper
  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper
  private lateinit var foodFactsRepositoryTestHelper: FoodFactsRepositoryTestHelper

  private lateinit var foodItem: FoodItem
  private lateinit var houseHold: HouseHold
  private lateinit var foodFacts: FoodFacts

  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()

    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)
    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(foodItemRepository)
    userRepositoryTestHelper = UserRepositoryTestHelper(userRepository)
    foodFactsRepositoryTestHelper = FoodFactsRepositoryTestHelper(foodFactsRepository)

    /*
    every { barcodeScannerViewModel.permissionGranted } returns true

       */

    // Create a FoodItem to be used in tests
    foodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    foodItem =
        FoodItem(
            uid = "foodItem1",
            foodFacts = foodFacts,
            expiryDate = Timestamp(Date(System.currentTimeMillis() + 86400000)), // Expires in 1 day
            owner = "John",
        )

    // Initialize the household with the food item
    houseHold =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            sharedRecipes = emptyList(),
            stinkyPoints = emptyMap(),
            ratPoints = emptyMap())

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

    val user =
        User(
            uid = "1",
            username = "John",
            email = "",
            householdUIDs = listOf("1"),
            selectedHouseholdUID = "1",
        )

    // Init the repositories with the test data
    householdRepositoryTestHelper.selectHousehold(houseHold)
    userRepositoryTestHelper.setUser(user)
  }

  // In this test an user tries to manually add a food item to their household and later not
  // satisfied with the manual approach tries rather to scan the item.
  @Test
  fun testEndToEnd_see_add_food_item() {
    // User starts at the login screen
    composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("loginButton").performClick()

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
    // Correct the expire date
    scrollableNode.performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("29122025")
    // Scroll to and click the submit button again
    scrollableNode.performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performClick()
    // Simulate the user submitting the form
    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    // Goes and Scans the item
    composeTestRule.onNodeWithTag("Scanner").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("barcodeScannerScreen").assertIsDisplayed()
    foodFactsRepositoryTestHelper.setFoodFactsSuggestions(listOf(foodFacts))
    composeTestRule.onNodeWithTag("locationDropdown").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    composeTestRule.onNodeWithTag("dropdownMenu_Select location").assertTextContains("Pantry")
    composeTestRule.onNodeWithTag("expireDateTextField").performTextInput("29122024")
    composeTestRule.onNodeWithTag("submitButton").performClick()
  }

  /*// In this test the user wants to first add a friend into a new household
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
  */
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
    composeTestRule.onNodeWithTag("goBackArrow").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
  }

  // In this test the User wants to add a new recipe as well as searching for the recipe of Paella
  // This end to end test has to be updated with the new workflow by @Ricardo
  /* @Test
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
  }*/
}
