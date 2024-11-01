//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.compose.ui.test.performClick
//import com.android.shelfLife.ui.utils.DropdownFields
//import org.junit.Rule
//import org.junit.Test
//
//class DropdownFieldsTest {
//
//  @get:Rule val composeTestRule = createComposeRule()
//
//  @Test
//  fun dropdownFieldsIsDisplayed() {
//    composeTestRule.setContent {
//      DropdownFields(
//          label = "Select Option",
//          options = arrayOf("Option 1", "Option 2", "Option 3"),
//          selectedOption = "Option 1",
//          onOptionSelected = {},
//          expanded = false,
//          onExpandedChange = {},
//          optionLabel = { it })
//    }
//    composeTestRule.onNodeWithTag("dropDownField").assertIsDisplayed()
//  }
//
//  @Test
//  fun dropdownFieldsOptionsAreDisplayed() {
//    composeTestRule.setContent {
//      DropdownFields(
//          label = "Select Option",
//          options = arrayOf("Option 1", "Option 2", "Option 3"),
//          selectedOption = "Option 1",
//          onOptionSelected = { /* Do nothing */},
//          expanded = true,
//          onExpandedChange = { /* Do nothing */},
//          optionLabel = { it })
//    }
//
//    composeTestRule.onNodeWithTag("dropDownItem_Option 1").assertIsDisplayed()
//    composeTestRule.onNodeWithTag("dropDownItem_Option 2").assertIsDisplayed()
//    composeTestRule.onNodeWithTag("dropDownItem_Option 3").assertIsDisplayed()
//  }
//
//  @Test
//  fun dropdownFieldsOptionIsSelected() {
//    var selectedOption = "Option 1"
//
//    composeTestRule.setContent {
//      DropdownFields(
//          label = "Select Option",
//          options = arrayOf("Option 1", "Option 2", "Option 3"),
//          selectedOption = selectedOption,
//          onOptionSelected = { option -> selectedOption = option },
//          expanded = true,
//          onExpandedChange = { /* Do nothing */},
//          optionLabel = { it })
//    }
//
//    composeTestRule.onNodeWithTag("dropDownItem_Option 2").performClick()
//
//    assert(selectedOption == "Option 2")
//  }
//}
