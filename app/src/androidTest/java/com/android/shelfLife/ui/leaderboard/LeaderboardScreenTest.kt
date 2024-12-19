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
import org.mockito.Mock
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

        val realUser = User(
            uid = "currentUserId",
            username = "Current User",
            email = "currentUser@example.com",
            photoUrl = null,
            householdUIDs = emptyList(),
            selectedHouseholdUID = null,
            recipeUIDs = emptyList()
        )
        userRepositoryTestHelper.setUser(realUser)

        leaderboardViewModel = LeaderboardViewModel(
            houseHoldRepository = houseHoldRepository,
            userRepository = userRepository
        )
    }

    @Test
    fun leaderboardScreen_displaysTopBarAndNoDataMessageIfEmpty(): Unit = runBlocking {
        selectedHousehold.value = HouseHold(
            uid = "house1",
            name = "TestHouse",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap()
        )

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
        composeTestRule.onNodeWithTag("goBackArrow").assertIsDisplayed().performClick()
        verify(navigationActions).goBack()
    }

    @Test
    fun leaderboardScreen_displaysTopLeaderAndOtherMembers(): Unit = runBlocking {
        // Set up test data
        selectedHousehold.value = HouseHold(
            uid = "house1",
            name = "TestHouse",
            members = listOf("user1", "user2", "user3"),
            sharedRecipes = emptyList(),
            ratPoints = mapOf("user1" to 50L, "user2" to 30L, "user3" to 10L),
            stinkyPoints = emptyMap()
        )

        whenever(userRepository.getUserNames(any())).thenReturn(
            mapOf("user1" to "Alice", "user2" to "Bob", "user3" to "Charlie")
        )


        composeTestRule.setContent {
            LeaderboardScreen(navigationActions = navigationActions, viewModel = leaderboardViewModel)
        }

        composeTestRule.onNodeWithText("Alice 👑").assertIsDisplayed()
        composeTestRule.onNodeWithText("50 points").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
        composeTestRule.onNodeWithText("30").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charlie").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun leaderboardScreen_togglesBetweenModes(): Unit = runBlocking {
        selectedHousehold.value = HouseHold(
            uid = "house1",
            name = "TestHouse",
            members = listOf("user1"),
            sharedRecipes = emptyList(),
            ratPoints = mapOf("user1" to 50L),
            stinkyPoints = mapOf("user1" to 20L)
        )

        whenever(userRepository.getUserNames(any())).thenReturn(mapOf("user1" to "Alice"))

        composeTestRule.setContent {
            LeaderboardScreen(navigationActions = navigationActions, viewModel = leaderboardViewModel)
        }

        // Initial mode is RAT
        composeTestRule.onNodeWithText("Alice 👑").assertIsDisplayed()
        composeTestRule.onNodeWithText("50 points").assertIsDisplayed()

        // Toggle mode to Stinky
        composeTestRule.onNodeWithText("Stinky Leaderboard").performClick()
        composeTestRule.waitForIdle()

        // Verify stinky points (still "Alice")
        composeTestRule.onNodeWithText("20 points").assertIsDisplayed()
    }

    @Test
    fun leaderboardScreen_displaysPrizeButtonIfUserIsKing(): Unit = runBlocking {
        selectedHousehold.value = HouseHold(
            uid = "house1",
            name = "TestHouse",
            members = listOf("currentUserId", "user2"),
            sharedRecipes = emptyList(),
            ratPoints = mapOf("currentUserId" to 100L, "user2" to 50L),
            stinkyPoints = emptyMap()
        )

        whenever(userRepository.getUserNames(any())).thenReturn(
            mapOf("currentUserId" to "KingUser", "user2" to "Bob")
        )

        composeTestRule.setContent {
            LeaderboardScreen(navigationActions = navigationActions, viewModel = leaderboardViewModel)
        }

        // Verify the prize button is displayed
        composeTestRule.onNodeWithText("Receive Your Prize").assertIsDisplayed()
    }

    @Test
    fun leaderboardScreen_handlesEmptyLeaderListGracefully(): Unit = runBlocking {
        selectedHousehold.value = HouseHold(
            uid = "house1",
            name = "TestHouse",
            members = emptyList(),
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap()
        )

        composeTestRule.setContent {
            LeaderboardScreen(navigationActions = navigationActions, viewModel = leaderboardViewModel)
        }

        composeTestRule.onNodeWithText("No data available").assertIsDisplayed()
    }
}