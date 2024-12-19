package com.android.shelfLife.model.permission

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing permissions within the application.
 *
 * This interface defines the contract for a repository that handles the operations related to permissions,
 * including checking and handling the result of permission requests.
 */
interface PermissionRepository {

  /** A StateFlow that emits whether the permission has been requested. */
  val permissionRequested: StateFlow<Boolean>

  /** A StateFlow that emits whether the permission has been granted. */
  val permissionGranted: StateFlow<Boolean>

  /** Checks the status of the camera permission. */
  fun checkCameraPermission()

  /**
   * Handles the result of a permission request.
   *
   * @param isGranted True if the permission was granted, false otherwise.
   */
  fun onPermissionResult(isGranted: Boolean)
}