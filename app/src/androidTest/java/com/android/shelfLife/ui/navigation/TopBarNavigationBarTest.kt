package com.android.shelfLife.ui.newnavigation

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.household.HouseHold

import com.android.shelfLife.ui.navigation.FilterChipItem
import com.android.shelfLife.ui.navigation.HouseHoldElement
import com.android.shelfLife.ui.navigation.TopNavigationBar
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TopNavigationBarTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var testHouseHold: HouseHold
    private lateinit var testFoodFacts: FoodFacts
    private lateinit var testNutritionFacts: NutritionFacts
    private lateinit var selectedHouseHold: HouseHold

    private lateinit var instrumentationContext: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context

        testNutritionFacts =
            NutritionFacts(
                energyKcal = 52,
                fat = 0.2,
                saturatedFat = 0.1,
                carbohydrates = 14.0,
                sugars = 10.0,
                proteins = 0.3,
                salt = 0.01
            )

        testFoodFacts =
            FoodFacts(
                name = "Apple",
                barcode = "1234567890",
                quantity = Quantity(1.0),
                nutritionFacts = testNutritionFacts
            )

        testHouseHold =
            HouseHold(
                uid = "householdId123",
                name = "Test Household",
                members = listOf("member1", "member2"),
                sharedRecipes = emptyList(),
                ratPoints = emptyMap(),
                stinkyPoints = emptyMap()
            )

        selectedHouseHold = testHouseHold

    }

    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun topNavigationBar_hamburgerIconDisplayed() {
        composeTestRule.setContent {
            TopNavigationBar(
                houseHold = testHouseHold,
                onHamburgerClick = {},
                filters = listOf(),
                selectedFilters = listOf(),
                onFilterChange = { _, _ -> })
        }

        composeTestRule
            .onNodeWithTag("hamburgerIcon")
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun topNavigationBar_titleDisplayed() {
        composeTestRule.setContent {
            TopNavigationBar(
                houseHold = testHouseHold,
                onHamburgerClick = {},
                filters = listOf(),
                selectedFilters = listOf(),
                onFilterChange = { _, _ -> })
        }

        composeTestRule.onNodeWithText("Test Household").assertExists().assertIsDisplayed()
    }

    @Test
    fun topNavigationBar_filterIconDisplayed_whenFiltersExist() {
        val filters = listOf("Filter1", "Filter2")

        composeTestRule.setContent {
            TopNavigationBar(
                houseHold = testHouseHold,
                onHamburgerClick = {},
                filters = filters,
                selectedFilters = listOf(),
                onFilterChange = { _, _ -> })
        }

        composeTestRule.onNodeWithTag("filterIcon").assertExists().assertIsDisplayed()
    }

    @Test
    fun topNavigationBar_filterIconNotDisplayed_whenNoFilters() {
        composeTestRule.setContent {
            TopNavigationBar(
                houseHold = testHouseHold,
                onHamburgerClick = {},
                filters = listOf(),
                selectedFilters = listOf(),
                onFilterChange = { _, _ -> })
        }

        composeTestRule.onNodeWithTag("filterIcon").assertDoesNotExist()
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun filterBar_isDisplayed_whenFilterIconClicked() {
        val filters = listOf("Filter1", "Filter2")

        composeTestRule.setContent {
            TopNavigationBar(
                houseHold = testHouseHold,
                onHamburgerClick = {},
                filters = filters,
                selectedFilters = listOf(),
                onFilterChange = { _, _ -> })
        }

        // Click the filter icon to show filter bar
        composeTestRule.onNodeWithTag("filterIcon").performClick()

        composeTestRule.onNodeWithTag("filterBar").assertExists().assertIsDisplayed()
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun filterBar_displaysCorrectFilters() {
        val filters = listOf("Filter1", "Filter2", "Filter3")

        composeTestRule.setContent {
            TopNavigationBar(
                houseHold = testHouseHold,
                onHamburgerClick = {},
                filters = filters,
                selectedFilters = listOf(),
                onFilterChange = { _, _ -> })
        }

        // Show filter bar
        composeTestRule.onNodeWithTag("filterIcon").performClick()

        // Assert each filter is displayed
        filters.forEach { filter ->
            composeTestRule.onNodeWithText(filter).assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun filterChipItem_toggleSelection() {
        val text = "TestFilter"

        composeTestRule.setContent {
            val isSelected = remember { mutableStateOf(false) }

            FilterChipItem(
                text = text,
                isSelected = isSelected.value,
                onClick = { isSelected.value = !isSelected.value }
            )
        }

        // Initially not selected
        composeTestRule.onNodeWithText(text).assertExists().assertIsDisplayed()

        // Click to toggle selection
        composeTestRule.onNodeWithText(text).performClick()

        // After selection, icon should appear
        composeTestRule.onNodeWithContentDescription("Selected").assertExists().assertIsDisplayed()
    }

    @Test
    fun houseHoldElement_displaysCorrectly() {
        composeTestRule.setContent {
            HouseHoldElement(
                household = testHouseHold,
                selectedHousehold = selectedHouseHold,
                onHouseholdSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Test Household").assertExists().assertIsDisplayed()
    }

    @Test
    fun houseHoldElement_isSelected() {
        composeTestRule.setContent {
            HouseHoldElement(
                household = testHouseHold,
                selectedHousehold = selectedHouseHold,
                onHouseholdSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Test Household")
            .assertExists()
            .assertIsDisplayed()
            .assert(hasText("Test Household", ignoreCase = true))
    }

    @Test
    fun houseHoldElement_onClick() {
        var clickedHouseHold: HouseHold? = null

        composeTestRule.setContent {
            HouseHoldElement(
                household = testHouseHold,
                selectedHousehold = selectedHouseHold,
                onHouseholdSelected = { clickedHouseHold = it }
            )
        }

        composeTestRule.onNodeWithText("Test Household").performClick()
        assert(clickedHouseHold == testHouseHold)
    }
}