import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.shelfLife.ui.utils.ErrorPopUp
import org.junit.Rule
import org.junit.Test

class ErrorPopUpTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun errorPopUpIsDisplayed() {
    composeTestRule.setContent {
      ErrorPopUp(showDialog = true, onDismiss = {}, errorMessages = listOf("An error occurred"))
    }

    composeTestRule.onNodeWithTag("errorDialog").assertIsDisplayed()
  }
}
