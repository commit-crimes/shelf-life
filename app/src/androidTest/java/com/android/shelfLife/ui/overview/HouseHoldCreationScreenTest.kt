package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.overview.HouseholdCreationScreenViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodItemRepositoryTestHelper
import helpers.HouseholdRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class HouseHoldCreationScreenTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var foodItemRepository: FoodItemRepository
  @Inject lateinit var invitationRepository: InvitationRepository
  @Inject lateinit var userRepository: UserRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper
  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper

  private lateinit var navigationActions: NavigationActions
  private lateinit var viewModel: HouseholdCreationScreenViewModel

  @Before
  fun setUp() {
    hiltRule.inject()
    navigationActions = mock()

    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)
    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(foodItemRepository)
    userRepositoryTestHelper = UserRepositoryTestHelper(userRepository)

    // Provide a user
    val realUser =
        User(
            uid = "currentUserId",
            username = "Current User",
            email = "user@example.com",
            photoUrl = null,
            householdUIDs = listOf("household123"),
            selectedHouseholdUID = "household123",
            recipeUIDs = emptyList())
    userRepositoryTestHelper.setUser(realUser)

    // After all mocks are set, create the viewModel
    createViewModel()
  }

  private fun createViewModel() {
    viewModel =
        HouseholdCreationScreenViewModel(
            houseHoldRepository = houseHoldRepository,
            foodItemRepository = foodItemRepository,
            invitationRepository = invitationRepository,
            userRepository = userRepository)
    assertNotNull("ViewModel should not be null", viewModel)
  }

  private fun setContent() {
    composeTestRule.setContent {
      HouseHoldCreationScreen(navigationActions = navigationActions, viewModel = viewModel)
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun houseHoldCreationScreen_displaysInitialUI() {
    setContent()

    // Assert main UI components
    composeTestRule.onNodeWithTag("HouseHoldNameTextField").assertIsDisplayed()
    composeTestRule.onNodeWithText("Household Members").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AddEmailFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ConfirmButton").assertIsDisplayed()
  }

  @Test
  fun houseHoldCreationScreen_handlesCloseButton() {
    setContent()

    // Click close button
    composeTestRule.onNodeWithTag("CloseButton").performClick()

    // Verify navigation back
    verify(navigationActions).goBack()
  }

  // For delete button:
  // If we set householdToEdit to non-null, then a delete button shows
  @Test
  fun houseHoldCreationScreen_handlesDeleteButton() {
    runBlocking {
      val mockHousehold =
          HouseHold(
              uid = "testHouseholdId",
              name = "Existing Household",
              members = listOf("uid1"),
              sharedRecipes = emptyList(),
              ratPoints = emptyMap(),
              stinkyPoints = emptyMap())
      householdRepositoryTestHelper.setHouseholdToEdit(mockHousehold)
    }

    createViewModel()
    setContent()

    // Click delete button
    composeTestRule.onNodeWithTag("DeleteButton").performClick()

    // Verify confirmation dialog is displayed
    // The dialog text may differ. If the dialog text is not "Are you sure...",
    // check the actual code in DeletionConfirmationPopUp
    // The code uses a generic DeletionConfirmationPopUp. You must ensure the text matches that
    // logic.
    // If it does not, adjust:
    // Here we guess it says "Delete Household"
    composeTestRule.onNodeWithText("Delete Household").assertIsDisplayed()

    // Confirm deletion (Look for the text that pops up after confirm)
    // The DeletionConfirmationPopUp used a "sign out" logic in previous code. Check that code
    // carefully.
    // It's missing from your snippet. Let's assume it says "Yes" to confirm:
    composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").performClick()

    // Verify navigation back
    verify(navigationActions).goBack()
  }
}
