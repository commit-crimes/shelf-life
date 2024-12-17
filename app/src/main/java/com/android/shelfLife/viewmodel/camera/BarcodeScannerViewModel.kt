package com.android.shelfLife.viewmodel.camera

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.SearchStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing the barcode scanner screen.
 *
 * @property application The application context
 */
@HiltViewModel
class BarcodeScannerViewModel
@Inject
constructor(
    private val application: Application,
    private val foodFactsRepository: FoodFactsRepository
) : ViewModel(), LifecycleObserver {

  private val sharedPreferences =
      application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

  var permissionGranted by
      mutableStateOf(
          ContextCompat.checkSelfPermission(application, Manifest.permission.CAMERA) ==
              PackageManager.PERMISSION_GRANTED)
    private set

  private var permissionRequested by
      mutableStateOf(sharedPreferences.getBoolean("permissionRequested", false))

  val searchStatus: StateFlow<SearchStatus> = foodFactsRepository.searchStatus

  val foodFactsSuggestions: StateFlow<List<FoodFacts>> = foodFactsRepository.foodFactsSuggestions

  init {
    if (!permissionRequested) {
      checkCameraPermission()
    }
  }

  fun searchByBarcode(barcode: Long) {
    foodFactsRepository.searchByBarcode(barcode)
  }

  fun resetSearchStatus() {
    foodFactsRepository.resetSearchStatus()
  }

  /** Checks if the camera permission is granted. */
  fun checkCameraPermission() {
    permissionGranted =
        ContextCompat.checkSelfPermission(application, Manifest.permission.CAMERA) ==
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
