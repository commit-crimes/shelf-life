package com.android.shelflife.ui.profile

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.profile.ProfileScreen
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testNameText_whenAccountIsNull_displaysGuest() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    composeTestRule.setContent {
      ProfileScreen(navigationActions = navigationActions, account = null)
    }

    composeTestRule.onNodeWithTag("profileNameText").assertIsDisplayed().assertTextEquals("Guest")
  }

  @Test
  fun profileScreen_displaysAccountName() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    val account =
        mockk<GoogleSignInAccount>(
            block = {
              every { displayName } returns "John Smith"
              every { photoUrl } returns null
              every { email } returns null
            })

    composeTestRule.setContent {
      ProfileScreen(navigationActions = navigationActions, account = account)
    }

    composeTestRule.onNodeWithTag("profileNameText").assertTextEquals("John Smith")
  }

  @Test
  fun testGreetingText_whenAccountIsNotNull_displaysEmail() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))

    val account =
        mockk<GoogleSignInAccount>(
            block = {
              every { email } returns "test@example.com"
              every { displayName } returns "Jon Smith"
              every { photoUrl } returns
                  Uri.parse(
                      "https://letsenhance.io/static/8f5e523ee6b2479e26ecc91b9c25261e/1015f/MainAfter.jpg")
            })
    composeTestRule.setContent {
      ProfileScreen(navigationActions = navigationActions, account = account)
    }

    composeTestRule
        .onNodeWithTag("profileEmailText")
        .assertIsDisplayed()
        .assertTextEquals("test@example.com")
  }

  @Test
  fun profileScreen_hidesEmailWhenNotAvailable() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    composeTestRule.setContent {
      ProfileScreen(navigationActions = navigationActions, account = null)
    }

    composeTestRule.onNodeWithTag("profileEmailText").assertDoesNotExist()
  }

  @Test
  fun testLogoutButtonFunctionality() {
    val navigationActions =
        mockk<NavigationActions>(
            relaxed = true, block = { every { currentRoute() } returns Route.AUTH })
    var signOutCalled = false
    val signOutUser = { signOutCalled = true }

    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions, account = null, signOutUser = signOutUser)
    }

    composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed().performClick()

    // Check that signOutUser was called
    assertTrue(signOutCalled)

    // Check that navigation navigated to AUTH
    assertEquals(Route.AUTH, navigationActions.currentRoute())
  }

  @Test
  fun testProfilePictureIsDisplayed() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    composeTestRule.setContent {
      ProfileScreen(navigationActions = navigationActions, account = null)
    }

    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
  }

  @Test
  fun testBottomNavigationIsDisplayed() {
    val navigationActions = NavigationActions(mockk<NavHostController>(relaxed = true))
    composeTestRule.setContent {
      ProfileScreen(navigationActions = navigationActions, account = null)
    }

    composeTestRule.onNodeWithText("Profile").assertIsDisplayed().assertIsSelected()
  }

  @Test
  fun testBottomNavigationChangesSelectedRoute() {
    val navigationActions =
        mockk<NavigationActions>(
            relaxed = true, block = { every { currentRoute() } returns Route.RECIPES })
    composeTestRule.setContent {
      ProfileScreen(navigationActions = navigationActions, account = null)
    }

    composeTestRule.onNodeWithText("Recipes").performClick()

    assertEquals(Route.RECIPES, navigationActions.currentRoute())
  }
}
