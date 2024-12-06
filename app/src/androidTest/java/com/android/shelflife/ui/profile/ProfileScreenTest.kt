// ProfileScreenTest.kt
package com.android.shelflife.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.android.shelfLife.model.invitations.InvitationViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.profile.ProfileScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertEquals
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var invitationViewModel: InvitationViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxUnitFun = true)

    // Initialize InvitationViewModel with a mock repository
    val mockRepository: InvitationRepositoryFirestore = mockk(relaxed = true)
    invitationViewModel = InvitationViewModel(mockRepository)

    // Mock the static method GoogleSignIn.getLastSignedInAccount
    mockkStatic(GoogleSignIn::class)

    // Create a mock GoogleSignInAccount
    val mockAccount = mockk<GoogleSignInAccount>()
    every { mockAccount.displayName } returns "John Smith"
    every { mockAccount.email } returns "test@example.com"
    every { mockAccount.photoUrl } returns null // Set this as needed

    // Mock the static method to return the mock account
    every { GoogleSignIn.getLastSignedInAccount(any()) } returns mockAccount
  }

  @After
  fun tearDown() {
    // Unmock the static method to avoid side effects on other tests
    unmockkStatic(GoogleSignIn::class)
  }

  @Test
  fun testNameText_whenAccountIsNull_displaysGuest() {
    // Mock getLastSignedInAccount to return null
    every { GoogleSignIn.getLastSignedInAccount(any()) } returns null

    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule.onNodeWithTag("profileNameText").assertIsDisplayed().assertTextEquals("Guest")
  }

  @Test
  fun profileScreen_displaysAccountName() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))

    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule
        .onNodeWithTag("profileNameText")
        .assertIsDisplayed()
        .assertTextEquals("John Smith")
  }

  @Test
  fun testGreetingText_whenAccountIsNotNull_displaysEmail() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))

    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule
        .onNodeWithTag("profileEmailText")
        .assertIsDisplayed()
        .assertTextEquals("test@example.com")
  }

  @Test
  fun profileScreen_hidesEmailWhenNotAvailable() {
    // Mock getLastSignedInAccount to return a mock account with null email
    val mockAccount = mockk<GoogleSignInAccount>()
    every { mockAccount.displayName } returns "John Smith"
    every { mockAccount.email } returns null
    every { mockAccount.photoUrl } returns null
    every { GoogleSignIn.getLastSignedInAccount(any()) } returns mockAccount

    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule.onNodeWithTag("profileEmailText").assertDoesNotExist()
  }

  @Test
  fun testLogoutButtonFunctionality() {
    val navigationActions =
        mockk<NavigationActions>(
            relaxed = true, block = { every { currentRoute() } returns Route.AUTH })

    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed().performClick()

    // Check that navigation navigated to AUTH
    assertEquals(Route.AUTH, navigationActions.currentRoute())
  }

  @Test
  fun testProfilePictureIsDisplayed() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
  }

  @Test
  fun testBottomNavigationIsDisplayed() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule.onNodeWithText("Profile").assertIsDisplayed().assertIsSelected()
  }

  @Test
  fun testBottomNavigationChangesSelectedRoute() {
    val navigationActions =
        mockk<NavigationActions>(
            relaxed = true, block = { every { currentRoute() } returns Route.RECIPES })
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule.onNodeWithText("Recipes").performClick()

    assertEquals(Route.RECIPES, navigationActions.currentRoute())
  }

  @Test
  fun displaysCorrectThemeLabel() {
    val navigationActions =
        mockk<NavigationActions>(
            relaxed = true, block = { every { currentRoute() } returns Route.PROFILE })

    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, invitationViewModel = invitationViewModel)
    }

    composeTestRule.onNodeWithTag("dropdownMenu_App Theme").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Light Mode").assertTextEquals("Light Mode")
    composeTestRule.onNodeWithTag("dropDownItem_Light Mode").performClick()
  }
}
