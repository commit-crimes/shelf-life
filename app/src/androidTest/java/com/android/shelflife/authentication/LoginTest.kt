import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.ui.authentication.SignInScreen
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.authentication.SignInViewModel
import com.example.compose.ShelfLifeTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// LoginTest.kt
@RunWith(AndroidJUnit4::class)
class LoginTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var signInViewModel: SignInViewModel

  @Before
  fun setup() {
    composeTestRule.activityRule.scenario.onActivity { activity ->
      // Initialize the ViewModel
      signInViewModel = SignInViewModel()

      // Set the content of the activity
      activity.setContent {
        ShelfLifeTheme {
          SignInScreen(
              navigationActions = NavigationActions(TestNavHostController(activity)),
              signInViewModel = signInViewModel)
        }
      }
    }
  }

  @Test
  fun titleAndButtonAreCorrectlyDisplayed() {
    // Assert that the login screen is displayed
    composeTestRule.onNodeWithTag("signInScreen").assertIsDisplayed()

    // Assert that the title and login button are displayed
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("Shelf Life")
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
  }

  //  @Test
  //  fun googleSignInShowsLoadingIndicator() {
  //    // Simulate clicking the login button
  //    composeTestRule.onNodeWithTag("loginButton").performClick()
  //
  //    // Use the test helper method to simulate loading
  //    signInViewModel.setSignInStateForTesting(SignInState.Loading)
  //
  //    // Assert that the loading indicator is displayed
  //    composeTestRule.onNodeWithTag("signInLoadingIndicator").assertIsDisplayed()
  //  }
}
