package com.android.shelflife.ui.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.android.shelfLife.model.invitations.InvitationViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.TopLevelDestinations
import com.android.shelfLife.ui.profile.ProfileScreen
import io.mockk.MockKAnnotations
import io.mockk.mockk
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
  private lateinit var householdViewModel: HouseholdViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxUnitFun = true)
    navigationActions = mock()
    val mockRepository: HouseholdRepositoryFirestore = mockk(relaxed = true)
    val mockListFoodItemsViewModel: ListFoodItemsViewModel = mockk(relaxed = true)

    householdViewModel =
        HouseholdViewModel(
            mockRepository,
            mockListFoodItemsViewModel,
            mockk<InvitationRepositoryFirestore>(relaxed = true))
  }

  @Test
  fun testBottomNavigationMenuFromProfileToOverview() {
    composeTestRule.setContent {
      ProfileScreen(
          navigationActions = navigationActions,
          invitationViewModel =
              InvitationViewModel(mockk<InvitationRepositoryFirestore>(relaxed = true)))
    }
    composeTestRule.onNodeWithTag(TopLevelDestinations.OVERVIEW.textId).performClick()
    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
  }
}
