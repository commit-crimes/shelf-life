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

class BarcodeScannerViewModel(application: Application) : AndroidViewModel(application) {

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

  private var scannedBarcode by mutableStateOf<String?>(null)

  fun onBarcodeScanned(barcode: String) {
    scannedBarcode = barcode
    // TODO add additional logic here, such as fetching product info
  }

  fun checkCameraPermission() {
    permissionGranted =
        ContextCompat.checkSelfPermission(
            getApplication<Application>().applicationContext, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
  }

  fun onPermissionResult(isGranted: Boolean) {
    permissionGranted = isGranted
    permissionRequested = true
    sharedPreferences.edit().putBoolean("permissionRequested", true).apply()
  }
}
