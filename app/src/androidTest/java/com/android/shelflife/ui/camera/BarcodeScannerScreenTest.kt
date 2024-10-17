package com.android.shelfLife.ui.camera

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BarcodeScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions
    private lateinit var barcodeScannerViewModel: BarcodeScannerViewModel
    private lateinit var foodFactsViewModel: FoodFactsViewModel
    private lateinit var householdViewModel: HouseholdViewModel
    private lateinit var foodItemViewModel: ListFoodItemsViewModel

    // Use real MutableStateFlow to simulate StateFlow behavior
    private lateinit var searchStatusFlow: MutableStateFlow<SearchStatus>
    private lateinit var foodFactsSuggestionsFlow: MutableStateFlow<List<FoodFacts>>

    @Before
    fun setUp() {
        navigationActions = mockk(relaxed = true)
        barcodeScannerViewModel = mockk(relaxed = true)
        householdViewModel = mockk(relaxed = true)
        foodItemViewModel = mockk(relaxed = true)

        // Initialize MutableStateFlow with default values
        searchStatusFlow = MutableStateFlow(SearchStatus.Success)
        foodFactsSuggestionsFlow = MutableStateFlow(emptyList())

        foodFactsViewModel = mockk {
            every { searchStatus } returns searchStatusFlow
            every { foodFactsSuggestions } returns foodFactsSuggestionsFlow
        }

        // Mock other interactions
        every { navigationActions.currentRoute() } returns Route.SCANNER
        every { barcodeScannerViewModel.permissionGranted } returns true
    }

    // Test if the BarcodeScannerScreen is displayed with all elements
    @Test
    fun barcodeScannerScreenIsDisplayedCorrectly() {
        composeTestRule.setContent {
            BarcodeScannerScreen(
                navigationActions = navigationActions,
                cameraViewModel = barcodeScannerViewModel,
                foodFactsViewModel = foodFactsViewModel,
                householdViewModel = householdViewModel,
                foodItemViewModel = foodItemViewModel
            )
        }

        // Check that the main screen is displayed
        composeTestRule.onNodeWithTag("barcodeScannerScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cameraPreviewBox").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scannerOverlay").assertIsDisplayed()
    }

    // Test that when permission is not granted, navigation to permission handler occurs
    @Test
    fun whenPermissionNotGranted_NavigateToPermissionHandler() {
        // Mock permission not granted
        every { barcodeScannerViewModel.permissionGranted } returns false

        composeTestRule.setContent {
            BarcodeScannerScreen(
                navigationActions = navigationActions,
                cameraViewModel = barcodeScannerViewModel,
                foodFactsViewModel = foodFactsViewModel,
                householdViewModel = householdViewModel,
                foodItemViewModel = foodItemViewModel
            )
        }

        // Verify navigation to permission handler
        verify { navigationActions.navigateTo(Screen.PERMISSION_HANDLER) }
    }

    // Test that ScannedItemFoodScreen is displayed after scanning
    @Test
    fun scannedItemFoodScreenIsDisplayedAfterScanning() {
        // Mock the scanning process
        every { barcodeScannerViewModel.permissionGranted } returns true

        // Prepare a sample FoodFacts object with non-null quantity and category
        val sampleFoodFacts = FoodFacts(
            name = "Sample Food",
            barcode = "1234567890",
            quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
            category = FoodCategory.OTHER
        )

        // Update the MutableStateFlow with the new search status and food facts
        searchStatusFlow.value = SearchStatus.Success
        foodFactsSuggestionsFlow.value = listOf(sampleFoodFacts)

        composeTestRule.setContent {
            // We need to directly call ScannedItemFoodScreen to test it
            ScannedItemFoodScreen(
                houseHoldViewModel = householdViewModel,
                foodFacts = sampleFoodFacts,
                foodItemViewModel = foodItemViewModel,
                onFinish = {}
            )
        }

        // Check that ScannedItemFoodScreen is displayed
        composeTestRule.onNodeWithTag("scannedItemFoodScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("locationDropdown").assertIsDisplayed()
        composeTestRule.onNodeWithTag("expireDateTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("openDateTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("buyDateTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("submitButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    }

    // Test clicking on back button returns to scanning
    @Test
    fun clickingBackButtonReturnsToScanning() {
        var onFinishCalled = false

        composeTestRule.setContent {
            // We need to directly call ScannedItemFoodScreen to test it
            ScannedItemFoodScreen(
                houseHoldViewModel = householdViewModel,
                foodFacts = FoodFacts(
                    name = "Sample Food",
                    barcode = "1234567890",
                    quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
                    category = FoodCategory.OTHER
                ),
                foodItemViewModel = foodItemViewModel,
                onFinish = { onFinishCalled = true }
            )
        }

        // Click the back button
        composeTestRule.onNodeWithTag("backButton").performClick()

        // Assert that onFinish was called
        assertTrue(onFinishCalled)
    }

    // Test submitting the form adds a food item
    @Test
    fun submittingFormAddsFoodItem() {
        composeTestRule.setContent {
            // We need to directly call ScannedItemFoodScreen to test it
            ScannedItemFoodScreen(
                houseHoldViewModel = householdViewModel,
                foodFacts = FoodFacts(
                    name = "Sample Food",
                    barcode = "1234567890",
                    quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
                    category = FoodCategory.OTHER
                ),
                foodItemViewModel = foodItemViewModel,
                onFinish = {}
            )
        }

        // Fill in the form
        composeTestRule.onNodeWithTag("expireDateTextField").performTextInput("31/12/2023")
        composeTestRule.onNodeWithTag("openDateTextField").performTextInput("01/01/2023")
        composeTestRule.onNodeWithTag("buyDateTextField").performTextInput("15/01/2023")

        // Click submit
        composeTestRule.onNodeWithTag("submitButton").performClick()

        // Verify that addFoodItem was called
        verify { householdViewModel.addFoodItem(any()) }
    }

    // Test cancelling the form returns to scanning
    @Test
    fun cancellingFormReturnsToScanning() {
        var onFinishCalled = false

        composeTestRule.setContent {
            // We need to directly call ScannedItemFoodScreen to test it
            ScannedItemFoodScreen(
                houseHoldViewModel = householdViewModel,
                foodFacts = FoodFacts(
                    name = "Sample Food",
                    barcode = "1234567890",
                    quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
                    category = FoodCategory.OTHER
                ),
                foodItemViewModel = foodItemViewModel,
                onFinish = { onFinishCalled = true }
            )
        }

        // Click the cancel button
        composeTestRule.onNodeWithTag("cancelButton").performClick()

        // Assert that onFinish was called
        assertTrue(onFinishCalled)
    }

    // Additional tests can be added as needed
}