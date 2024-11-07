package com.android.shelflife.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.ui.navigation.HouseholdDrawerItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HouseHoldElementTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var household: HouseHold

  @Before
  fun setUp() {
    household =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            foodItems = listOf())
  }

  @Test
  fun editHouseholdIndicatorIconIsVisibleInEditMode() {

    // Act: Set the content with editMode = true
    composeTestRule.setContent {
      HouseholdDrawerItem(
          household = household,
          selectedHousehold = household,
          editMode = true, // Enable edit mode to show the edit icon
          onHouseholdSelected = {},
          onHouseholdEditSelected = {})
    }

    // Assert: Check if the editHouseholdIndicatorIcon is displayed
    composeTestRule.onNodeWithTag("editHouseholdIndicatorIcon").assertIsDisplayed()
  }

  @Test
  fun editHouseholdIndicatorIconIsNotVisibleWhenEditModeIsFalse() {

    // Act: Set the content with editMode = false
    composeTestRule.setContent {
      HouseholdDrawerItem(
          household = household,
          selectedHousehold = household,
          editMode = false, // Disable edit mode to hide the edit icon
          onHouseholdSelected = {},
          onHouseholdEditSelected = {})
    }

    // Assert: Check if the editHouseholdIndicatorIcon does not exist
    composeTestRule.onNodeWithTag("editHouseholdIndicatorIcon").assertDoesNotExist()
  }
}
