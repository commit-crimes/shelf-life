package com.android.shelfLife.ui.profile

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.profile.ProfileScreenViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.*
import org.mockito.kotlin.verify


@HiltAndroidTest
class ProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var mockViewModel: ProfileScreenViewModel
    private lateinit var navigationActions: NavigationActions

    @Before
    fun setUp() {
        hiltRule.inject()

        // Mock the NavigationActions
        navigationActions = mockk(relaxed = true)

        // Create real StateFlow objects
        val userFlow = MutableStateFlow(
            User(
                uid = "currentUserId",
                username = "Current User",
                email = "currentUser@example.com",
                photoUrl = null,
                householdUIDs = emptyList(),
                selectedHouseholdUID = null,
                recipeUIDs = emptyList()
            )
        )
        val invitationsFlow = MutableStateFlow(emptyList<String>())
        val themeMenuStateFlow = mutableStateOf(false)

        // Mock the ViewModel with real StateFlows
        mockViewModel = mockk(relaxed = true) {
            every { currentUser } returns userFlow.asStateFlow()
            every { invitationUIDS } returns invitationsFlow.asStateFlow()
            every { changeThemeMenuState } returns themeMenuStateFlow
            every { signOut(any()) } returns Unit
        }
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

        // Click the logout button
        composeTestRule.onNodeWithTag("logoutButton").performClick()

        // Verify the navigation action
        verify { navigationActions.navigateToAndClearBackStack(Screen.AUTH) }
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