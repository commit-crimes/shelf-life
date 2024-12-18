package com.android.shelfLife.ui

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
        .performTextInput("29122024")
    composeTestRule.onNodeWithTag("submitButton").performClick()
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
    composeTestRule.onNodeWithTag("goBackArrow").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
  }
}
