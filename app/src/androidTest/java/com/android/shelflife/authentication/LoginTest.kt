package com.android.shelfLife

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class MainActivityTest : TestCase() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule val intentsTestRule = IntentsTestRule(MainActivity::class.java)

  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setup() {
    firebaseAuth = FirebaseAuth.getInstance()
    firebaseAuth.signOut() // Ensure the user is signed out before each test
    navigationActions = mock(NavigationActions::class.java)
  }

  @Test
  fun titleAndButtonAreCorrectlyDisplayed() {
    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("Shelf Life")

    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
  }

  @Test
  fun googleSignInReturnsValidActivityResult() {
    composeTestRule.onNodeWithTag("loginButton").performClick()

    // Assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    intended(toPackage("com.google.android.gms"))
  }

  @Test
  fun overviewScreenDisplaysWhenLoggedIn() {
    firebaseAuth.signInAnonymously().addOnCompleteListener {
      if (it.isSuccessful) {
        verify(navigationActions).navigateToAndClearBackStack(Screen.OVERVIEW)
        composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
      }
    }
  }

  @Test
  fun addFoodScreenAccessibleFromOverview() {
    firebaseAuth.signInAnonymously().addOnCompleteListener {
      if (it.isSuccessful) {
        composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("navigateToAddFood").performClick()
        composeTestRule.onNodeWithTag("addFoodScreen").assertIsDisplayed()
      }
    }
  }
  //
  //  @Test
  //  fun barcodeScannerScreenAccessibleWhenPermissionGranted() {
  //    firebaseAuth.signInAnonymously().addOnCompleteListener {
  //      if (it.isSuccessful) {
  //        composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
  //        composeTestRule.onNodeWithTag("navigateToBarcodeScanner").performClick()
  //        composeTestRule.onNodeWithTag("barcodeScannerScreen").assertIsDisplayed()
  //      }
  //    }
  //  }

  @Test
  fun recipeScreenAccessibleFromOverview() {
    firebaseAuth.signInAnonymously().addOnCompleteListener {
      if (it.isSuccessful) {
        composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("navigateToRecipes").performClick()
        composeTestRule.onNodeWithTag("recipeScreen").assertIsDisplayed()
      }
    }
  }

  @Test
  fun profileScreenAccessibleFromOverview() {
    firebaseAuth.signInAnonymously().addOnCompleteListener {
      if (it.isSuccessful) {
        composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("navigateToProfile").performClick()
        composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()
      }
    }
  }
}
