package com.android.shelfLife.ui.camera

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BarcodeScannerScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navigationActions: NavigationActions
    private lateinit var barcodeScannerViewModel: BarcodeScannerViewModel
    private lateinit var foodFactsViewModel: FoodFactsViewModel
    private lateinit var householdViewModel: HouseholdViewModel
    private lateinit var foodItemViewModel: ListFoodItemsViewModel

    private lateinit var fakeRepository: FakeFoodFactsRepository

    @Before
    fun setUp() {
        // Initialize MockK
        MockKAnnotations.init(this, relaxed = true)

        navigationActions = mockk(relaxed = true)
        barcodeScannerViewModel = mockk(relaxed = true)
        householdViewModel = mockk(relaxed = true)
        foodItemViewModel = mockk(relaxed = true)

        // Mock other interactions
        every { navigationActions.currentRoute() } returns Route.SCANNER
        every { barcodeScannerViewModel.permissionGranted } returns true

        // Initialize the fake repository and real ViewModel
        fakeRepository = FakeFoodFactsRepository()
        foodFactsViewModel = FoodFactsViewModel(fakeRepository)
    }

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

    @Test
    fun scannedItemFoodScreenIsDisplayedAfterScanning() {
        // Set up the fake repository to return a sample food item
        val sampleFoodFacts = FoodFacts(
            name = "Sample Food",
            barcode = "1234567890",
            quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
            category = FoodCategory.OTHER
        )
        fakeRepository.foodFactsList = listOf(sampleFoodFacts)

        // Simulate the scanning process
        composeTestRule.setContent {
            BarcodeScannerScreen(
                navigationActions = navigationActions,
                cameraViewModel = barcodeScannerViewModel,
                foodFactsViewModel = foodFactsViewModel,
                householdViewModel = householdViewModel,
                foodItemViewModel = foodItemViewModel
            )
        }

        // Simulate scanning a barcode
        composeTestRule.activity.runOnUiThread {
            foodFactsViewModel.searchByBarcode(1234567890L)
        }

        // Wait for the UI to update
        composeTestRule.waitForIdle()

        // Check that ScannedItemFoodScreen is displayed
        composeTestRule.onNodeWithTag("scannedItemFoodScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("locationDropdown").assertIsDisplayed()
        composeTestRule.onNodeWithTag("expireDateTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("openDateTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("buyDateTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("submitButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    }

    @Test
    fun clickingBackButtonReturnsToScanning() {
        var onFinishCalled = false

        composeTestRule.setContent {
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

//    @Test
//    fun submittingFormAddsFoodItem() {
//        composeTestRule.setContent {
//            ScannedItemFoodScreen(
//                houseHoldViewModel = householdViewModel,
//                foodFacts = FoodFacts(
//                    name = "Sample Food",
//                    barcode = "1234567890",
//                    quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
//                    category = FoodCategory.OTHER
//                ),
//                foodItemViewModel = foodItemViewModel,
//                onFinish = {}
//            )
//        }
//
//        // Fill in the form
//        composeTestRule.onNodeWithTag("expireDateTextField").performTextInput("31/12/2023")
//        composeTestRule.onNodeWithTag("openDateTextField").performTextInput("01/01/2023")
//        composeTestRule.onNodeWithTag("buyDateTextField").performTextInput("15/01/2023")
//
//        // Click submit
//        composeTestRule.onNodeWithTag("submitButton").performClick()
//
//        // Verify that addFoodItem was called
//        verify { householdViewModel.addFoodItem(any()) }
//    }

    @Test
    fun cancellingFormReturnsToScanning() {
        var onFinishCalled = false

        composeTestRule.setContent {
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

    // Include the FakeFoodFactsRepository within the test class or as a nested class
    inner class FakeFoodFactsRepository : FoodFactsRepository {
        var shouldReturnError = false
        var foodFactsList = listOf<FoodFacts>()

        override fun searchFoodFacts(
            searchInput: FoodSearchInput,
            onSuccess: (List<FoodFacts>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            if (shouldReturnError) {
                onFailure(Exception("Test exception"))
            } else {
                onSuccess(foodFactsList)
            }
        }
    }
}
