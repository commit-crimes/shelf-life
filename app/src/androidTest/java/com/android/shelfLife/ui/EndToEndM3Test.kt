package com.android.shelfLife.ui

import android.net.Uri
import androidx.compose.ui.geometry.Offset
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
import helpers.PermissionRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import io.mockk.every
import io.mockk.mockk
import java.util.*
import javax.inject.Inject
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

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper
  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper
  private lateinit var foodFactsRepositoryTestHelper: FoodFactsRepositoryTestHelper
  private lateinit var permissionRepositoryTestHelper: PermissionRepositoryTestHelper

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

    @Test
    fun testEndToEnd_household_management() {
        //User logs in and navigates to the profile screen
        householdRepositoryTestHelper.selectHousehold(null)
        composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loginButton").performClick()
        //User arrives at the first time welcome screen
        composeTestRule.onNodeWithTag("firstTimeWelcomeScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("householdNameSaveButton").performClick()

        //User arrives at household creation screen
        composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New test Household")
        composeTestRule.onNodeWithTag("ConfirmButton").performClick()
        householdRepositoryTestHelper.selectHousehold(HouseHold("1", "New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()))

        //User arrives at the overview screen
        composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

        //User opens the hamburger menu and adds a new household
        composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("addHouseholdIcon").assertIsDisplayed().performClick()

        //User adds a new household
        composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New test Household")
        composeTestRule.onNodeWithTag("ConfirmButton").performClick()

        //Correct household name
        composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextClearance()
        composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New test Household 2")
        composeTestRule.onNodeWithTag("ConfirmButton").performClick()
        householdRepositoryTestHelper.selectHousehold(HouseHold("2", "New test Household 2", emptyList(), emptyList(), emptyMap(), emptyMap()))
        householdRepositoryTestHelper.setHouseholds(listOf(HouseHold("1", "New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()), HouseHold("2", "New test Household 2", emptyList(), emptyList(), emptyMap(), emptyMap())))

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("householdElement_0").assertTextEquals("New test Household")
        composeTestRule.onNodeWithTag("householdElement_1").assertTextEquals("New test Household 2")

        //User edits the first household
        composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()
        composeTestRule.waitForIdle()
composeTestRule.onNodeWithTag("householdElement_0").onChildren().filter(hasTestTag("editHouseholdIndicatorIcon")).onFirst()
    .assertIsDisplayed()
    .performClick()
        householdRepositoryTestHelper.setHouseholdToEdit(HouseHold("1", "New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()))

        //User edits the household name
        composeTestRule.onNodeWithTag("HouseHoldCreationScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("HouseHoldNameTextField").assert(hasText("New test Household"))
        composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextClearance()
        composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New New test Household")
        composeTestRule.onNodeWithTag("ConfirmButton").performClick()
        householdRepositoryTestHelper.selectHousehold(HouseHold("1", "New New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()))
        householdRepositoryTestHelper.setHouseholds(listOf(HouseHold("1", "New New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()), HouseHold("2", "New test Household 2", emptyList(), emptyList(), emptyMap(), emptyMap())))

        //User deletes the second household
        composeTestRule.onNodeWithTag("hamburgerIcon").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("householdSelectionDrawer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("householdElement_1").assertTextEquals("New test Household 2")
        composeTestRule.onNodeWithTag("editHouseholdIcon").performClick()
        composeTestRule.onNodeWithTag("householdElement_1").onChildren().filter(hasTestTag("deleteHouseholdIcon")).onFirst().performClick()
        composeTestRule.onNodeWithTag("DeleteConfirmationDialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").performClick()
        householdRepositoryTestHelper.selectHousehold(HouseHold("1", "New New test Household", emptyList(), emptyList(), emptyMap(), emptyMap()))
        householdRepositoryTestHelper.setHouseholds(listOf(HouseHold("1", "New New test Household", emptyList(), emptyList(), emptyMap(), emptyMap())))
        composeTestRule.onNodeWithTag("householdElement_1").assertIsNotDisplayed()

        Espresso.pressBack()
        composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

        //User logs out
        composeTestRule.onNodeWithTag("Profile").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profileScaffold").assertIsDisplayed()
        composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()
    }


}
