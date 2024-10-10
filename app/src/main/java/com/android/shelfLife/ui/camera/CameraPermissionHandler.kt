package com.android.shelfLife.ui.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen

/**
 * Composable function for handling camera permissions.
 *
 * @param navigationActions The navigation actions to be used in the screen
 * @param viewModel The ViewModel for the barcode scanner
 */
@Composable
fun CameraPermissionHandler(
    navigationActions: NavigationActions,
    viewModel: BarcodeScannerViewModel = viewModel()
) {
  val context = LocalContext.current
  val activity = context as Activity

  val permissionGranted = viewModel.permissionGranted

  // For triggering permission requests
  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
          isGranted: Boolean ->
        viewModel.onPermissionResult(isGranted) // Update permission result in ViewModel
      }

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

  // Check if we should show a rationale for the permission
  var shouldShowRationale by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    shouldShowRationale =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
  }

  // Core logic for handling permission states
  LaunchedEffect(permissionGranted, shouldShowRationale) {
    if (permissionGranted) {
      // If permission is granted, navigate to the camera screen
      navigationActions.navigateTo(Screen.BARCODE_SCANNER)
    } else {
      val currentPermissionStatus =
          ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

      when {
        // "Don't allow" state (no pop-up, just show PermissionDeniedScreen)
        currentPermissionStatus == PackageManager.PERMISSION_DENIED && shouldShowRationale -> {
          // The permission is denied and should not trigger a rationale pop-up.
          // This means the user set "Don't allow" in the settings.
          // Show the PermissionDeniedScreen without pop-up.
        }

        // "Ask every time" state (show pop-up and PermissionDeniedScreen)
        currentPermissionStatus == PackageManager.PERMISSION_DENIED && !shouldShowRationale -> {
          // The permission is denied but we should show the rationale and pop-up.
          // This means the user is in "Ask every time" mode or denied without "Don't allow."
          launcher.launch(Manifest.permission.CAMERA)
        }
      }
    }
  }

  // Show the PermissionDeniedScreen if permission is not granted

  if (!permissionGranted) {
    PermissionDeniedScreen(navigationActions)
  }
}
