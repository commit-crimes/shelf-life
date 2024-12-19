package com.android.shelfLife.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import com.android.shelfLife.MainActivity
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.permission.PermissionRepository
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodFactsRepositoryTestHelper
import helpers.FoodItemRepositoryTestHelper
import helpers.HouseholdRepositoryTestHelper
import helpers.PermissionRepositoryTestHelper
import helpers.RecipeRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class EndToEndM3Test {
  @Inject lateinit var foodItemRepository: FoodItemRepository
  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var foodFactsRepository: FoodFactsRepository
  @Inject lateinit var permissionRepository: PermissionRepository
  @Inject lateinit var recipeRepository: RecipeRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper
  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper
  private lateinit var foodFactsRepositoryTestHelper: FoodFactsRepositoryTestHelper
  private lateinit var permissionRepositoryTestHelper: PermissionRepositoryTestHelper
  private lateinit var recipeRepositoryTestHelper: RecipeRepositoryTestHelper

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
    permissionRepositoryTestHelper = PermissionRepositoryTestHelper(permissionRepository)
    recipeRepositoryTestHelper = RecipeRepositoryTestHelper(recipeRepository)

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
            expiryDate = Timestamp(Date(2025 - 1900, 11, 29)), // Expires in 1 day
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

  /**
   * This test goes through the following flow:
   * 1. User logs in
   * 2. User scans a barcode
   * 3. User inputs details for the food item
   * 4. User checks the overview screen
   * 5. User logs out
   */
  @Test
  fun testEndToEnd_add_food_item_with_scanner() {
    // User starts at the login screen
    composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("loginButton").performClick()
    // User is now on the overview Screen
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    // User navigates to the scanner screen
    composeTestRule.onNodeWithTag("Scanner").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("barcodeScannerScreen").assertIsDisplayed()
    // User scans the barcode
    foodFactsRepositoryTestHelper.setFoodFactsSuggestions(listOf(foodFacts))
    foodFactsRepositoryTestHelper.setSearchStatus(SearchStatus.Success)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("bottomSheetScaffold").assertIsDisplayed()

    // Swipe up to reveal the bottom sheet
    composeTestRule.onNodeWithTag("bottomSheetScaffold").performTouchInput {
      swipe(
          start = Offset(centerX, bottom - 10f), // Start from near the bottom of the sheet
          end = Offset(centerX, top + 10f), // Drag to near the top
          durationMillis = 500 // Duration for the swipe
          )
    }

    // Input details for the food item
    composeTestRule.onNodeWithTag("locationDropdown", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("dropDownItem_Pantry", useUnmergedTree = true).performClick()
    composeTestRule
        .onNodeWithTag("dropdownMenu_Select location", useUnmergedTree = true)
        .assertTextContains("Pantry")
    composeTestRule
        .onNodeWithTag("expireDateTextField", useUnmergedTree = true)
        .performTextInput("29122025")
    composeTestRule.onNodeWithTag("submitButton").performClick()
    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))
    composeTestRule.waitForIdle()

    // User navigates back to the overview screen
    composeTestRule.onNodeWithTag("Overview").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    // User navigates to the profile screen
    composeTestRule.onNodeWithTag("Profile").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profileScaffold").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed().performClick()

    // User is now on the login screen
    composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
  }

  /**
   * This test goes through the following flow:
   * 1. User logs in
   * 2. User adds a new household from the first time welcome screen
   * 3. User adds a second household
   * 4. User edits the first households name
   * 5. User deletes the second household
   * 6. User logs out
   */
  @Test
  fun testEndToEnd_household_management() {
    // User logs in and navigates to the profile screen
    householdRepositoryTestHelper.selectHousehold(null)
    composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").performClick()
    // User arrives at the first time welcome screen
    composeTestRule.onNodeWithTag("firstTimeWelcomeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("householdNameSaveButton").performClick()

    // User arrives at household creation screen
    composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New test Household")
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()
    householdRepositoryTestHelper.selectHousehold(
        HouseHold("1", "New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()))

    // User arrives at the overview screen
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    // User opens the hamburger menu and adds a new household
    composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addHouseholdIcon").assertIsDisplayed().performClick()

    // User adds a new household
    composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New test Household")
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()

    // Correct household name
    composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextClearance()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New test Household 2")
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()
    householdRepositoryTestHelper.selectHousehold(
        HouseHold("2", "New test Household 2", emptyList(), emptyList(), emptyMap(), emptyMap()))
    householdRepositoryTestHelper.setHouseholds(
        listOf(
            HouseHold("1", "New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()),
            HouseHold(
                "2", "New test Household 2", emptyList(), emptyList(), emptyMap(), emptyMap())))

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("householdElement_0").assertTextEquals("New test Household")
    composeTestRule.onNodeWithTag("householdElement_1").assertTextEquals("New test Household 2")

    // User edits the first household
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag("householdElement_0")
        .onChildren()
        .filter(hasTestTag("editHouseholdIndicatorIcon"))
        .onFirst()
        .assertIsDisplayed()
        .performClick()
    householdRepositoryTestHelper.setHouseholdToEdit(
        HouseHold("1", "New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()))

    // User edits the household name
    composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").assert(hasText("New test Household"))
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextClearance()
    composeTestRule
        .onNodeWithTag("HouseHoldNameTextField")
        .performTextInput("New New test Household")
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()
    householdRepositoryTestHelper.selectHousehold(
        HouseHold("1", "New New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()))
    householdRepositoryTestHelper.setHouseholds(
        listOf(
            HouseHold(
                "1", "New New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()),
            HouseHold(
                "2", "New test Household 2", emptyList(), emptyList(), emptyMap(), emptyMap())))

    // User deletes the second household
    composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("householdElement_1").assertTextEquals("New test Household 2")
    composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()
    composeTestRule
        .onNodeWithTag("householdElement_1")
        .onChildren()
        .filter(hasTestTag("deleteHouseholdIcon"))
        .onFirst()
        .performClick()
    composeTestRule.onNodeWithTag("DeleteConfirmationDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").performClick()
    householdRepositoryTestHelper.selectHousehold(
        HouseHold("1", "New New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()))
    householdRepositoryTestHelper.setHouseholds(
        listOf(
            HouseHold(
                "1", "New New test Household", emptyList(), emptyList(), emptyMap(), emptyMap())))
    composeTestRule.onNodeWithTag("householdElement_1").assertIsNotDisplayed()

    // Close drawer
    Espresso.pressBack()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    // User logs out
    composeTestRule.onNodeWithTag("Profile").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profileScaffold").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
  }

  @Test
  fun testEndToEnd_add_food_manually_then_create_recipe() {

    // User logs in and navigates to the profile screen
    householdRepositoryTestHelper.selectHousehold(houseHold)
    composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("loginButton").performClick()

    // User arrives at the overview screen
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    // User navigates to the add food item screen
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("addFoodFab").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("addFoodItemScreen").assertIsDisplayed()

    composeTestRule.waitForIdle()
    // User adds a food item manually
    // Scroll to and interact with the input fields
    val scrollableNode = composeTestRule.onNodeWithTag("addFoodItemScreen")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodName"))
    composeTestRule.onNodeWithTag("inputFoodName").performTextInput("Apple")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodAmount"))
    composeTestRule.onNodeWithTag("inputFoodAmount").performTextInput("5")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodExpireDate"))
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodExpireDate").performTextInput("29122025")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodOpenDate"))
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodOpenDate").performTextInput("01122025")

    scrollableNode.performScrollToNode(hasTestTag("inputFoodBuyDate"))
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputFoodBuyDate").performTextInput("30112025")

    // Scroll to and click the submit button
    scrollableNode.performScrollToNode(hasTestTag("foodSave"))
    composeTestRule.onNodeWithTag("foodSave").performClick()
    foodItemRepositoryTestHelper.setFoodItems(listOf(foodItem))
    composeTestRule.waitForIdle()

    // User navigates back to the overview screen
    composeTestRule.onNodeWithTag("Overview").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    // User navigates to the recipe
    composeTestRule.onNodeWithTag("Recipes").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("recipesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("addRecipeFab").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("addRecipeScreen").assertIsDisplayed()

    // User adds a recipe

    composeTestRule.onNodeWithTag("inputRecipeTitle").performTextInput("Apple Pie")
    composeTestRule.onNodeWithTag("inputRecipeServings").performTextInput("5")
    composeTestRule.onNodeWithTag("inputRecipeTime").performTextInput("45")
    composeTestRule.onNodeWithTag("addIngredientButton").performClick()
    composeTestRule.onNodeWithTag("addIngredientPopUp").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputIngredientName").performTextInput("Apple")
    composeTestRule.onNodeWithTag("inputIngredientQuantity").performTextInput("500")
    composeTestRule.onNodeWithTag("addIngredientButton2").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("ingredientItem").assertIsDisplayed()

    // User adds instructions
    composeTestRule.onNodeWithTag("addInstructionButton").performClick()
    composeTestRule.onNodeWithTag("inputRecipeInstruction").performTextInput("Bake the apple")

    // User adds the recipe
    recipeRepositoryTestHelper.setRecipes(
        listOf(Recipe("1", "Apple Pie", emptyList(), 5.0f, Duration.parse("PT45M"), emptyList())))
    composeTestRule.onNodeWithTag("addButton").assertIsDisplayed().performClick()

    composeTestRule.waitForIdle()

    // User clicks on the recipe
    composeTestRule.onNodeWithTag("recipesCards").assertIsDisplayed().performClick()
    recipeRepositoryTestHelper.setSelectedRecipe(
        Recipe(
            "1",
            "Apple Pie",
            listOf("Bake the apple"),
            5.0f,
            Duration.parse("PT45M"),
            listOf(Ingredient("Apple", Quantity(500.0, FoodUnit.GRAM)))))
    composeTestRule.onNodeWithTag("individualRecipesScreen").assertIsDisplayed()

    // User starts the recipe
    composeTestRule.onNodeWithTag("startButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("servingsScreen").assertIsDisplayed()

    // User selects the number of servings
    composeTestRule.onNodeWithTag("increaseButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("increaseButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("increaseButton").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("servingsText").assertIsDisplayed().assertTextEquals("8.0")
    composeTestRule.onNodeWithTag("decreaseButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("servingsText").assertIsDisplayed().assertTextEquals("7.0")

    // User continues the recipe
    composeTestRule.onNodeWithTag("nextFab").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("selectFoodItemsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodItemCard").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("amountSlider").performSemanticsAction(
        SemanticsActions.SetProgress) { progress ->
          progress.invoke(5f)
        }
    composeTestRule.onNodeWithTag("doneButton").assertIsDisplayed().performClick()
    // User arrives at the recipe execution screen
    composeTestRule.onNodeWithTag("instructionScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("instructionText").assert(hasText("Bake the apple"))
    // User finishes the recipe
    composeTestRule.onNodeWithTag("finishButton").assertIsDisplayed().performClick()
    foodItemRepositoryTestHelper.setFoodItems(emptyList())

    // User navigates back to the overview screen
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("foodItemCard").assertIsNotDisplayed()
  }
}
