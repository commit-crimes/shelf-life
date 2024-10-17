package com.android.shelfLife.ui.camera

import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStatus
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.formatDateToTimestamp
import com.android.shelfLife.ui.utils.formatTimestampToDate
import com.android.shelfLife.utilities.BarcodeAnalyzer
import com.google.firebase.Timestamp

/**
 * Composable function for the barcode scanner screen.
 *
 * @param navigationActions The navigation actions to be used in the screen
 * @param viewModel The ViewModel for the barcode scanner
 */
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

  // Observe lifecycle to detect when the app resumes
  val lifecycleOwner = LocalLifecycleOwner.current

  // Use a MutableState to control scanning
  val isScanningState = remember { mutableStateOf(true) }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        cameraViewModel.checkCameraPermission()
        isScanningState.value = true
      } else if (event == Lifecycle.Event.ON_PAUSE) {
        isScanningState.value = false
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(permissionGranted) {
    if (!permissionGranted) {
      navigationActions.navigateTo(Screen.PERMISSION_HANDLER)
    }
  }

  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { selected -> navigationActions.navigateTo(selected) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.SCANNER)
      }) { paddingValues ->
        if (permissionGranted) {
          Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // State variables
            val barcodeScanned = remember { mutableStateOf<String?>(null) }
            val foodScanned = remember { mutableStateOf(false) }
            val foodFacts = remember { mutableStateOf<FoodFacts?>(null) }
            val isScanning by isScanningState
            val searchInProgress = remember { mutableStateOf(false) }

            // ROI calculation (same as before)
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
                  Log.d("BarcodeScanner", "Scanned barcode: $scannedBarcode")
                  beep()
                  Toast.makeText(context, "Scanned barcode: $scannedBarcode", Toast.LENGTH_SHORT)
                      .show()
                  barcodeScanned.value = scannedBarcode
                  isScanningState.value = false
                  searchInProgress.value = true
                },
                onPreviewViewCreated = { previewView -> },
                roiRect = roiRectF.value ?: RectF(0f, 0f, 1f, 1f),
                shouldScan = { isScanning })

            // Scanner Overlay on top
            ScannerOverlay()

            // Start the search when searchInProgress is true
            val currentBarcode = barcodeScanned.value
            if (searchInProgress.value && currentBarcode != null) {
              LaunchedEffect(currentBarcode) {
                foodFactsViewModel.searchByBarcode(currentBarcode.toLong())
              }
            }

            // Observe searchStatus
            val searchStatus by foodFactsViewModel.searchStatus.collectAsState()
            LaunchedEffect(searchStatus) {
              when (searchStatus) {
                is SearchStatus.Success -> {
                  val suggestions = foodFactsViewModel.foodFactsSuggestions.value
                  if (suggestions.isNotEmpty()) {
                    foodFacts.value = suggestions[0]
                    foodScanned.value = true
                  } else {
                    Toast.makeText(context, "Food Not Found", Toast.LENGTH_SHORT).show()
                    navigationActions.navigateTo(Screen.ADD_FOOD)
                  }
                  // Reset states
                  barcodeScanned.value = null
                  searchInProgress.value = false
                  foodFactsViewModel.resetSearchStatus()
                }
                is SearchStatus.Failure -> {
                  Toast.makeText(context, "Search failed", Toast.LENGTH_SHORT).show()
                  barcodeScanned.value = null
                  searchInProgress.value = false
                  foodFactsViewModel.resetSearchStatus()
                }
                else -> {
                  // Do nothing for Idle or Loading
                }
              }
            }

            if (foodScanned.value) {
              ScannedItemFoodScreen(
                  householdViewModel,
                  foodFacts.value!!,
                  foodItemViewModel,
                  onFinish = {
                    foodScanned.value = false
                    isScanningState.value = true
                  })
            }
          }
        }
      }
}

// Function to calculate ROI rectangle based on screen dimensions
fun calculateRoiRectF(screenWidth: Float, screenHeight: Float): RectF {
  val rectWidth = screenWidth * 0.8f
  val rectHeight = screenHeight * 0.2f
  val left = (screenWidth - rectWidth) / 2f
  val top = (screenHeight - rectHeight) / 2f

  return RectF(
      left / screenWidth,
      top / screenHeight,
      (left + rectWidth) / screenWidth,
      (top + rectHeight) / screenHeight)
}

@Composable
fun ScannerOverlay() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val rectWidth = canvasWidth * 0.8f
    val rectHeight = canvasHeight * 0.2f
    val left = (canvasWidth - rectWidth) / 2f
    val top = (canvasHeight - rectHeight) / 2f

    // Semi-transparent background
    drawRect(
        color = Color(0x40ffffff), // low opacity white
        size = size)

    // Transparent rectangle in the middle (ROI)
    drawRect(
        color = Color.Transparent,
        topLeft = Offset(left, top),
        size = Size(rectWidth, rectHeight),
        blendMode = BlendMode.Clear)

    // Draw border around the rectangle
    drawRect(
        color = Color.White,
        topLeft = Offset(left, top),
        size = Size(rectWidth, rectHeight),
        style = Stroke(width = 4.dp.toPx()))
  }
}

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit,
    onPreviewViewCreated: (PreviewView) -> Unit,
    roiRect: RectF,
    shouldScan: () -> Boolean
) {
  AndroidView(
      factory = { context ->
        val previewView = PreviewView(context)
        onPreviewViewCreated(previewView)
        startCamera(context, previewView, onBarcodeScanned, roiRect, shouldScan)
        previewView
      },
      modifier = modifier.fillMaxSize())
}

fun startCamera(
    context: Context,
    previewView: PreviewView,
    onBarcodeScanned: (String) -> Unit,
    roiRect: RectF,
    shouldScan: () -> Boolean
) {
  val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

  cameraProviderFuture.addListener(
      {
        val cameraProvider = cameraProviderFuture.get()

        val preview =
            Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

        // Set up Barcode Analyzer
        val imageAnalyzer =
            ImageAnalysis.Builder().build().also {
              it.setAnalyzer(
                  ContextCompat.getMainExecutor(context),
                  BarcodeAnalyzer(onBarcodeScanned, roiRect, shouldScan))
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
          cameraProvider.unbindAll()
          cameraProvider.bindToLifecycle(
              context as LifecycleOwner, cameraSelector, preview, imageAnalyzer)
        } catch (exc: Exception) {
          Log.e("CameraX", "Use case binding failed", exc)
        }
      },
      ContextCompat.getMainExecutor(context))
}

@Composable
fun PermissionDeniedScreen(navigationActions: NavigationActions) {
  val context = LocalContext.current
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { selected -> navigationActions.navigateTo(selected) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.SCANNER)
      }) { paddingVals ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingVals),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "Camera permission is required to scan barcodes.")
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                  onClick = {
                    // Open app settings
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                          data = Uri.fromParts("package", context.packageName, null)
                        }
                    context.startActivity(intent)
                  }) {
                    Text(text = "Open Settings")
                  }
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannedItemFoodScreen(
    houseHoldViewModel: HouseholdViewModel,
    foodFacts: FoodFacts,
    foodItemViewModel: ListFoodItemsViewModel,
    onFinish: () -> Unit // Callback to reset scanning state
) {

  var location by remember { mutableStateOf(FoodStorageLocation.PANTRY) }
  var expireDate by remember { mutableStateOf("") }
  var openDate by remember { mutableStateOf("") }
  var buyDate by remember { mutableStateOf(formatTimestampToDate(Timestamp.now())) }

  var locationExpanded by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
            title = { Text("Add Food Item") },
            navigationIcon = {
              // Back button to return to the previous screen
              IconButton(onClick = { onFinish() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back Icon")
              }
            })
      },
  ) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
          Spacer(modifier = Modifier.height(16.dp))

          ExposedDropdownMenuBox(
              expanded = locationExpanded,
              onExpandedChange = { locationExpanded = !locationExpanded }) {
                OutlinedTextField(
                    value = location.name.lowercase(),
                    onValueChange = {},
                    label = { Text("Location") },
                    readOnly = true,
                    trailingIcon = {
                      ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(
                    expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                      FoodStorageLocation.values().forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.name) },
                            onClick = {
                              location = selectionOption
                              locationExpanded = false
                            })
                      }
                    }
              }

          // For dates a future improvement could be having a calendar interface rather than manual
          // input
          OutlinedTextField(
              value = expireDate,
              onValueChange = { expireDate = it },
              label = { Text("Expire Date") },
              placeholder = { Text("dd/mm/yyyy") },
              modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          )

          OutlinedTextField(
              value = openDate,
              onValueChange = { openDate = it },
              label = { Text("Open Date") },
              placeholder = { Text("dd/mm/yyyy") },
              modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          )

          OutlinedTextField(
              value = buyDate,
              onValueChange = { buyDate = it },
              label = { Text("Buy Date") },
              placeholder = { Text("dd/mm/yyyy") },
              modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
          )

          Button(
              onClick = {
                val newFoodItem =
                    FoodItem(
                        uid = foodItemViewModel.getUID(),
                        foodFacts = foodFacts,
                        location = location,
                        expiryDate = formatDateToTimestamp(expireDate),
                        openDate = formatDateToTimestamp(openDate),
                        buyDate = formatDateToTimestamp(buyDate),
                        // Logic to determine status depending on the dates given
                        status = FoodStatus.CLOSED)
                houseHoldViewModel.addFoodItem(newFoodItem)
                onFinish() // Call the callback to remove the screen and resume scanning
              },
              modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text(text = "Submit", fontSize = 18.sp)
              }
          Spacer(modifier = Modifier.height(16.dp))
          Button(
              onClick = {
                // Reset the scanned food item
                onFinish() // Call the callback to remove the screen and resume scanning
              },
              modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text(text = "Cancel", fontSize = 18.sp)
              }
        }
  }
}

// Function to play a beep sound
fun beep() {
  val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
  toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
}
