package com.android.shelfLife.ui.camera

import android.content.Intent
import android.graphics.RectF
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.OnLifecycleEvent
import kotlinx.coroutines.launch

/**
 * Composable function for the Barcode Scanner Screen.
 *
 * @param navigationActions Actions for navigation.
 * @param cameraViewModel ViewModel for the camera.
 * @param foodFactsViewModel ViewModel for food facts.
 * @param householdViewModel ViewModel for household.
 * @param foodItemViewModel ViewModel for food items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    navigationActions: NavigationActions,
    cameraViewModel: BarcodeScannerViewModel = viewModel(),
    foodFactsViewModel: FoodFactsViewModel,
    householdViewModel: HouseholdViewModel,
    foodItemViewModel: ListFoodItemsViewModel
) {
  val context = LocalContext.current
  val permissionGranted = cameraViewModel.permissionGranted

  // State variables
  val isScanningState = remember { mutableStateOf(true) }
  val foodScanned = remember { mutableStateOf(false) }
  val barcodeScanned = remember { mutableStateOf<String?>(null) }
  val foodFacts = remember { mutableStateOf<FoodFacts?>(null) }
  val searchInProgress = remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()

  // Bottom sheet scaffold state with initial state as Hidden
  val sheetScaffoldState =
      rememberBottomSheetScaffoldState(
          bottomSheetState =
              rememberStandardBottomSheetState(
                  initialValue = SheetValue.Hidden, skipHiddenState = false))

  OnLifecycleEvent(
      onResume = {
        cameraViewModel.checkCameraPermission()
        isScanningState.value = true
      },
      onPause = {
        isScanningState.value = false
        coroutineScope.launch { sheetScaffoldState.bottomSheetState.hide() }
      })

  LaunchedEffect(permissionGranted) {
    if (!permissionGranted) {
      navigationActions.navigateTo(Screen.PERMISSION_HANDLER)
    }
  }

  // Listen to BottomSheetScaffold state changes
  LaunchedEffect(sheetScaffoldState.bottomSheetState) {
    snapshotFlow { sheetScaffoldState.bottomSheetState.currentValue }
        .collect { sheetState ->
          if (sheetState == SheetValue.Hidden || sheetState == SheetValue.PartiallyExpanded) {
            // Resume scanning when the sheet is hidden
            isScanningState.value = true
            foodScanned.value = false
            barcodeScanned.value = null
            foodFacts.value = null
          }
        }
  }

  // Parent Scaffold to host the Bottom Navigation Bar
  Scaffold(
      modifier = Modifier.testTag("barcodeScannerScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { selected -> navigationActions.navigateTo(selected) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.SCANNER)
      }) { innerPadding ->
        // BottomSheetScaffold inside the parent Scaffold
        BottomSheetScaffold(
            scaffoldState = sheetScaffoldState,
            sheetContent = {
              val foodFactsValue = foodFacts.value
              if (foodScanned.value && foodFactsValue != null) {
                FoodInputContent(
                    foodFacts = foodFactsValue,
                    onSubmit = { newFoodItem ->
                      // Reset states
                      foodScanned.value = false
                      isScanningState.value = true
                      coroutineScope.launch { sheetScaffoldState.bottomSheetState.hide() }
                      householdViewModel.addFoodItem(newFoodItem)
                      Log.d("BarcodeScanner", "Food item added")
                    },
                    onCancel = {
                      // Reset states
                      foodScanned.value = false
                      isScanningState.value = true
                      coroutineScope.launch { sheetScaffoldState.bottomSheetState.hide() }
                      Log.d("BarcodeScanner", "Cancelled")
                    },
                    foodItemViewModel = foodItemViewModel,
                )
              }
              Spacer(modifier = Modifier.height(100.dp))
            },
            modifier =
                Modifier.padding(innerPadding) // Apply the inner padding from the parent Scaffold
            ) {
              Box(
                  modifier =
                      Modifier.fillMaxSize()
                          .clickable {
                            // Reset states
                            foodScanned.value = false
                            isScanningState.value = true
                            coroutineScope.launch { sheetScaffoldState.bottomSheetState.hide() }
                          }
                          .testTag("cameraPreviewBox")) {
                    // ROI calculation
                    val roiRectF = remember { mutableStateOf<RectF?>(null) }
                    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
                    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels

                    val calculatedRoiRectF =
                        calculateRoiRectF(screenWidth.toFloat(), screenHeight.toFloat())
                    roiRectF.value = calculatedRoiRectF

                    // Camera Preview
                    CameraPreviewView(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeScanned = { scannedBarcode ->
                          Log.i("BarcodeScanner", "Scanned barcode: $scannedBarcode")
                          beep()
                          barcodeScanned.value = scannedBarcode
                          isScanningState.value = false
                          searchInProgress.value = true
                        },
                        onPreviewViewCreated = {},
                        roiRect = roiRectF.value ?: RectF(0f, 0f, 1f, 1f),
                        shouldScan = { isScanningState.value && !foodScanned.value })

                    // Scanner Overlay on top
                    ScannerOverlay()
                  }
            }
      }

  // Handle barcode scanning and search
  val currentBarcode = barcodeScanned.value
  if (searchInProgress.value && currentBarcode != null) {
    LaunchedEffect(currentBarcode) { foodFactsViewModel.searchByBarcode(currentBarcode.toLong()) }
  }

  // Observe searchStatus and update foodScanned.value
  val searchStatus by foodFactsViewModel.searchStatus.collectAsState()
  LaunchedEffect(searchStatus) {
    when (searchStatus) {
      is SearchStatus.Success -> {
        val suggestions = foodFactsViewModel.foodFactsSuggestions.value
        if (suggestions.isNotEmpty()) {
          foodFacts.value = suggestions[0]
          foodScanned.value = true
          coroutineScope.launch { sheetScaffoldState.bottomSheetState.expand() }
        } else {
          Toast.makeText(context, "Food Not Found in Database", Toast.LENGTH_SHORT).show()
          navigationActions.navigateTo(Screen.ADD_FOOD)
        }
        // Reset states
        barcodeScanned.value = null
        searchInProgress.value = false
        foodFactsViewModel.resetSearchStatus()
      }
      is SearchStatus.Failure -> {
        Toast.makeText(context, "Search failed, check internet connection", Toast.LENGTH_SHORT)
            .show()
        barcodeScanned.value = null
        searchInProgress.value = false
        foodFactsViewModel.resetSearchStatus()
      }
      else -> {
        // Do nothing for Idle or Loading
      }
    }
  }
}

/**
 * Composable function to display the permission denied screen.
 *
 * @param navigationActions Actions for navigation.
 */
@Composable
fun PermissionDeniedScreen(navigationActions: NavigationActions) {
  val context = LocalContext.current
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { selected -> navigationActions.navigateTo(selected) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.SCANNER)
      },
      modifier = Modifier.semantics { testTag = "permissionDeniedScreen" }) { paddingVals ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(paddingVals).semantics {
                  testTag = "permissionDeniedColumn"
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = "Camera permission is required to scan barcodes.",
                  modifier = Modifier.semantics { testTag = "permissionDeniedMessage" })
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                  onClick = {
                    // Open app settings
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                          data = Uri.fromParts("package", context.packageName, null)
                        }
                    context.startActivity(intent)
                  },
                  modifier = Modifier.semantics { testTag = "openSettingsButton" }) {
                    Text(text = "Open Settings")
                  }
            }
      }
}

/** Function to play a beep sound. */
fun beep() {
  val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
  toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
}
