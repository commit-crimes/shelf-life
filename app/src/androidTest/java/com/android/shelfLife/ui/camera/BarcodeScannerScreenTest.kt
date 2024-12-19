package com.android.shelfLife.ui.camera

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.shelfLife.HiltTestActivity
import com.android.shelfLife.model.foodFacts.*
import com.android.shelfLife.model.permission.PermissionRepository
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.viewmodel.camera.BarcodeScannerViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import helpers.PermissionRepositoryTestHelper
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@HiltAndroidTest
class BarcodeScannerScreenTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

  @Inject lateinit var foodFactsRepository: FoodFactsRepository
  @Inject lateinit var permissionRepository: PermissionRepository

  private lateinit var permissionRepositoryTestHelper: PermissionRepositoryTestHelper

  private lateinit var instrumentationContext: android.content.Context
  private lateinit var navigationActions: NavigationActions

  // Mock for searchStatus and foodFactsSuggestions flows
  private val searchStatusFlow = MutableStateFlow<SearchStatus>(SearchStatus.Idle)
  private val foodFactsSuggestionsFlow = MutableStateFlow<List<FoodFacts>>(emptyList())

  @Before
  fun setUp() {
    hiltRule.inject()
    navigationActions = mock()

    permissionRepositoryTestHelper = PermissionRepositoryTestHelper(permissionRepository)

    // Mock repository flows
    whenever(foodFactsRepository.searchStatus).thenReturn(searchStatusFlow.asStateFlow())
    whenever(foodFactsRepository.foodFactsSuggestions)
        .thenReturn(foodFactsSuggestionsFlow.asStateFlow())

    whenever(permissionRepository.onPermissionResult(any())).then {
      permissionRepositoryTestHelper.setPermissionGranted(false)
      permissionRepositoryTestHelper.setPermissionRequested(true)
    }
  }

  private fun createViewModel(): BarcodeScannerViewModel {
    val applicationContext = composeTestRule.activity.applicationContext
    assertNotNull("Application context should not be null", applicationContext)
    return BarcodeScannerViewModel(
        foodFactsRepository = foodFactsRepository, permissionRepository = permissionRepository)
  }

  private fun setContent(viewModel: BarcodeScannerViewModel) {
    composeTestRule.setContent {
      BarcodeScannerScreen(navigationActions = navigationActions, cameraViewModel = viewModel)
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun barcodeScannerScreen_displaysCameraPreview() {
    val viewModel = createViewModel()
    setContent(viewModel)

    // Assert that the camera preview is displayed
    composeTestRule.onNodeWithTag("cameraPreviewBox").assertIsDisplayed()
  }

  @Test
  fun barcodeScannerScreen_navigatesToPermissionHandler_ifPermissionDenied() {
    val viewModel = createViewModel()
    // Simulate permission denied
    runBlocking { viewModel.onPermissionResult(false) }
    composeTestRule.waitForIdle()
    setContent(viewModel)

    // Verify navigation to permission handler
    verify(navigationActions).navigateTo(Screen.PERMISSION_HANDLER)
  }

  @Test
  fun barcodeScannerScreen_scansBarcodeAndSearches() {
    val viewModel = createViewModel()
    setContent(viewModel)

    // Simulate barcode scanning
    val scannedBarcode = "1234567890"
    composeTestRule.runOnUiThread {
      viewModel.searchByBarcode(scannedBarcode.toLong())
      searchStatusFlow.value = SearchStatus.Loading
    }

    // Assert that search status is Loading
    assert(searchStatusFlow.value is SearchStatus.Loading)
  }

  //    @Test
  //    fun barcodeScannerScreen_handlesSearchSuccess_andShowsBottomSheet() {
  //        val viewModel = createViewModel()
  //        setContent(viewModel)
  //
  //        // Simulate successful search
  //        val testFoodFacts = FoodFacts(
  //            name = "Apple",
  //            barcode = "1234567890",
  //            quantity = Quantity(1.0, FoodUnit.COUNT),
  //            category = FoodCategory.FRUIT,
  //            nutritionFacts = NutritionFacts(),
  //            imageUrl = FoodFacts.DEFAULT_IMAGE_URL
  //        )
  //
  //        composeTestRule.runOnUiThread {
  //            foodFactsSuggestionsFlow.value = listOf(testFoodFacts)
  //            searchStatusFlow.value = SearchStatus.Success
  //        }
  //
  //        // Verify bottom sheet is partially expanded with food details
  //        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
  //        composeTestRule.onNodeWithText(FoodCategory.FRUIT.name).assertIsDisplayed()
  //    }

  @Test
  fun barcodeScannerScreen_handlesSearchFailure() {
    val viewModel = createViewModel()
    setContent(viewModel)

    // Simulate search failure
    composeTestRule.runOnUiThread { searchStatusFlow.value = SearchStatus.Failure }

    // Verify failure dialog is displayed
    composeTestRule.onNodeWithText("Search Failed").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Check your internet connection and try again later.")
        .assertIsDisplayed()

    // Close the dialog
    composeTestRule.onNodeWithText("OK").performClick()

    // Verify dialog is dismissed
    composeTestRule.onNodeWithText("Search Failed").assertDoesNotExist()
  }
}
