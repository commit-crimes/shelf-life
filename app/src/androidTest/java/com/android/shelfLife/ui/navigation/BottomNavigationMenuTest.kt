package com.android.shelfLife.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.profile.ProfileScreen
import com.android.shelfLife.viewmodel.profile.ProfileScreenViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.UserRepositoryTestHelper
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class BottomNavigationMenuTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var invitationRepository: InvitationRepository

  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper

  private lateinit var navigationActions: NavigationActions
  private lateinit var instrumentationContext: android.content.Context

  @Before
  fun setUp() {
    hiltRule.inject()

    navigationActions = mock()
    instrumentationContext = InstrumentationRegistry.getInstrumentation().context

    // Mock userRepository to return userInvitationsFlow
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
    // By default, no invitations are returned
    runBlocking {
      whenever(invitationRepository.getInvitationsBatch(any())).thenReturn(emptyList())
    }
  }

  /**
   * Creates the ProfileScreenViewModel AFTER setting up the flows and mocks for the test scenario,
   * following the same pattern as the InvitationScreenTest.
   */
  private fun createViewModel(): ProfileScreenViewModel {
    return ProfileScreenViewModel(userRepository)
  }

  @Test
  fun testBottomNavigationMenuFromProfileToOverview() = runBlocking {
    // For this test, we might simulate that user has no invitations or some scenario.
    // Set the flows and mocks as needed before creating the ViewModel.
    userRepositoryTestHelper.setInvitations(emptyList())

    // Create the ViewModel after data is set
    val profileViewModel = createViewModel()

    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions,
          context = instrumentationContext,
          profileViewModel = profileViewModel)
    }
    composeTestRule.waitForIdle()

    composeTestRule.onRoot().printToLog("UI-TREE")

    // Perform click on the OVERVIEW tab
    composeTestRule
        .onNodeWithTag(TopLevelDestinations.OVERVIEW.textId)
        .assertIsDisplayed()
        .performClick()

    // Verify navigation action
    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
  }
}
