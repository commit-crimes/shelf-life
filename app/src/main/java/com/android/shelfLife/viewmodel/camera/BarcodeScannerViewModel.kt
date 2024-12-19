package com.android.shelfLife.viewmodel.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.SearchStatus
import com.android.shelfLife.model.permission.PermissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val foodFactsRepository: FoodFactsRepository,
    private val permissionRepository: PermissionRepository
) : ViewModel() {

  private val permissionRequested = permissionRepository.permissionRequested
  val permissionGranted = permissionRepository.permissionGranted

  val searchStatus: StateFlow<SearchStatus> = foodFactsRepository.searchStatus

  val foodFactsSuggestions: StateFlow<List<FoodFacts>> = foodFactsRepository.foodFactsSuggestions

  init {
    if (!permissionRequested.value) {
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
    permissionRepository.checkCameraPermission()
  }

  /**
   * Requests the camera permission.
   *
   * @param isGranted boolean indicating if the permission is granted
   */
  fun onPermissionResult(isGranted: Boolean) {
    permissionRepository.onPermissionResult(isGranted)
  }
}
