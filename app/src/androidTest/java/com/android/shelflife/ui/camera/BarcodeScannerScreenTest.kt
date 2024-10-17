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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class BarcodeScannerScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var barcodeScannerViewModel: BarcodeScannerViewModel
  private lateinit var foodFactsViewModel: FoodFactsViewModel
  private lateinit var householdViewModel: HouseholdViewModel
  private lateinit var foodItemViewModel: ListFoodItemsViewModel

  @Before
  fun setUp() {
    navigationActions = mock()
    barcodeScannerViewModel = mock()
    foodFactsViewModel = mock()
    householdViewModel = mock()
    foodItemViewModel = mock()

    whenever(navigationActions.currentRoute()).thenReturn(Route.SCANNER)
    whenever(barcodeScannerViewModel.permissionGranted).thenReturn(true)
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
          foodItemViewModel = foodItemViewModel)
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
    whenever(barcodeScannerViewModel.permissionGranted).thenReturn(false)

    composeTestRule.setContent {
      BarcodeScannerScreen(
          navigationActions = navigationActions,
          cameraViewModel = barcodeScannerViewModel,
          foodFactsViewModel = foodFactsViewModel,
          householdViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Verify navigation to permission handler
    verify(navigationActions).navigateTo(Screen.PERMISSION_HANDLER)
  }

  // Test that ScannedItemFoodScreen is displayed after scanning
  @Test
  fun scannedItemFoodScreenIsDisplayedAfterScanning() {
    // Mock the scanning process
    whenever(barcodeScannerViewModel.permissionGranted).thenReturn(true)

    // Prepare a sample FoodFacts object with non-null quantity and category
    val sampleFoodFacts =
        FoodFacts(
            name = "Sample Food",
            barcode = "1234567890",
            quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
            category = FoodCategory.OTHER)

    // Mock the foodFactsViewModel to return success and the sample food facts
    whenever(foodFactsViewModel.searchStatus)
        .thenReturn(mock { on { value } doReturn SearchStatus.Success })
    whenever(foodFactsViewModel.foodFactsSuggestions)
        .thenReturn(mock { on { value } doReturn listOf(sampleFoodFacts) })

    composeTestRule.setContent {
      // We need to directly call ScannedItemFoodScreen to test it
      ScannedItemFoodScreen(
          houseHoldViewModel = householdViewModel,
          foodFacts = sampleFoodFacts,
          foodItemViewModel = foodItemViewModel,
          onFinish = {})
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
          foodFacts =
              FoodFacts(
                  name = "Sample Food",
                  barcode = "1234567890",
                  quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
                  category = FoodCategory.OTHER),
          foodItemViewModel = foodItemViewModel,
          onFinish = { onFinishCalled = true })
    }

    // Click the back button
    composeTestRule.onNodeWithTag("backButton").performClick()

    // Assert that onFinish was called
    assert(onFinishCalled)
  }

  // Test submitting the form adds a food item
  @Test
  fun submittingFormAddsFoodItem() {
    composeTestRule.setContent {
      // We need to directly call ScannedItemFoodScreen to test it
      ScannedItemFoodScreen(
          houseHoldViewModel = householdViewModel,
          foodFacts =
              FoodFacts(
                  name = "Sample Food",
                  barcode = "1234567890",
                  quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
                  category = FoodCategory.OTHER),
          foodItemViewModel = foodItemViewModel,
          onFinish = {})
    }

    // Fill in the form
    composeTestRule.onNodeWithTag("expireDateTextField").performTextInput("31/12/2023")
    composeTestRule.onNodeWithTag("openDateTextField").performTextInput("01/01/2023")
    composeTestRule.onNodeWithTag("buyDateTextField").performTextInput("15/01/2023")

    // Click submit
    composeTestRule.onNodeWithTag("submitButton").performClick()

    // Verify that addFoodItem was called
    verify(householdViewModel).addFoodItem(any())
  }

  // Test cancelling the form returns to scanning
  @Test
  fun cancellingFormReturnsToScanning() {
    var onFinishCalled = false

    composeTestRule.setContent {
      // We need to directly call ScannedItemFoodScreen to test it
      ScannedItemFoodScreen(
          houseHoldViewModel = householdViewModel,
          foodFacts =
              FoodFacts(
                  name = "Sample Food",
                  barcode = "1234567890",
                  quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
                  category = FoodCategory.OTHER),
          foodItemViewModel = foodItemViewModel,
          onFinish = { onFinishCalled = true })
    }

    // Click the cancel button
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    // Assert that onFinish was called
    assert(onFinishCalled)
  }

  // Additional tests can be added as needed
}
