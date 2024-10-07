package com.android.shelfLife.ui.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.TopLevelDestination

@Composable
fun BarcodeScannerScreen(navigationActions: NavigationActions) {
  // Get the context in a Composable way
  val context = LocalContext.current

  // Pass the context and start the camera in the lambda
  CameraPreviewView(modifier = Modifier.fillMaxSize()) { previewView ->
    startCamera(context, previewView) // Now using the context outside the lambda
  }
}

@Composable
fun CameraPreviewView(modifier: Modifier = Modifier, startCamera: (PreviewView) -> Unit) {
  AndroidView(
      factory = { context ->
        val previewView = PreviewView(context)
        startCamera(previewView) // Call the regular Kotlin function here
        previewView
      },
      modifier = modifier.fillMaxSize())
}

fun startCamera(context: Context, previewView: PreviewView) {
  val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

  cameraProviderFuture.addListener(
      {
        val cameraProvider = cameraProviderFuture.get()

        val preview =
            Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
          cameraProvider.unbindAll()
          cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)
        } catch (exc: Exception) {
          Log.e("CameraX", "Use case binding failed", exc)
        }
      },
      ContextCompat.getMainExecutor(context))
}

@Composable
fun PermissionDeniedScreen(navigationActions: NavigationActions) {
  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Camera permission is required to scan barcodes. Go to Settings -> ShelfLife -> Camera Permission and enable it. ")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
              // Optionally prompt user to go to settings to enable permissions
              navigationActions.navigateTo(Route.AUTH)
            }) {
              Text(text = "Go Back")
            }
      }
}
