package com.android.shelfLife.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.profile.ProfileScreenViewModel
import com.example.compose.LocalThemeTogglerProvider
import com.example.compose.ThemeMode
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertTrue
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@HiltAndroidTest
class ProfileScreenTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var viewModel: ProfileScreenViewModel
  @Inject lateinit var userRepository: UserRepository
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    hiltRule.inject()

    navigationActions = mock(NavigationActions::class.java)

    whenever(userRepository.user)
        .thenReturn(
            MutableStateFlow(
                User(
                    "currentUserId",
                    "Current User",
                    "currentuser@gmail.com",
                    "",
                    "",
                    emptyList(),
                    emptyList())))
    whenever(userRepository.invitations).thenReturn(MutableStateFlow(emptyList()))
    viewModel = ProfileScreenViewModel(userRepository)
  }

  @Test
  fun testProfileNameDisplaysCorrectly() {
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions,
          context = composeTestRule.activity.applicationContext,
          profileViewModel = viewModel)
    }

    // Verify that the name is displayed
    composeTestRule
        .onNodeWithTag("profileNameText")
        .assertIsDisplayed()
        .assertTextEquals("Current User")
  }

  @Test
  fun testLogoutButtonNavigatesToAuth() {
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions,
          context = composeTestRule.activity.applicationContext,
          profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("logoutButton").performClick()

    verify(navigationActions).navigateToAndClearBackStack(Screen.AUTH)
  }

  @Test
  fun testProfilePictureIsDisplayed() {
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions,
          context = composeTestRule.activity.applicationContext,
          profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
  }

  @Test
  fun testEasterEgg() {
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions,
          context = composeTestRule.activity.applicationContext,
          profileViewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag("profilePicture")
        .assertIsDisplayed()
        .performClick()
        .performClick()
        .performClick()
        .performClick()
        .performClick()

    verify(navigationActions).navigateTo(Screen.EASTER_EGG)
  }

  @Test
  fun testLessThanFiveClicksDoesNotTriggerEasterEgg() {
    navigationActions = mockk(relaxed = true)
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions,
          context = composeTestRule.activity.applicationContext,
          profileViewModel = viewModel)
    }

    // Simulate 4 clicks on the image
    repeat(4) { composeTestRule.onNodeWithTag("profilePicture").performClick() }
    // Verify that navigation to Screen.EASTER_EGG was NOT triggered
    verify(exactly = 0) { navigationActions.navigateTo(Screen.EASTER_EGG) }
  }

    @Test
    fun themeTogglingTest(){
        composeTestRule.setContent {
            ProfileScreen(
                navigationActions = navigationActions,
                context = composeTestRule.activity.applicationContext,
                profileViewModel = viewModel)
        }
        composeTestRule.onNodeWithTag("themeToggler").performClick()
        assertTrue(viewModel.changeThemeMenuState.value)
        composeTestRule.onNodeWithText("Dark Mode").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Light Mode").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark Mode").performClick()
        verify(navigationActions).navigateToAndClearBackStack(Route.PROFILE)
    }

    @Test
    fun invitationsPopUp(){
        whenever(userRepository.user)
            .thenReturn(
                MutableStateFlow(
                    User(
                        "currentUserId",
                        "Current User",
                        "currentuser@gmail.com",
                        "",
                        "",
                        listOf("i1"),
                        emptyList())))
        composeTestRule.setContent {
            ProfileScreen(
                navigationActions = navigationActions,
                context = composeTestRule.activity.applicationContext,
                profileViewModel = viewModel)
        }

    }
}
