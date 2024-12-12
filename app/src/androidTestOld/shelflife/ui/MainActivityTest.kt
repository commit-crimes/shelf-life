package com.android.shelflife

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.TopLevelDestination
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var navController: NavHostController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    composeTestRule.setContent {
      navController = rememberNavController()
      navigationActions = NavigationActions(navController)

      NavHost(navController = navController, startDestination = Route.AUTH) {
        composable(Route.AUTH) { PlaceholderScreen("Auth") }
        composable(Route.OVERVIEW) { PlaceholderScreen("Overview") }
        composable(Route.PROFILE) { PlaceholderScreen("Profile") }
        composable(Route.SCANNER) { PlaceholderScreen("Scanner") }
        composable(Route.RECIPES) { PlaceholderScreen("Recipes") }
      }
    }
  }

  @Test
  fun testNavigateToOverview() {
    composeTestRule.runOnUiThread {
      navigationActions.navigateTo(
          TopLevelDestination(route = Route.OVERVIEW, icon = mockk(), textId = "Overview"))
    }

    composeTestRule.onNodeWithTag("Overview").assertIsDisplayed()
  }

  @Test
  fun testNavigateToProfile() {
    composeTestRule.runOnUiThread { navigationActions.navigateTo(Route.PROFILE) }

    composeTestRule.onNodeWithTag("Profile").assertIsDisplayed()
  }

  @Test
  fun testNavigateToAndClearBackStack() {
    composeTestRule.runOnUiThread { navigationActions.navigateToAndClearBackStack(Route.SCANNER) }

    composeTestRule.onNodeWithTag("Scanner").assertIsDisplayed()
  }

  @Test
  fun testCurrentRouteReturnsCorrectRoute() {
    composeTestRule.runOnUiThread { navigationActions.navigateTo(Route.RECIPES) }

    composeTestRule.waitForIdle()
    assert(navigationActions.currentRoute() == Route.RECIPES)
  }

  @Test
  fun testGoBack() {
    composeTestRule.runOnUiThread {
      navigationActions.navigateTo(Route.RECIPES)
      navigationActions.goBack()
    }

    composeTestRule.waitForIdle()
    // After going back, it should return to the previous route (e.g., AUTH in this case)
    assert(navigationActions.currentRoute() == Route.AUTH)
  }

  @Composable
  fun PlaceholderScreen(tag: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize().testTag(tag))
  }
}
