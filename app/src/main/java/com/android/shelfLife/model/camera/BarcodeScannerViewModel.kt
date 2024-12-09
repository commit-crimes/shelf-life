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
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.SearchStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for managing the barcode scanner screen.
 *
 * @property application The application context
 */
@HiltViewModel
class BarcodeScannerViewModel
@Inject
constructor(
    application: Application,
    private val foodFactsRepository: FoodFactsViewModel
    ) : AndroidViewModel(application) {

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

  val searchStatus: StateFlow<SearchStatus> = foodFactsRepository.searchStatus

  val foodFactsSuggestions: StateFlow<List<FoodFacts>> = foodFactsRepository.foodFactsSuggestions

  fun searchByBarcode(barcode: Long) {
    foodFactsRepository.searchByBarcode(barcode)
  }

  fun resetSearchStatus() {
    foodFactsRepository.resetSearchStatus()
  }

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
