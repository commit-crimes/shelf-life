package com.android.shelfLife.ui.authentication

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.TopLevelDestinations
import com.android.shelfLife.viewmodel.authentication.SignInState
import com.android.shelfLife.viewmodel.authentication.SignInViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class SignInTest {

    @get:Rule(order = 0) val hiltAndroidTestRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var navigationActions: NavigationActions

    @Inject lateinit var houseHoldRepository: HouseHoldRepository
    @Inject lateinit var listFoodItemsRepository: FoodItemRepository
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var recipeRepository: RecipeRepository

    private lateinit var signInViewModel: SignInViewModel
    private lateinit var instrumentationContext: Context


    @Before
    fun setUp() {
        hiltAndroidTestRule.inject()
        navigationActions = mock()

        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        whenever(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)

        signInViewModel = SignInViewModel(
            firebaseAuth = firebaseAuth,
            userRepository = userRepository,
            householdRepository = houseHoldRepository,
            foodItemRepository = listFoodItemsRepository,
            recipeRepository = recipeRepository,
            appContext = instrumentationContext
        )
    }


    @Test
    fun signInScreenDisplaysCorrectUIElements() {
        composeTestRule.setContent {
            SignInScreen(
                navigationActions = navigationActions,
                signInViewModel = signInViewModel
            )
        }

        // Check that the sign in screen is displayed
        composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()

        // Check that the login title is displayed
        composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()

        // Check that the login button is displayed
        composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    }

    @Test
    fun signInScreenShowsLoadingIndicatorWhenInLoadingState(): Unit = runBlocking {
        composeTestRule.setContent {
            SignInScreen(
                navigationActions = navigationActions,
                signInViewModel = signInViewModel
            )
        }

        // Set the SignInState to Loading
        composeTestRule.runOnUiThread {
            signInViewModel.setSignInStateForTesting(SignInState.Loading)
        }

        // Wait for UI updates
        composeTestRule.waitForIdle()

        // Check that the loading indicator is displayed
        composeTestRule.onNodeWithTag("signInLoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun signInSuccessNavigatesToOverview() = runBlocking {
        composeTestRule.setContent {
            SignInScreen(
                navigationActions = navigationActions,
                signInViewModel = signInViewModel
            )
        }

        // Set the SignInState to Success
        composeTestRule.runOnUiThread {
            signInViewModel.setSignInStateForTesting(SignInState.Success(mock()))
        }

        // Wait for UI updates
        composeTestRule.waitForIdle()

        // Verify that navigation to OVERVIEW is triggered
        verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
    }

    @Test
    fun signInErrorDoesNotNavigateToOverview() = runBlocking {
        composeTestRule.setContent {
            SignInScreen(
                navigationActions = navigationActions,
                signInViewModel = signInViewModel
            )
        }

        // Set the SignInState to Error
        composeTestRule.runOnUiThread {
            signInViewModel.setSignInStateForTesting(SignInState.Error("Test Error"))
        }

        // Wait for UI updates
        composeTestRule.waitForIdle()

        // Verify that navigation to OVERVIEW is not called in case of Error
        verify(navigationActions, never()).navigateTo(TopLevelDestinations.OVERVIEW)
    }

    @Test
    fun userAlreadyLoggedInNavigatesToOverviewOnLaunch() = runBlocking {
        // Set the SignInState to Success before composing
        composeTestRule.runOnUiThread {
            signInViewModel.setSignInStateForTesting(SignInState.Success(mock()))
        }

        // Render the SignInScreen
        composeTestRule.setContent {
            SignInScreen(
                navigationActions = navigationActions,
                signInViewModel = signInViewModel
            )
        }

        // Wait for UI updates
        composeTestRule.waitForIdle()

        // Verify that navigation to OVERVIEW is triggered
        verify(navigationActions).navigateTo(eq(TopLevelDestinations.OVERVIEW))
    }
}