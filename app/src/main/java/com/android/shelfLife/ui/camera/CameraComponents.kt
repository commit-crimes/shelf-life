package com.android.shelfLife.ui.camera

import android.content.Context
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.android.shelfLife.utilities.BarcodeAnalyzer

/**
 * Calculates the Region of Interest (ROI) rectangle for barcode scanning based on screen dimensions.
 *
 * The ROI is a rectangular area on the screen where barcode scanning is focused.
 *
 * @param screenWidth The width of the screen in pixels.
 * @param screenHeight The height of the screen in pixels.
 * @return A [RectF] object representing the ROI as normalized coordinates.
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

/**
 * Composable function that displays the scanner overlay on the camera preview.
 *
 * The overlay includes a semi-transparent background with a clear rectangular
 * Region of Interest (ROI) in the center, surrounded by a white border.
 */
@Composable
fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize().testTag("scannerOverlay")) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val rectWidth = canvasWidth * 0.8f
        val rectHeight = canvasHeight * 0.2f
        val left = (canvasWidth - rectWidth) / 2f
        val top = (canvasHeight - rectHeight) / 2f

        // Draw a semi-transparent background.
        drawRect(
            color = Color(0x40ffffff), // Low-opacity white
            size = size
        )

        // Draw a transparent rectangle (ROI) in the middle.
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            blendMode = BlendMode.Clear
        )

        // Draw a white border around the ROI.
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

/**
 * Composable function that displays the camera preview.
 *
 * This function integrates a native Android [PreviewView] into the Compose UI.
 *
 * @param modifier Modifier for the composable.
 * @param onBarcodeScanned Callback function triggered when a barcode is successfully scanned.
 * @param onPreviewViewCreated Callback function triggered when the [PreviewView] is created.
 * @param roiRect A [RectF] defining the Region of Interest for barcode scanning.
 * @param shouldScan Lambda function to determine whether barcode scanning should proceed.
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
 * Starts the camera and sets up a barcode analyzer.
 *
 * This function initializes CameraX components, binds the camera to the lifecycle,
 * and configures the barcode analyzer to scan barcodes within the specified ROI.
 *
 * @param context The application context.
 * @param previewView The [PreviewView] used to display the camera feed.
 * @param onBarcodeScanned Callback function triggered when a barcode is successfully scanned.
 * @param roiRect A [RectF] defining the Region of Interest for barcode scanning.
 * @param shouldScan Lambda function to determine whether barcode scanning should proceed.
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

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Set up the barcode analyzer.
            val imageAnalyzer = ImageAnalysis.Builder().build().also {
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