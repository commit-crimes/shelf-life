package com.android.shelfLife.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.overview.FirstTimeWelcomeScreenViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.FoodItemRepositoryTestHelper
import helpers.HouseholdRepositoryTestHelper
import helpers.UserRepositoryTestHelper
import javax.inject.Inject
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class FirstTimeWelcomeScreenTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var houseHoldRepository: HouseHoldRepository
  @Inject lateinit var foodItemRepository: FoodItemRepository
  @Inject lateinit var userRepository: UserRepository

  private lateinit var userRepositoryTestHelper: UserRepositoryTestHelper
  private lateinit var householdRepositoryTestHelper: HouseholdRepositoryTestHelper
  private lateinit var foodItemRepositoryTestHelper: FoodItemRepositoryTestHelper

  private lateinit var instrumentationContext: android.content.Context
  private lateinit var navigationActions: NavigationActions
  private lateinit var overviewScreenViewModel: FirstTimeWelcomeScreenViewModel

  // Mocked flows
  private val householdsFlow = MutableStateFlow<List<HouseHold>>(emptyList())
  private val selectedHouseholdFlow = MutableStateFlow<HouseHold?>(null)
  private val userFlow = MutableStateFlow<User?>(null)
  private val foodItemsFlow = MutableStateFlow<List<FoodItem>>(emptyList())

  @Before
  fun setUp() {
    hiltRule.inject()
    navigationActions = mock()
    instrumentationContext = InstrumentationRegistry.getInstrumentation().context

    userRepositoryTestHelper = UserRepositoryTestHelper(userRepository)
    householdRepositoryTestHelper = HouseholdRepositoryTestHelper(houseHoldRepository)
    foodItemRepositoryTestHelper = FoodItemRepositoryTestHelper(foodItemRepository)

    // Provide a user
    val realUser =
        User(
            uid = "currentUserId",
            username = "Current User",
            email = "user@example.com",
            photoUrl = null,
            householdUIDs = listOf("household123"),
            selectedHouseholdUID = "household123",
            recipeUIDs = emptyList())
    userRepositoryTestHelper.setUser(realUser)

    // Provide a selected household
    val exampleSelectedHousehold =
        HouseHold(
            uid = "household123",
            name = "Example Household",
            members = listOf("currentUserId", "member2"),
            sharedRecipes = emptyList(),
            ratPoints = mapOf("currentUserId" to 10),
            stinkyPoints = mapOf("member2" to 5))
    householdRepositoryTestHelper.selectHousehold(exampleSelectedHousehold)

    createViewModel()
  }

  private fun createViewModel() {
    overviewScreenViewModel =
        FirstTimeWelcomeScreenViewModel(
            houseHoldRepository = houseHoldRepository,
        )
    assertNotNull("ViewModel should not be null", overviewScreenViewModel)
  }

  private fun setContent() {
    composeTestRule.setContent {
      FirstTimeWelcomeScreen(
          navigationActions = navigationActions, overviewScreenViewModel = overviewScreenViewModel)
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun firstTimeWelcomeScreen_displaysWelcomeUI() {
    setContent()

    // Assert that the main UI components are displayed
    composeTestRule.onNodeWithTag("firstTimeWelcomeScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Welcome to ShelfLife!").assertIsDisplayed()
    composeTestRule.onNodeWithText("Get started by creating your Household").assertIsDisplayed()
    composeTestRule.onNodeWithTag("householdNameSaveButton").assertIsDisplayed()
  }
}
