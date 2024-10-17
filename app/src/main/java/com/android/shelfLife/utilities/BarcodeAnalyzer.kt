package com.android.shelfLife.utilities

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit,
    private val roiRectF: RectF,
    private val shouldScan: () -> Boolean // Accepts a lambda to control scanning
) : ImageAnalysis.Analyzer {

  @OptIn(ExperimentalGetImage::class)
  override fun analyze(imageProxy: ImageProxy) {
    if (!shouldScan()) {
      imageProxy.close()
      return
    }

    val mediaImage = imageProxy.image
    if (mediaImage != null) {
      val rotationDegrees = imageProxy.imageInfo.rotationDegrees
      val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

      val scanner = BarcodeScanning.getClient()
      scanner
          .process(inputImage)
          .addOnSuccessListener { barcodes ->
            val imageWidth = inputImage.width
            val imageHeight = inputImage.height

            // Map ROI to image coordinates
            val roiRect =
                Rect(
                    (roiRectF.left * imageWidth).toInt(),
                    (roiRectF.top * imageHeight).toInt(),
                    (roiRectF.right * imageWidth).toInt(),
                    (roiRectF.bottom * imageHeight).toInt())

            // Find the first barcode within the ROI
            val targetBarcode =
                barcodes.firstOrNull { barcode ->
                  val boundingBox = barcode.boundingBox
                  boundingBox != null && Rect.intersects(boundingBox, roiRect)
                }

            targetBarcode?.rawValue?.let { barcodeValue -> onBarcodeScanned(barcodeValue) }
          }
          .addOnFailureListener { e -> Log.e("BarcodeAnalyzer", "Barcode scanning failed", e) }
          .addOnCompleteListener { imageProxy.close() }
    } else {
      imageProxy.close()
    }
  }
}
