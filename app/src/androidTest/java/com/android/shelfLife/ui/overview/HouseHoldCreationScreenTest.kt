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
import com.android.shelfLife.ui.navigation.Screen
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

    composeTestRule.onNodeWithTag("HouseHoldNameTextField").assertIsDisplayed()
    composeTestRule.onNodeWithText("Household Members").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AddEmailFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ConfirmButton").assertIsDisplayed()
  }

  @Test
  fun houseHoldCreationScreen_handlesCloseButton() {
    setContent()

    composeTestRule.onNodeWithTag("CloseButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun houseHoldCreationScreen_handlesAddEmail() {
    setContent()

    composeTestRule.onNodeWithTag("AddEmailFab").performClick()
    composeTestRule.onNodeWithTag("EmailInputField").performTextInput("friend@example.com")
    composeTestRule.onNodeWithTag("AddEmailButton").performClick()

    composeTestRule.onNodeWithText("friend@example.com").assertIsDisplayed()
  }

  @Test
  fun houseHoldCreationScreen_handlesRemoveEmail() {
    setContent()
    composeTestRule.onNodeWithTag("AddEmailFab").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("EmailInputField").performTextInput("friend@example.com")
    composeTestRule.onNodeWithTag("AddEmailButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithText("friend@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RemoveEmailButton").performClick()

    composeTestRule.onNodeWithText("friend@example.com").assertDoesNotExist()
  }

  @Test
  fun houseHoldCreationScreen_handlesInvalidHouseholdName() {
    setContent()

    composeTestRule.onNodeWithTag("ConfirmButton").performClick()
    composeTestRule.onNodeWithText("Household name already exists or is empty").assertIsDisplayed()
  }

  @Test
  fun houseHoldCreationScreen_handlesValidHouseholdName() {
    setContent()

    composeTestRule.onNodeWithTag("HouseHoldNameTextField").performTextInput("New Household")
    composeTestRule.onNodeWithTag("ConfirmButton").performClick()

    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }

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

    composeTestRule.onNodeWithTag("DeleteButton").performClick()
    composeTestRule.onNodeWithText("Delete Household").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmDeleteHouseholdButton").performClick()

    verify(navigationActions).goBack()
  }
}
