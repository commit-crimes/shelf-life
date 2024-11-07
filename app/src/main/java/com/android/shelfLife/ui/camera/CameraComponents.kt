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
      (top + rectHeight) / screenHeight)
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
      modifier = modifier.fillMaxSize().testTag("cameraPreviewView"))
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
