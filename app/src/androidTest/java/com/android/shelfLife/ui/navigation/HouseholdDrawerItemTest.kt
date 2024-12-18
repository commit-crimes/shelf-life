package com.android.shelfLife.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.household.HouseHold
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HouseholdDrawerItemTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  private lateinit var household: HouseHold
  private lateinit var instrumentationContext: android.content.Context

  @Before
  fun setUp() {
    hiltRule.inject()

    instrumentationContext = InstrumentationRegistry.getInstrumentation().context

    // Initialize the household data
    household =
        HouseHold(
            uid = "1",
            name = "Test Household",
            members = listOf("John", "Doe"),
            // If the real HouseHold class doesn't have foodItems, adjust accordingly.
            // Assuming a HouseHold class similar to your code snippet:
            sharedRecipes = emptyList(),
            ratPoints = emptyMap(),
            stinkyPoints = emptyMap(),
        )
  }

  @Test
  fun editHouseholdIndicatorIconIsVisibleInEditMode() {
    // Act: Set the content with editMode = true
    composeTestRule.setContent {
      HouseholdDrawerItem(
          household = household,
          selectedHousehold = household,
          editMode = true,
          onHouseholdSelected = {},
          onHouseholdEditSelected = {},
          onHouseholdDeleteSelected = {})
    }

    composeTestRule.waitForIdle()

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
          editMode = false,
          onHouseholdSelected = {},
          onHouseholdEditSelected = {},
          onHouseholdDeleteSelected = {})
    }

    composeTestRule.waitForIdle()

    // Assert: Check if the editHouseholdIndicatorIcon does not exist
    composeTestRule.onNodeWithTag("editHouseholdIndicatorIcon").assertDoesNotExist()
  }
}
