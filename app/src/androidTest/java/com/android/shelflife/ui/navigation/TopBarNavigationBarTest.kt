package com.android.shelfLife.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.household.HouseHold
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopNavigationBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var testHouseHold: HouseHold
  private lateinit var testFoodFacts: FoodFacts
  private lateinit var testNutritionFacts: NutritionFacts

  @Before
  fun setUp() {
    // Set up a valid instance of NutritionFacts
    testNutritionFacts =
        NutritionFacts(
            energyKcal = 52,
            fat = 0.2,
            saturatedFat = 0.1,
            carbohydrates = 14.0,
            sugars = 10.0,
            proteins = 0.3,
            salt = 0.01)

    // Set up a valid instance of FoodFacts
    testFoodFacts =
        FoodFacts(
            name = "Apple",
            barcode = "1234567890",
            quantity = Quantity(1.0),
            nutritionFacts = testNutritionFacts // Use the correct NutritionFacts instance
            )

    // Set up a valid instance of HouseHold with all parameters
    testHouseHold =
        HouseHold(
            uid = "householdId123",
            name = "Test Household",
            members = listOf("member1", "member2"),
            foodItems =
                listOf(
                    FoodItem(
                        uid = "foodItemId1",
                        foodFacts = testFoodFacts,
                        location = FoodStorageLocation.PANTRY,
                        expiryDate = null,
                        openDate = null,
                        buyDate = Timestamp.now(),
                        status = FoodStatus.CLOSED)))
  }

  @OptIn(ExperimentalAnimationApi::class)
  @Test
  fun topNavigationBar_hamburgerIconDisplayed() {
    // Act
    composeTestRule.setContent {
      TopNavigationBar(
          houseHold = testHouseHold,
          onHamburgerClick = {},
          filters = listOf(),
          selectedFilters = listOf(),
          onFilterChange = { _, _ -> })
    }

    // Assert
    composeTestRule
        .onNodeWithTag("hamburgerIcon")
        .assertExists()
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun topNavigationBar_titleDisplayed() {
    // Act
    composeTestRule.setContent {
      TopNavigationBar(
          houseHold = testHouseHold,
          onHamburgerClick = {},
          filters = listOf(),
          selectedFilters = listOf(),
          onFilterChange = { _, _ -> })
    }

    // Assert
    composeTestRule.onNodeWithText("Test Household").assertExists().assertIsDisplayed()
  }

  @Test
  fun topNavigationBar_filterIconDisplayed_whenFiltersExist() {
    // Arrange
    val filters = listOf("Filter1", "Filter2")

    // Act
    composeTestRule.setContent {
      TopNavigationBar(
          houseHold = testHouseHold,
          onHamburgerClick = {},
          filters = filters,
          selectedFilters = listOf(),
          onFilterChange = { _, _ -> })
    }

    // Assert
    composeTestRule.onNodeWithTag("filterIcon").assertExists().assertIsDisplayed()
  }

  @Test
  fun topNavigationBar_filterIconNotDisplayed_whenNoFilters() {
    // Act
    composeTestRule.setContent {
      TopNavigationBar(
          houseHold = testHouseHold,
          onHamburgerClick = {},
          filters = listOf(),
          selectedFilters = listOf(),
          onFilterChange = { _, _ -> })
    }

    // Assert
    composeTestRule.onNodeWithTag("filterIcon").assertDoesNotExist()
  }

  @OptIn(ExperimentalAnimationApi::class)
  @Test
  fun filterBar_isDisplayed_whenFilterIconClicked() {
    // Arrange
    val filters = listOf("Filter1", "Filter2")

    // Act
    composeTestRule.setContent {
      TopNavigationBar(
          houseHold = testHouseHold,
          onHamburgerClick = {},
          filters = filters,
          selectedFilters = listOf(),
          onFilterChange = { _, _ -> })
    }

    // Click the filter icon to toggle the filter bar visibility
    composeTestRule.onNodeWithTag("filterIcon").performClick()

    // Assert
    composeTestRule.onNodeWithTag("filterBar").assertExists().assertIsDisplayed()
  }

  @OptIn(ExperimentalAnimationApi::class)
  @Test
  fun filterBar_displaysCorrectFilters() {
    // Arrange
    val filters = listOf("Filter1", "Filter2", "Filter3")

    // Act
    composeTestRule.setContent {
      TopNavigationBar(
          houseHold = testHouseHold,
          onHamburgerClick = {},
          filters = filters,
          selectedFilters = listOf(),
          onFilterChange = { _, _ -> })
    }

    // Click the filter icon to toggle the filter bar visibility
    composeTestRule.onNodeWithTag("filterIcon").performClick()

    // Assert each filter is displayed
    filters.forEach { filter ->
      composeTestRule.onNodeWithText(filter).assertExists().assertIsDisplayed()
    }
  }

  @Test
  fun filterChipItem_toggleSelection() {
    // Arrange
    val text = "TestFilter"

    // Act
    composeTestRule.setContent {
      // Use remember and mutableStateOf to manage the selection state
      val isSelected = remember { mutableStateOf(false) }

      FilterChipItem(
          text = text,
          isSelected = isSelected.value,
          onClick = {
            isSelected.value = !isSelected.value // Toggle the selection state
          })
    }

    // Assert initially not selected
    composeTestRule.onNodeWithText(text).assertExists().assertIsDisplayed()

    // Act: Simulate click to toggle selection
    composeTestRule.onNodeWithText(text).performClick()

    // Assert that the selection icon is displayed after selection
    composeTestRule.onNodeWithContentDescription("Selected").assertExists().assertIsDisplayed()
  }
}
