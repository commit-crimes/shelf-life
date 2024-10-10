package com.android.shelfLife.ui.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen

/**
 * Composable function for the barcode scanner screen.
 *
 * @param navigationActions The navigation actions to be used in the screen
 * @param viewModel The ViewModel for the barcode scanner
 */
@Composable
fun BarcodeScannerScreen(
    navigationActions: NavigationActions,
    viewModel: BarcodeScannerViewModel = viewModel()
) {
  val context = LocalContext.current
  val permissionGranted = viewModel.permissionGranted

  // Observe lifecycle to detect when the app resumes
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        // Re-check the permission status when the app resumes
        viewModel.checkCameraPermission()
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  // If permission is not granted, navigate back to the PermissionHandler
  LaunchedEffect(permissionGranted) {
    if (!permissionGranted) {
      navigationActions.navigateTo(Screen.PERMISSION_HANDLER)
    }
  }

  // Rest of your BarcodeScannerScreen UI
  if (permissionGranted) {
    // Display the camera preview
    CameraPreviewView(modifier = Modifier.fillMaxSize()) { previewView ->
      startCamera(context, previewView)
    }
  } else {
    // Optionally, display a message or placeholder
    Text("Camera permission is required.")
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
fun PermissionDeniedScreen() {
  val context = LocalContext.current

  Column(
      modifier = Modifier.fillMaxSize(),
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
