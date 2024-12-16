package com.android.shelfLife.viewmodel.camera

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.foodFacts.FoodFacts
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.SearchStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for managing the barcode scanner functionality and its related data.
 *
 * This ViewModel handles:
 * - Managing camera permission state.
 * - Initiating barcode searches using the FoodFactsRepository.
 * - Storing and updating camera permission requests in shared preferences.
 *
 * @param context The application context for accessing resources and shared preferences.
 * @param foodFactsRepository Repository to handle food-related data fetching.
 */
@HiltViewModel
class BarcodeScannerViewModel
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val foodFactsRepository: FoodFactsRepository
) : ViewModel() {

    private val application = context as Application

    // SharedPreferences to track if the camera permission has been requested
    private val sharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // Tracks if the camera permission has been granted
    var permissionGranted by mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    )
        private set

    // Tracks if the camera permission has been requested
    private var permissionRequested by
    mutableStateOf(sharedPreferences.getBoolean("permissionRequested", false))

    // Observes the search status for barcode searches
    val searchStatus: StateFlow<SearchStatus> = foodFactsRepository.searchStatus

    // Observes the list of food facts suggestions returned from the repository
    val foodFactsSuggestions: StateFlow<List<FoodFacts>> = foodFactsRepository.foodFactsSuggestions

    init {
        // Check camera permission if it hasn't been requested before
        if (!permissionRequested) {
            checkCameraPermission()
        }
    }

    /**
     * Searches for food facts using the provided barcode.
     *
     * @param barcode The barcode to search for in the FoodFactsRepository.
     */
    fun searchByBarcode(barcode: Long) {
        foodFactsRepository.searchByBarcode(barcode)
    }

    /**
     * Resets the search status to its initial state.
     */
    fun resetSearchStatus() {
        foodFactsRepository.resetSearchStatus()
    }

    /**
     * Checks if the camera permission has been granted and updates the state accordingly.
     */
    fun checkCameraPermission() {
        permissionGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
    }

    /**
     * Handles the result of the camera permission request.
     *
     * This function is called after the user responds to the permission request dialog.
     * It updates the `permissionGranted` state and stores the request status in shared preferences.
     *
     * @param isGranted A boolean indicating whether the camera permission was granted.
     */
    fun onPermissionResult(isGranted: Boolean) {
        permissionGranted = isGranted
        permissionRequested = true
        // Save the permission request status in shared preferences
        sharedPreferences.edit().putBoolean("permissionRequested", true).apply()
    }
}