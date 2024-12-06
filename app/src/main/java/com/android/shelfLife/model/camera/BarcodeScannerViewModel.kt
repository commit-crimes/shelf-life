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
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.OpenFoodFactsRepository

/**
 * ViewModel for managing the barcode scanner screen.
 *
 * @property application The application context
 */
class BarcodeScannerViewModel(application: Application, foodFactsRepository: FoodFactsRepository) : AndroidViewModel(application) {

  private val sharedPreferences =
      getApplication<Application>()
          .applicationContext
          .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

  var permissionGranted by
      mutableStateOf(
          ContextCompat.checkSelfPermission(
              getApplication<Application>().applicationContext, Manifest.permission.CAMERA) ==
              PackageManager.PERMISSION_GRANTED)
    private set

  private var permissionRequested by
      mutableStateOf(sharedPreferences.getBoolean("permissionRequested", false))

  /** Checks if the camera permission is granted. */
  fun checkCameraPermission() {
    permissionGranted =
        ContextCompat.checkSelfPermission(
            getApplication<Application>().applicationContext, Manifest.permission.CAMERA) ==
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
