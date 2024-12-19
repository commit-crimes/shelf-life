package com.android.shelfLife.model.permission

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository class for managing permissions using SharedPreferences.
 *
 * @property context The application context.
 */
class SharedPrefPermissionRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
) : PermissionRepository {
  private val application = context as Application
  private val sharedPreferences =
      application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

  // StateFlow to track if the permission is granted
  private val _permissionGranted =
      MutableStateFlow(
          ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
              PackageManager.PERMISSION_GRANTED)

  /** StateFlow that emits whether the permission has been granted. */
  override val permissionGranted = _permissionGranted.asStateFlow()

  // StateFlow to track if the permission has been requested
  private val _permissionRequested =
      MutableStateFlow(sharedPreferences.getBoolean("permissionRequested", false))

  /** StateFlow that emits whether the permission has been requested. */
  override val permissionRequested = _permissionRequested.asStateFlow()

  /** Checks the status of the camera permission. */
  override fun checkCameraPermission() {
    _permissionGranted.value =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
  }

  /**
   * Handles the result of a permission request.
   *
   * @param isGranted True if the permission was granted, false otherwise.
   */
  override fun onPermissionResult(isGranted: Boolean) {
    _permissionGranted.value = isGranted
    _permissionRequested.value = true
    sharedPreferences.edit().putBoolean("permissionRequested", true).apply()
  }
}
