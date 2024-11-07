package com.android.shelflife.ui.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.TopLevelDestinations
import com.android.shelfLife.ui.profile.ProfileScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class BottomNavigationMenuTest {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationActions = mock()
  }

  @Test
  fun testBottomNavigationMenuFromProfileToOverview() {
    composeTestRule.setContent { ProfileScreen(navigationActions) }
    composeTestRule.onNodeWithTag(TopLevelDestinations.OVERVIEW.textId).performClick()
    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
  }
}
