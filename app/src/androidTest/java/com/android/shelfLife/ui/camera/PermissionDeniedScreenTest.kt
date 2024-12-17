package com.android.shelfLife.ui.camera

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PermissionDeniedScreenTest {

  @get:Rule val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
  }

  @Test
  fun testPermissionDeniedMessageIsDisplayed() {
    val navController = mockk<NavHostController>(relaxed = true)
    val navigationActions = NavigationActions(navController)

    composeTestRule.setContent { PermissionDeniedScreen(navigationActions = navigationActions) }

    composeTestRule.onNodeWithTag("permissionDeniedMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("openSettingsButton").assertIsDisplayed()
  }

  @Test
  fun testBottomNavigationIsDisplayed() {
    val navController = mockk<NavHostController>(relaxed = true)
    val navigationActions = NavigationActions(navController)

    composeTestRule.setContent { PermissionDeniedScreen(navigationActions = navigationActions) }

    // Verify the bottom navigation is displayed and the SCANNER tab is selected
    composeTestRule.onNodeWithText("Scanner").assertIsDisplayed().assertIsSelected()
  }

  @Test
  fun testClickOnOtherTabNavigatesToCorrectScreen() {
    val navigationActions =
        mockk<NavigationActions>(
            relaxed = true, block = { every { currentRoute() } returns Route.RECIPES })

    composeTestRule.setContent { PermissionDeniedScreen(navigationActions = navigationActions) }

    // Click on another tab in the bottom navigation menu
    composeTestRule.onNodeWithText("Recipes").performClick()

    // Verify that navigateTo() was called with the correct route
    assertEquals(Route.RECIPES, navigationActions.currentRoute())
  }
}
