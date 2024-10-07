package com.android.shelfLife.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen

@Composable
fun CameraPermissionHandler(navigationActions: NavigationActions) {
  val context = LocalContext.current
  val activity = context as ComponentActivity

  when {
    // If the permission is granted, navigate to the camera screen
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED -> {
      navigationActions.navigateTo(Screen.BARCODE_SCANNER)
    }

    // If the permission was denied previously, show permission denied screen
    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) -> {
      navigationActions.navigateTo(Screen.PERMISSION_DENIED)
    }

    // First time asking for permission, show the permission request pop-up
    else -> {
      ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 101)
      navigationActions.navigateTo(Screen.PERMISSION_HANDLER)
    }
  }
}
