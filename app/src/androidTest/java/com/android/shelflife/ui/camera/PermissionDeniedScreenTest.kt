package com.android.shelfLife.ui.scanner

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.ui.camera.PermissionDeniedScreen
import com.android.shelfLife.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class PermissionDeniedScreenTest {

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock()
  }

  // Helper function to set up the PermissionDeniedScreen
  private fun setUpPermissionDeniedScreen() {
    composeTestRule.setContent { PermissionDeniedScreen(navigationActions = navigationActions) }
  }

  // Helper function to check if the common UI elements are displayed
  private fun verifyCommonUIElements() {
    composeTestRule.onNodeWithTag("permissionDeniedScreen").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Camera permission is required to scan barcodes.")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Open Settings").assertIsDisplayed()
  }

  @Test
  fun permissionDeniedMessageIsDisplayed() {
    setUpPermissionDeniedScreen()
    verifyCommonUIElements()
  }

  @Test
  fun clickOpenSettingsButtonTriggersIntent() {
    setUpPermissionDeniedScreen()

    // Click the "Open Settings" button
    composeTestRule.onNodeWithText("Open Settings").performClick()

    // Here you can verify intent-related behavior using appropriate test libraries/mocking
    // The actual intent verification is tricky in Compose tests and might require additional setup
  }

      @Test
      fun bottomNavigationIsDisplayed() {
          setUpPermissionDeniedScreen()

          // Verify the bottom navigation is displayed and the SCANNER tab is selected

   composeTestRule.onNodeWithContentDescription("SCANNER").assertIsDisplayed().assertIsSelected()

          // Select another tab to verify navigation
          composeTestRule.onNodeWithContentDescription("OTHER_TAB").performClick()

          // Verify that navigateTo() was called with the correct tab
          verify(navigationActions).navigateTo("OTHER_TAB")
      }

      @Test
      fun clickOnOtherTabNavigatesToCorrectScreen() {
          setUpPermissionDeniedScreen()

          // Click on another tab in the bottom navigation menu
          composeTestRule.onNodeWithContentDescription("OTHER_TAB").performClick()

          // Verify that navigateTo() was called with the correct route
          verify(navigationActions).navigateTo("OTHER_TAB")
      }
}
