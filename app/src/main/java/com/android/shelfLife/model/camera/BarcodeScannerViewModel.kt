package com.android.shelfLife.model.camera

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel

/**
 * ViewModel for managing the barcode scanner screen.
 *
 * @property application The application context
 */
class BarcodeScannerViewModel(application: Application) : AndroidViewModel(application) {

  private val context = getApplication<Application>().applicationContext
  private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

  var permissionGranted by
      mutableStateOf(
          ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
              PackageManager.PERMISSION_GRANTED)
    private set

  var permissionRequested by
      mutableStateOf(sharedPreferences.getBoolean("permissionRequested", false))
    private set

  /** Checks if the camera permission is granted. */
  fun checkCameraPermission() {
    permissionGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
  }

  /**
   * Requests the camera permission.
   *
   * @param isGranted boolean indicating if the permission is granted
   */
  fun onPermissionResult(isGranted: Boolean) {
    permissionGranted = isGranted
    permissionRequested = true
    sharedPreferences.edit().putBoolean("permissionRequested", true).apply()
  }
}
