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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodStorageLocation
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.OnLifecycleEvent
import com.android.shelfLife.utilities.BarcodeAnalyzer
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function for the Barcode Scanner Screen.
 *
 * @param navigationActions Actions for navigation.
 * @param cameraViewModel ViewModel for the camera.
 * @param foodFactsViewModel ViewModel for food facts.
 * @param householdViewModel ViewModel for household.
 * @param foodItemViewModel ViewModel for food items.
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

    // Use a MutableState to control scanning
    val isScanningState = remember { mutableStateOf(true) }

    OnLifecycleEvent(
        onResume = {
            cameraViewModel.checkCameraPermission()
            isScanningState.value = true
        },
        onPause = {
            isScanningState.value = false
        }
    )

    LaunchedEffect(permissionGranted) {
        if (!permissionGranted) {
            navigationActions.navigateTo(Screen.PERMISSION_HANDLER)
        }
    }

    Scaffold(
        modifier = Modifier.testTag("barcodeScannerScreen"),
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { selected -> navigationActions.navigateTo(selected) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.SCANNER
            )
        },
        content = { contentPadding ->
            if (permissionGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .testTag("cameraPreviewBox")
                ) {
                    // State variables
                    val barcodeScanned = remember { mutableStateOf<String?>(null) }
                    val foodScanned = remember { mutableStateOf(false) }
                    val foodFacts = remember { mutableStateOf<FoodFacts?>(null) }
                    val isScanning by isScanningState
                    val searchInProgress = remember { mutableStateOf(false) }

                    // ROI calculation
                    val roiRectF = remember { mutableStateOf<RectF?>(null) }
                    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
                    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels

                    val calculatedRoiRectF =
                        calculateRoiRectF(screenWidth.toFloat(), screenHeight.toFloat())
                    roiRectF.value = calculatedRoiRectF

                    // Camera Preview
                    if (!foodScanned.value) {
                        CameraPreviewView(
                            modifier = Modifier.fillMaxSize(),
                            onBarcodeScanned = { scannedBarcode ->
                                Log.d("BarcodeScanner", "Scanned barcode: $scannedBarcode")
                                beep()
                                Toast.makeText(
                                    context, "Scanned barcode: $scannedBarcode", Toast.LENGTH_SHORT
                                ).show()
                                barcodeScanned.value = scannedBarcode
                                isScanningState.value = false
                                searchInProgress.value = true
                            },
                            onPreviewViewCreated = {},
                            roiRect = roiRectF.value ?: RectF(0f, 0f, 1f, 1f),
                            shouldScan = { isScanning }
                        )

                        // Scanner Overlay on top
                        ScannerOverlay()
                    }

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

                    if (foodScanned.value && foodFacts.value != null) {
                        ScannedItemFoodScreen(
                            houseHoldViewModel = householdViewModel,
                            foodFacts = foodFacts.value!!,
                            foodItemViewModel = foodItemViewModel,
                            onFinish = {
                                foodScanned.value = false
                                isScanningState.value = true
                            }
                        )
                    }
                }
            }
        }
    )
}

/**
 * Function to calculate the Region of Interest (ROI) rectangle based on screen dimensions.
 *
 * @param screenWidth Width of the screen.
 * @param screenHeight Height of the screen.
 * @return RectF representing the ROI.
 */
fun calculateRoiRectF(screenWidth: Float, screenHeight: Float): RectF {
    val rectWidth = screenWidth * 0.8f
    val rectHeight = screenHeight * 0.2f
    val left = (screenWidth - rectWidth) / 2f
    val top = (screenHeight - rectHeight) / 2f

    return RectF(
        left / screenWidth,
        top / screenHeight,
        (left + rectWidth) / screenWidth,
        (top + rectHeight) / screenHeight
    )
}

/** Composable function to display the scanner overlay. */
@Composable
fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize().testTag("scannerOverlay")) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val rectWidth = canvasWidth * 0.8f
        val rectHeight = canvasHeight * 0.2f
        val left = (canvasWidth - rectWidth) / 2f
        val top = (canvasHeight - rectHeight) / 2f

        // Semi-transparent background
        drawRect(
            color = Color(0x40ffffff), // low opacity white
            size = size
        )

        // Transparent rectangle in the middle (ROI)
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            blendMode = BlendMode.Clear
        )

        // Draw border around the rectangle
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

/**
 * Composable function to display the camera preview.
 *
 * @param modifier Modifier for the composable.
 * @param onBarcodeScanned Callback when a barcode is scanned.
 * @param onPreviewViewCreated Callback when the preview view is created.
 * @param roiRect Region of Interest rectangle.
 * @param shouldScan Lambda to determine if scanning should occur.
 */
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
        modifier = modifier.fillMaxSize().testTag("cameraPreviewView")
    )
}

/**
 * Function to start the camera and set up the barcode analyzer.
 *
 * @param context Context of the application.
 * @param previewView Preview view for the camera.
 * @param onBarcodeScanned Callback when a barcode is scanned.
 * @param roiRect Region of Interest rectangle.
 * @param shouldScan Lambda to determine if scanning should occur.
 */
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
                        BarcodeAnalyzer(onBarcodeScanned, roiRect, shouldScan)
                    )
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as LifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        },
        ContextCompat.getMainExecutor(context)
    )
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
                selectedItem = Route.SCANNER
            )
        },
        modifier = Modifier.semantics { testTag = "permissionDeniedScreen" }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .semantics { testTag = "permissionDeniedColumn" },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Camera permission is required to scan barcodes.",
                modifier = Modifier.semantics { testTag = "permissionDeniedMessage" }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.semantics { testTag = "openSettingsButton" }
            ) {
                Text(text = "Open Settings")
            }
        }
    }
}

/**
 * Composable function to display the scanned item food screen.
 *
 * @param houseHoldViewModel ViewModel for household.
 * @param foodFacts Food facts data.
 * @param foodItemViewModel ViewModel for food items.
 * @param onFinish Callback to reset scanning state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannedItemFoodScreen(
    houseHoldViewModel: HouseholdViewModel,
    foodFacts: FoodFacts,
    foodItemViewModel: ListFoodItemsViewModel,
    onFinish: () -> Unit
) {

    val context = LocalContext.current
    var location by remember { mutableStateOf(FoodStorageLocation.PANTRY) }
    var expireDate by remember { mutableStateOf("") }
    var openDate by remember { mutableStateOf("") }
    var buyDate by remember { mutableStateOf(formatTimestampToDate(Timestamp.now())) }

    var expireDateError by remember { mutableStateOf<String?>(null) }
    var openDateError by remember { mutableStateOf<String?>(null) }
    var buyDateError by remember { mutableStateOf<String?>(null) }

    var locationExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("scannedItemFoodScreen"),
        topBar = {
            TopAppBar(
                title = { Text("Add Food Item") },
                navigationIcon = {
                    IconButton(
                        onClick = { onFinish() },
                        modifier = Modifier.testTag("backButton")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back Icon"
                        )
                    }
                }
            )
        },
        content = { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Location Dropdown (unchanged)
                ExposedDropdownMenuBox(
                    expanded = locationExpanded,
                    onExpandedChange = { locationExpanded = !locationExpanded },
                    modifier = Modifier.testTag("locationDropdown")
                ) {
                    OutlinedTextField(
                        value = location.name.lowercase(),
                        onValueChange = {},
                        label = { Text("Location") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("locationTextField")
                    )
                    ExposedDropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false },
                        modifier = Modifier.testTag("locationMenu")
                    ) {
                        FoodStorageLocation.entries.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    location = selectionOption
                                    locationExpanded = false
                                },
                                modifier = Modifier.testTag("locationOption_${selectionOption.name}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expire Date Field with Error Handling and Input Masking
                OutlinedTextField(
                    value = expireDate,
                    onValueChange = { newValue ->
                        expireDate = newValue.filter { it.isDigit() }
                        expireDateError = getDateErrorMessage(expireDate)
                    },
                    label = { Text("Expire Date") },
                    placeholder = { Text("dd/MM/yyyy") },
                    isError = expireDateError != null,
                    visualTransformation = DateVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expireDateTextField")
                )
                if (expireDateError != null) {
                    Text(
                        text = expireDateError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Open Date Field with Error Handling and Input Masking
                OutlinedTextField(
                    value = openDate,
                    onValueChange = { newValue ->
                        openDate = newValue.filter { it.isDigit() }
                        openDateError = getDateErrorMessage(openDate)

                        // Additional validation: openDate should not be before buyDate
                        if (openDateError == null && buyDateError == null &&
                            openDate.length == 8 && buyDate.length == 8
                        ) {
                            if (!isDateAfterOrEqual(openDate, buyDate)) {
                                openDateError = "Open Date cannot be before Buy Date"
                            } else {
                                openDateError = null
                            }
                        }
                    },
                    label = { Text("Open Date") },
                    placeholder = { Text("dd/MM/yyyy") },
                    isError = openDateError != null,
                    visualTransformation = DateVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("openDateTextField")
                )
                if (openDateError != null) {
                    Text(
                        text = openDateError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buy Date Field with Error Handling and Input Masking
                OutlinedTextField(
                    value = buyDate,
                    onValueChange = { newValue ->
                        buyDate = newValue.filter { it.isDigit() }
                        buyDateError = getDateErrorMessage(buyDate)

                        // Re-validate openDate against buyDate
                        if (openDateError == null && buyDateError == null &&
                            openDate.length == 8 && buyDate.length == 8
                        ) {
                            if (!isDateAfterOrEqual(openDate, buyDate)) {
                                openDateError = "Open Date cannot be before Buy Date"
                            } else {
                                openDateError = null
                            }
                        }
                    },
                    label = { Text("Buy Date") },
                    placeholder = { Text("dd/MM/yyyy") },
                    isError = buyDateError != null,
                    visualTransformation = DateVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("buyDateTextField")
                )
                if (buyDateError != null) {
                    Text(
                        text = buyDateError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        // Validate all inputs before proceeding
                        val isExpireDateValid = expireDateError == null && expireDate.isNotEmpty()
                        val isOpenDateValid = openDateError == null && (openDate.isEmpty() || openDate.length == 8)
                        val isBuyDateValid = buyDateError == null && buyDate.isNotEmpty()

                        val expiryTimestamp = formatDateToTimestamp(expireDate)
                        val openTimestamp = if (openDate.isNotEmpty()) formatDateToTimestamp(openDate) else null
                        val buyTimestamp = formatDateToTimestamp(buyDate)

                        if (isExpireDateValid && isOpenDateValid && isBuyDateValid &&
                            expiryTimestamp != null && buyTimestamp != null
                        ) {
                            val newFoodItem = FoodItem(
                                uid = foodItemViewModel.getUID(),
                                foodFacts = foodFacts,
                                location = location,
                                expiryDate = expiryTimestamp,
                                openDate = openTimestamp,
                                buyDate = buyTimestamp,
                                // Additional logic for status if needed
                            )
                            houseHoldViewModel.addFoodItem(newFoodItem)
                            onFinish()
                        } else {
                            // Handle the case where validation fails
                            Toast.makeText(
                                context,
                                "Please correct the errors before submitting.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submitButton")
                ) {
                    Text(text = "Submit", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onFinish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cancelButton")
                ) {
                    Text(text = "Cancel", fontSize = 18.sp)
                }
            }
        }
    )
}

// Custom VisualTransformation with proper OffsetMapping
class DateVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        // Remove any non-digit characters
        val digits = text.text.filter { it.isDigit() }

        // Build the formatted text with slashes
        val formattedText = buildString {
            for (i in digits.indices) {
                append(digits[i])
                if ((i == 1 || i == 3) && i != digits.lastIndex) {
                    append('/')
                }
            }
        }

        // Create an OffsetMapping for the cursor position
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = offset
                if (offset > 2) transformedOffset++
                if (offset > 4) transformedOffset++
                return transformedOffset.coerceAtMost(formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = offset
                if (offset > 2) originalOffset--
                if (offset > 5) originalOffset--
                return originalOffset.coerceAtMost(digits.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

// Helper function to get error message for date input
fun getDateErrorMessage(dateStr: String): String? {
    if (dateStr.isEmpty()) {
        return "Date cannot be empty"
    }
    if (dateStr.length != 8) {
        return "Incomplete date"
    }
    val formattedDateStr = insertSlashes(dateStr)
    return if (isValidDate(formattedDateStr)) null else "Invalid date"
}

// Function to insert slashes into the date string
fun insertSlashes(input: String): String {
    // Input is expected to be up to 8 digits
    val sb = StringBuilder()
    val digits = input.take(8) // Ensure no more than 8 digits
    for (i in digits.indices) {
        sb.append(digits[i])
        if ((i == 1 || i == 3) && i != digits.lastIndex) {
            sb.append('/')
        }
    }
    return sb.toString()
}

// Function to validate date in dd/MM/yyyy format without using exceptions
fun isValidDate(dateStr: String): Boolean {
    // Check if the dateStr matches the pattern dd/MM/yyyy
    val datePattern = Regex("""\d{2}/\d{2}/\d{4}""")
    if (!datePattern.matches(dateStr)) {
        return false
    }

    val parts = dateStr.split("/")
    val day = parts[0].toIntOrNull() ?: return false
    val month = parts[1].toIntOrNull() ?: return false
    val year = parts[2].toIntOrNull() ?: return false

    // Check if month is valid
    if (month !in 1..12) {
        return false
    }

    // Check if day is valid for the given month
    val daysInMonth = when (month) {
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }

    if (day !in 1..daysInMonth) {
        return false
    }

    // Additional checks can be added (e.g., year range)
    return true
}

// Helper function to check if a year is a leap year
fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))
}

// Function to compare two dates (returns true if date1 >= date2)
fun isDateAfterOrEqual(dateStr1: String, dateStr2: String): Boolean {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date1 = sdf.parse(insertSlashes(dateStr1)) ?: return false
    val date2 = sdf.parse(insertSlashes(dateStr2)) ?: return false
    return !date1.before(date2)
}

// Function to convert a string date to Timestamp, handling exceptions
fun formatDateToTimestamp(dateString: String): Timestamp? {
    return try {
        val formattedDateStr = insertSlashes(dateString)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = sdf.parse(formattedDateStr)
        if (date != null) Timestamp(date) else null
    } catch (e: Exception) {
        null
    }
}

// Function to format a Timestamp to a date string (stored as digits without slashes)
fun formatTimestampToDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

/** Function to play a beep sound. */
fun beep() {
    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
}
