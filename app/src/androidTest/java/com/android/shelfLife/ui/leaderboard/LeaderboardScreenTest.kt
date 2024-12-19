package com.android.shelfLife.ui.leaderboard

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.HouseholdRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class LeaderboardScreenTest {

  @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var userRepository: UserRepository

  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper

  private lateinit var navigationActions: NavigationActions
  private lateinit var leaderboardViewModel: LeaderboardViewModel

  private val selectedHousehold = MutableStateFlow<HouseHold?>(null)

  @Before
  fun setUp() {
    hiltAndroidTestRule.inject()
    navigationActions = mock()

    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)
    userRepositoryTestHelper = UserRepositoryTestHelper(userRepository)

    val realUser =
        User(
            uid = "currentUserId",
            username = "Current User",
            email = "currentUser@example.com",
            photoUrl = null,
            householdUIDs = emptyList(),
            selectedHouseholdUID = null,
            recipeUIDs = emptyList())
    userRepositoryTestHelper.setUser(realUser)

    leaderboardViewModel =
        LeaderboardViewModel(
            houseHoldRepository = houseHoldRepository, userRepository = userRepository)
  }

  @Test
  fun leaderboardScreen_displaysTopBarAndNoDataMessageIfEmpty(): Unit = runBlocking {
    selectedHousehold.value =
        HouseHold(
            uid = "house1",
            name = "TestHouse",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    composeTestRule.setContent {
      LeaderboardScreen(navigationActions = navigationActions, viewModel = leaderboardViewModel)
    }

    composeTestRule.onNodeWithText("Leaderboards").assertIsDisplayed()
    composeTestRule.onNodeWithText("No data available").assertIsDisplayed()
  }

  @Test
  fun leaderboardScreen_clickingTopBarBackButton_callsGoBack() {
    composeTestRule.setContent {
      LeaderboardScreen(navigationActions = navigationActions, viewModel = leaderboardViewModel)
    }

    composeTestRule.onNodeWithText("Leaderboards").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackArrow").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun leaderboardScreen_handlesEmptyLeaderListGracefully(): Unit = runBlocking {
    selectedHousehold.value =
        HouseHold(
            uid = "house1",
            name = "TestHouse",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap())

    composeTestRule.setContent {
      LeaderboardScreen(navigationActions = navigationActions, viewModel = leaderboardViewModel)
    }

    composeTestRule.onNodeWithText("No data available").assertIsDisplayed()
  }
}
