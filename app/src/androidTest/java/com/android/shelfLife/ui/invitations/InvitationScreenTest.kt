package com.android.shelfLife.ui.invitations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.invitations.InvitationViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.HouseholdRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import io.mockk.verify
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class InvitationScreenTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var invitationRepository: InvitationRepository
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var houseHoldRepository: HouseHoldRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    hiltRule.inject()
    navigationActions = mock()

    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)
    userRepositoryTestHelper = UserRepositoryTestHelper(userRepository)

    // Provide a real user
    val realUser =
        User(
            uid = "currentUserId",
            username = "Current User",
            email = "currentUser@example.com",
            photoUrl = null,
            householdUIDs = emptyList(),
            selectedHouseholdUID = null,
            recipeUIDs = emptyList())
    userRepositoryTestHelper.setUser(realUser)

    // By default, return empty list for any getInvitationsBatch() call
    runBlocking {
      whenever(invitationRepository.getInvitationsBatch(any())).thenReturn(emptyList())
    }
  }

  private fun createViewModel(): InvitationViewModel {
    // Create the ViewModel after we set the userInvitationsFlow and mocks
    return InvitationViewModel(invitationRepository, userRepository, houseHoldRepository)
  }

  @Test
  fun noInvitationsDisplaysNoPendingMessage(): Unit = runBlocking {
    // No invitations scenario
    userRepositoryTestHelper.setInvitations(emptyList())
    whenever(invitationRepository.getInvitationsBatch(emptyList())).thenReturn(emptyList())

    // Create ViewModel after setting flows and mocks
    val invitationViewModel = createViewModel()

    composeTestRule.setContent {
      InvitationScreen(navigationActions = navigationActions, viewModel = invitationViewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onRoot().printToLog("UI-TREE")
    // Verify "No pending invitations" message is displayed
    composeTestRule.onNodeWithText("No pending invitations").assertIsDisplayed()
  }

  @Test
  fun singleInvitationIsDisplayed(): Unit = runBlocking {
    val invitationId = "invitation1"
    val invitation =
        Invitation(
            invitationId = invitationId,
            invitedUserId = "user123",
            householdId = "house123",
            householdName = "Test House",
            inviterUserId = "inviter123",
            timestamp = Timestamp(Date()))

    // Before creating ViewModel, set userInvitationsFlow and mocks
    userRepositoryTestHelper.setInvitations(listOf(invitationId))
    whenever(invitationRepository.getInvitationsBatch(listOf(invitationId)))
        .thenReturn(listOf(invitation))

    val invitationViewModel = createViewModel()

    composeTestRule.setContent {
      InvitationScreen(navigationActions = navigationActions, viewModel = invitationViewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onRoot().printToLog("UI-TREE")
    composeTestRule.onNodeWithTag("invitationCard").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("You have been invited to join household: Test House")
        .assertIsDisplayed()
  }

  @Test
  fun acceptInvitationCallsRepositoriesAndNavigatesBack() = runBlocking {
    val invitationId = "invitation1"
    val invitation =
        Invitation(
            invitationId = invitationId,
            invitedUserId = "user123",
            householdId = "house123",
            householdName = "Test House",
            inviterUserId = "inviter123",
            timestamp = Timestamp(Date()))

    // Set up data before ViewModel creation
    userRepositoryTestHelper.setInvitations(listOf(invitationId))
    whenever(invitationRepository.getInvitationsBatch(listOf(invitationId)))
        .thenReturn(listOf(invitation))

    val invitationViewModel = createViewModel()

    composeTestRule.setContent {
      InvitationScreen(navigationActions = navigationActions, viewModel = invitationViewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onRoot().printToLog("UI-TREE")
    composeTestRule.onNodeWithText("Accept").assertIsDisplayed().performClick()

    verify { userRepository.deleteInvitationUID(invitationId) }
    verify(invitationRepository).acceptInvitation(invitation)
    verify { userRepository.addCurrentUserToHouseHold("house123", "user123") }
    verify(navigationActions).goBack()
  }

  @Test
  fun declineInvitationCallsRepositoriesAndNavigatesBack() = runBlocking {
    val invitationId = "invitation2"
    val invitation =
        Invitation(
            invitationId = invitationId,
            invitedUserId = "user456",
            householdId = "house456",
            householdName = "Another House",
            inviterUserId = "inviter456",
            timestamp = Timestamp(Date()))

    // Set up data before ViewModel creation
    userRepositoryTestHelper.setInvitations(listOf(invitationId))
    whenever(invitationRepository.getInvitationsBatch(listOf(invitationId)))
        .thenReturn(listOf(invitation))

    val invitationViewModel = createViewModel()

    composeTestRule.setContent {
      InvitationScreen(navigationActions = navigationActions, viewModel = invitationViewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onRoot().printToLog("UI-TREE")
    composeTestRule.onNodeWithText("Decline").assertIsDisplayed().performClick()

    verify { userRepository.deleteInvitationUID(invitationId) }
    verify(invitationRepository).declineInvitation(invitation)
    verify(navigationActions).goBack()
  }
}
