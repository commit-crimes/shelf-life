package com.android.shelfLife.ui.easteregg

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.ui.navigation.NavigationActions
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@HiltAndroidTest
class EasterEggScreenTest {

  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
    navigationActions = mock()
  }

  @Test
  fun easterEggScreenDisplaysTitleAndMessage() {
    composeTestRule.setContent { EasterEggScreen(navigationActions = navigationActions) }

    // Check that the top bar title is displayed
    composeTestRule.onNodeWithTag("eastereggTitle").assertIsDisplayed()

    // Check that the message "No recipe selected. Should not happen" is displayed
    composeTestRule.onNodeWithText("No recipe selected. Should not happen").assertIsDisplayed()
  }

  @Test
  fun clickingTopBarBackArrowCallsGoBack() {
    composeTestRule.setContent { EasterEggScreen(navigationActions = navigationActions) }

    // Assuming CustomTopAppBar or the back arrow has a known testTag "backArrowIcon"
    composeTestRule.onNodeWithTag("goBackArrow").performClick()

    // Verify that goBack() was called
    verify(navigationActions).goBack()
  }
}
