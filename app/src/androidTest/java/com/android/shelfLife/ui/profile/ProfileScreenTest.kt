package com.android.shelfLife.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.profile.ProfileScreenViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import javax.inject.Inject


@HiltAndroidTest
class ProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var mockViewModel: ProfileScreenViewModel
    @Inject
    lateinit var userRepository: UserRepository
    private lateinit var navigationActions: NavigationActions

    @Before
    fun setUp() {
        hiltRule.inject()

        navigationActions = mock(NavigationActions::class.java)

        whenever (userRepository.user).thenReturn(
            MutableStateFlow(
                User(
                    "currentUserId",
                    "Current User",
                    "currentuser@gmail.com",
                    null,
                    "",
                    emptyList(),
                    emptyList()))
        )
        whenever (userRepository.invitations).thenReturn(MutableStateFlow(emptyList()))
        mockViewModel = ProfileScreenViewModel(userRepository)
    }

    @Test
    fun testProfileNameDisplaysCorrectly() {
        composeTestRule.setContent {
            ProfileScreen(
                navigationActions = navigationActions,
                context = composeTestRule.activity.applicationContext,
                profileViewModel = mockViewModel
            )
        }

        // Verify that the name is displayed
        composeTestRule.onNodeWithTag("profileNameText")
            .assertIsDisplayed()
            .assertTextEquals("Current User")
    }

    @Test
    fun testLogoutButtonNavigatesToAuth() {
        composeTestRule.setContent {
            ProfileScreen(
                navigationActions = navigationActions,
                context = composeTestRule.activity.applicationContext,
                profileViewModel = mockViewModel
            )
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
                profileViewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    }
}