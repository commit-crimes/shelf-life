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
 * @property foodFactsRepository Repository for accessing food facts data.
 * @property permissionRepository Repository for managing permissions.
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

  /**
   * Initiates a search for food facts by barcode.
   *
   * @param barcode The barcode to search for.
   */
  fun searchByBarcode(barcode: Long) {
    foodFactsRepository.searchByBarcode(barcode)
  }

  /** Resets the search status to its initial state. */
  fun resetSearchStatus() {
    foodFactsRepository.resetSearchStatus()
  }

  /** Sets the search status to failure. */
  fun setFailureStatus() {
    foodFactsRepository.setFailureStatus()
  }

  /** Checks if the camera permission is granted. */
  fun checkCameraPermission() {
    permissionRepository.checkCameraPermission()
  }

  /**
   * Handles the result of the camera permission request.
   *
   * @param isGranted Boolean indicating if the permission is granted.
   */
  fun onPermissionResult(isGranted: Boolean) {
    permissionRepository.onPermissionResult(isGranted)
  }
}
