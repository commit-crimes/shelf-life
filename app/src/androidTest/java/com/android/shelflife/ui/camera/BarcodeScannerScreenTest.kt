package com.android.shelfLife.ui.camera

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.newnavigation.NavigationActions
import com.android.shelfLife.ui.newnavigation.Route
import com.android.shelfLife.ui.newnavigation.Screen
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BarcodeScannerScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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

    // Mock getUID() to return a valid UID
    every { foodItemViewModel.getUID() } returns "testUID"
    every { householdViewModel.addFoodItem(any()) } just runs

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
          foodItemViewModel = foodItemViewModel)
    }

    // Check that the main screen is displayed
    composeTestRule.onNodeWithTag("barcodeScannerScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cameraPreviewBox").assertIsDisplayed()
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
          foodItemViewModel = foodItemViewModel)
    }

    // Verify navigation to permission handler
    verify { navigationActions.navigateTo(Screen.PERMISSION_HANDLER) }
  }

  @Test
  fun scannedItemFoodScreenIsDisplayedAfterScanning() {
    // Set up the fake repository to return a sample food item
    val sampleFoodFacts =
        FoodFacts(
            name = "Sample Food",
            barcode = "1234567890",
            quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
            category = FoodCategory.OTHER)
    fakeRepository.foodFactsList = listOf(sampleFoodFacts)

    composeTestRule.setContent {
      BarcodeScannerScreen(
          navigationActions = navigationActions,
          cameraViewModel = barcodeScannerViewModel,
          foodFactsViewModel = foodFactsViewModel,
          householdViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Simulate scanning a barcode
    composeTestRule.activity.runOnUiThread { foodFactsViewModel.searchByBarcode(1234567890L) }

    // Wait until the bottom sheet is displayed with food details
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("Sample Food").fetchSemanticsNodes().isNotEmpty()
    }

    // Assert that the food details are displayed in the bottom sheet
    composeTestRule.onNodeWithText("Sample Food").assertIsDisplayed()

    // Wait until the input fields are displayed
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("expireDateTextField").fetchSemanticsNodes().isNotEmpty()
    }

    // Now assert that the input form is displayed
    composeTestRule.onNodeWithTag("locationDropdown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expireDateTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("openDateTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("buyDateTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("submitButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()

    // test dropdown
    composeTestRule.onNodeWithTag("locationDropdown").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Pantry").performClick()
    composeTestRule.onNodeWithTag("dropdownMenu_Select location").assertTextContains("Pantry")

    composeTestRule.onNodeWithTag("locationDropdown").performClick()
    composeTestRule.onNodeWithTag("dropDownItem_Fridge").performClick()
    composeTestRule.onNodeWithTag("dropdownMenu_Select location").assertTextContains("Fridge")
  }

  @Test
  fun cancellingFormReturnsToScanning() {
    // Set up the fake repository to return a sample food item
    val sampleFoodFacts =
        FoodFacts(
            name = "Sample Food",
            barcode = "1234567890",
            quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
            category = FoodCategory.OTHER)
    fakeRepository.foodFactsList = listOf(sampleFoodFacts)

    composeTestRule.setContent {
      BarcodeScannerScreen(
          navigationActions = navigationActions,
          cameraViewModel = barcodeScannerViewModel,
          foodFactsViewModel = foodFactsViewModel,
          householdViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Simulate scanning a barcode
    composeTestRule.activity.runOnUiThread { foodFactsViewModel.searchByBarcode(1234567890L) }

    // Wait until the bottom sheet is displayed with food details
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("Sample Food").fetchSemanticsNodes().isNotEmpty()
    }

    // Wait until the input fields are displayed
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("expireDateTextField").fetchSemanticsNodes().isNotEmpty()
    }

    // Click the cancel button
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    // Verify that the BottomSheet is dismissed and scanning resumes
    composeTestRule.onNodeWithTag("expireDateTextField").assertDoesNotExist()
    composeTestRule.onNodeWithTag("openDateTextField").assertDoesNotExist()
    composeTestRule.onNodeWithTag("buyDateTextField").assertDoesNotExist()
  }

  @Test
  fun submittingFormWithInvalidDateShowsError() {
    // Set up the fake repository to return a sample food item
    val sampleFoodFacts =
        FoodFacts(
            name = "Sample Food",
            barcode = "1234567890",
            quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
            category = FoodCategory.OTHER)
    fakeRepository.foodFactsList = listOf(sampleFoodFacts)

    composeTestRule.setContent {
      BarcodeScannerScreen(
          navigationActions = navigationActions,
          cameraViewModel = barcodeScannerViewModel,
          foodFactsViewModel = foodFactsViewModel,
          householdViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Simulate scanning a barcode
    composeTestRule.activity.runOnUiThread { foodFactsViewModel.searchByBarcode(1234567890L) }

    // Wait until the bottom sheet is displayed with food details
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("Sample Food").fetchSemanticsNodes().isNotEmpty()
    }

    // Wait until the input fields are displayed
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("expireDateTextField").fetchSemanticsNodes().isNotEmpty()
    }

    // Fill in the form with an invalid date
    composeTestRule.onNodeWithTag("expireDateTextField").performTextInput("32122023") // Invalid day
    composeTestRule.onNodeWithTag("openDateTextField").performTextInput("01012023")
    composeTestRule.onNodeWithTag("buyDateTextField").performTextClearance()
    composeTestRule.onNodeWithTag("buyDateTextField").performTextInput("15012023")

    // Click submit
    composeTestRule.onNodeWithTag("submitButton").performClick()

    // Check that an error message is displayed for expire date
    composeTestRule.onNodeWithText("Invalid date").assertIsDisplayed()
  }

  @Test
  fun dateFieldAddsSlashesAutomatically() {
    // Set up the fake repository to return a sample food item
    val sampleFoodFacts =
        FoodFacts(
            name = "Sample Food",
            barcode = "1234567890",
            quantity = Quantity(amount = 1.0, unit = FoodUnit.COUNT),
            category = FoodCategory.OTHER)
    fakeRepository.foodFactsList = listOf(sampleFoodFacts)

    composeTestRule.setContent {
      BarcodeScannerScreen(
          navigationActions = navigationActions,
          cameraViewModel = barcodeScannerViewModel,
          foodFactsViewModel = foodFactsViewModel,
          householdViewModel = householdViewModel,
          foodItemViewModel = foodItemViewModel)
    }

    // Simulate scanning a barcode
    composeTestRule.activity.runOnUiThread { foodFactsViewModel.searchByBarcode(1234567890L) }

    // Wait until the bottom sheet is displayed with food details
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("Sample Food").fetchSemanticsNodes().isNotEmpty()
    }

    // Wait until the input fields are displayed
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithTag("expireDateTextField").fetchSemanticsNodes().isNotEmpty()
    }

    // Input digits into the expire date field
    composeTestRule.onNodeWithTag("expireDateTextField").performTextInput("31122023")

    // Check that the EditableText is '31/12/2023'
    composeTestRule
        .onNodeWithTag("expireDateTextField")
        .assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText, AnnotatedString("31/12/2023")))
  }

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
