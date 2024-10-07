package com.android.shelfLife.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen

@Composable
fun CameraPermissionHandler(navigationActions: NavigationActions) {
  val context = LocalContext.current
  val activity = context as Activity

  // State to keep track of whether the permission is granted
  var permissionGranted by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  // State to keep track of whether we have already requested the permission
  var permissionRequested by remember { mutableStateOf(false) }

  // Permission launcher
  val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    permissionGranted = isGranted
    permissionRequested = true
  }

  // Observe lifecycle to detect when the app resumes
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        // Re-check the permission status when the app resumes
        val newPermissionStatus = ContextCompat.checkSelfPermission(
          context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted != newPermissionStatus) {
          permissionGranted = newPermissionStatus
        }
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // Navigate to the camera screen when permission is granted
  LaunchedEffect(permissionGranted) {
    if (permissionGranted) {
      navigationActions.navigateTo(Screen.BARCODE_SCANNER)
    }
  }

  when {
    permissionGranted -> {
      // Permission is granted; navigation is handled in LaunchedEffect
    }
    !permissionRequested -> {
      // First-time request: show the standard permission pop-up
      SideEffect {
        launcher.launch(Manifest.permission.CAMERA)
      }
    }
    else -> {
      // Permission denied: show the PermissionDeniedScreen
      PermissionDeniedScreen()
    }
  }
}