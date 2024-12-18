package com.android.shelfLife.ui

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
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodItemRepositoryTestHelper
import helpers.HouseholdRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import java.util.*
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class EndToEndM1Test {
  @Inject lateinit var foodFactsRepository: FoodFactsRepository
  @Inject lateinit var recipeRepository: RecipeRepository
  @Inject lateinit var recipeGeneratorRepository: RecipeGeneratorRepository
  @Inject lateinit var foodItemRepository: FoodItemRepository
  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var invitationRepository: InvitationRepository
  @Inject lateinit var userRepository: UserRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper
  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper

  private lateinit var houseHold: HouseHold
  private lateinit var foodItem: FoodItem

  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()

    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)
    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(foodItemRepository)
    userRepositoryTestHelper = UserRepositoryTestHelper(userRepository)

    // Create a FoodItem to be used in tests
    val foodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "123456789",
            quantity = Quantity(5.0, FoodUnit.COUNT),
            category = FoodCategory.FRUIT)
    foodItem =
        FoodItem(
            uid = "mockedUID",
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
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap(),
        )

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

  @Test
  fun testEndToEndFlow() {
    // User starts at the login screen
    composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("loginButton").performClick()

    // composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    // User is now on the overview Screen
    // User wants to add a new food item
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFoodFab").assertHasClickAction()
    composeTestRule.onNodeWithTag("addFoodFab").performClick()

      composeTestRule.onNodeWithTag("addFoodFab").performClick()
    composeTestRule.onNodeWithTag("addFoodItemTitle").assertIsDisplayed()

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

    // Simulate the user submitting the form
    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))

    composeTestRule.onNodeWithTag("foodSave").performClick()

    composeTestRule.waitForIdle()
    // User is now on the overview screen
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    // User now wants to view the details of the food item
    composeTestRule.onNodeWithTag("foodItemCard").assertIsDisplayed()
    foodItemRepositoryTestHelper.setSelectedFoodItem(foodItem)
    composeTestRule.onNodeWithTag("foodItemCard").assertHasClickAction()
    composeTestRule.onNodeWithTag("foodItemCard").performClick()

    composeTestRule.onNodeWithTag("IndividualFoodItemScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("IndividualFoodItemName").assertTextContains("Apple")
  }
}
