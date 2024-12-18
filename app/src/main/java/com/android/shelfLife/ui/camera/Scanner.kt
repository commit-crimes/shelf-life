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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.model.foodFacts.FoodCategory
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodUnit
import com.android.shelfLife.model.foodFacts.NutritionFacts
import com.android.shelfLife.model.foodFacts.Quantity
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.OnLifecycleEvent
import com.android.shelfLife.viewmodel.camera.BarcodeScannerViewModel
import com.android.shelfLife.viewmodel.overview.FoodItemViewModel
import kotlinx.coroutines.launch

/**
 * Composable function for the Barcode Scanner Screen.
 *
 * @param navigationActions Actions for navigation.
 * @param cameraViewModel ViewModel for the camera.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    navigationActions: NavigationActions,
    cameraViewModel: BarcodeScannerViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val permissionGranted = cameraViewModel.permissionGranted

  // Create a Saver for FoodFacts. We'll store it as a Map<String, Any?>.
  val FoodFactsSaver =
      Saver<FoodFacts?, Map<String, Any?>>(
          save = { value: FoodFacts? ->
            if (value == null) {
              mapOf("isNull" to true)
            } else {
              mapOf(
                  "isNull" to false,
                  "name" to value.name,
                  "barcode" to value.barcode,
                  "quantity_amount" to value.quantity.amount,
                  "quantity_unit" to value.quantity.unit.name,
                  "category" to value.category.name,
                  "nutrition_energyKcal" to value.nutritionFacts.energyKcal,
                  "nutrition_fat" to value.nutritionFacts.fat,
                  "nutrition_saturatedFat" to value.nutritionFacts.saturatedFat,
                  "nutrition_carbohydrates" to value.nutritionFacts.carbohydrates,
                  "nutrition_sugars" to value.nutritionFacts.sugars,
                  "nutrition_proteins" to value.nutritionFacts.proteins,
                  "nutrition_salt" to value.nutritionFacts.salt,
                  "imageUrl" to value.imageUrl)
            }
          },
          restore = { map: Map<String, Any?> ->
            val isNull = map["isNull"] as? Boolean ?: true
            if (isNull) {
              null
            } else {
              val name = map["name"] as? String ?: return@Saver null
              val barcode = map["barcode"] as? String ?: ""
              val amount = (map["quantity_amount"] as? Double) ?: 0.0
              val unitName = map["quantity_unit"] as? String ?: FoodUnit.GRAM.name
              val unit = FoodUnit.valueOf(unitName)
              val categoryName = map["category"] as? String ?: FoodCategory.OTHER.name
              val category = FoodCategory.valueOf(categoryName)

              val energyKcal = (map["nutrition_energyKcal"] as? Int) ?: 0
              val fat = (map["nutrition_fat"] as? Double) ?: 0.0
              val saturatedFat = (map["nutrition_saturatedFat"] as? Double) ?: 0.0
              val carbohydrates = (map["nutrition_carbohydrates"] as? Double) ?: 0.0
              val sugars = (map["nutrition_sugars"] as? Double) ?: 0.0
              val proteins = (map["nutrition_proteins"] as? Double) ?: 0.0
              val salt = (map["nutrition_salt"] as? Double) ?: 0.0
              val imageUrl = map["imageUrl"] as? String ?: FoodFacts.DEFAULT_IMAGE_URL

              FoodFacts(
                  name = name,
                  barcode = barcode,
                  quantity = Quantity(amount = amount, unit = unit),
                  category = category,
                  nutritionFacts =
                      NutritionFacts(
                          energyKcal = energyKcal,
                          fat = fat,
                          saturatedFat = saturatedFat,
                          carbohydrates = carbohydrates,
                          sugars = sugars,
                          proteins = proteins,
                          salt = salt),
                  imageUrl = imageUrl)
            }
          })
  // State variables
  val isScanningState = rememberSaveable { mutableStateOf(true) }
  val foodScanned = rememberSaveable { mutableStateOf(false) }
  val barcodeScanned = rememberSaveable { mutableStateOf<String?>(null) }
  val foodFacts = rememberSaveable(stateSaver = FoodFactsSaver) { mutableStateOf<FoodFacts?>(null) }
  val searchInProgress = rememberSaveable { mutableStateOf(false) }

  val showFailureDialog = rememberSaveable { mutableStateOf(false) }

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
          if (sheetState == SheetValue.Hidden) {
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
                val foodItemViewModel = hiltViewModel<FoodItemViewModel>()
                foodItemViewModel.resetForScanner()
                FoodInputContent(
                    foodItemViewModel = foodItemViewModel,
                    foodFacts = foodFactsValue,
                    onSubmit = {
                      // Reset states
                      foodScanned.value = false
                      isScanningState.value = true
                      coroutineScope.launch { sheetScaffoldState.bottomSheetState.hide() }
                      Log.d("BarcodeScanner", "Food item added")
                    },
                    onCancel = {
                      // Reset states
                      foodScanned.value = false
                      isScanningState.value = true
                      coroutineScope.launch { sheetScaffoldState.bottomSheetState.hide() }
                      Log.d("BarcodeScanner", "Cancelled")
                    },
                    onExpandRequested = {
                      // When the partially expanded content is clicked, expand the sheet fully
                      coroutineScope.launch { sheetScaffoldState.bottomSheetState.expand() }
                    })
              }
              Spacer(modifier = Modifier.height(100.dp))
            },
            sheetPeekHeight = 240.dp,
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
  // TODO check if barcode can be converted to long before passing to searchByBarcode
  if (searchInProgress.value && currentBarcode != null) {
    LaunchedEffect(currentBarcode) { cameraViewModel.searchByBarcode(currentBarcode.toLong()) }
  }

  // Observe searchStatus and update foodScanned.value
  val searchStatus by cameraViewModel.searchStatus.collectAsState()
  LaunchedEffect(searchStatus) {
    when (searchStatus) {
      is SearchStatus.Success -> {
        val suggestions = cameraViewModel.foodFactsSuggestions.value
        if (suggestions.isNotEmpty()) {
          foodFacts.value = suggestions[0]
          foodScanned.value = true
          coroutineScope.launch { sheetScaffoldState.bottomSheetState.partialExpand() }
        } else {
          Toast.makeText(context, "Food Not Found in Database", Toast.LENGTH_SHORT).show()

          navigationActions.navigateTo(Screen.ADD_FOOD)
        }
        // Reset states
        barcodeScanned.value = null
        searchInProgress.value = false
        cameraViewModel.resetSearchStatus()
      }
      is SearchStatus.Failure -> {
        Toast.makeText(context, "Search failed, check internet connection", Toast.LENGTH_SHORT)
            .show()
        showFailureDialog.value = true
        barcodeScanned.value = null
        searchInProgress.value = false
        cameraViewModel.resetSearchStatus()
      }
      else -> {
        // Do nothing for Idle or Loading
      }
    }
  }

  if (showFailureDialog.value) {
    AlertDialog(
        onDismissRequest = {
          // User tapped outside or back button
          // Handle similarly to pressing OK or X: reset scanning
          showFailureDialog.value = false
          foodScanned.value = false
          isScanningState.value = true
        },
        title = { Text(text = "Search Failed") },
        text = { Text("Check your internet connection and try again later.") },
        confirmButton = {
          TextButton(
              onClick = {
                // OK button pressed: reset scanning and hide dialog
                showFailureDialog.value = false
                foodScanned.value = false
                isScanningState.value = true
              }) {
                Text("OK")
              }
        })
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
