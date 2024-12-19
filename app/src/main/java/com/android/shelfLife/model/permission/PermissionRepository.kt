package com.android.shelfLife.model.permission

import kotlinx.coroutines.flow.StateFlow

interface PermissionRepository {
  val permissionRequested: StateFlow<Boolean>

  val permissionGranted: StateFlow<Boolean>

  fun checkCameraPermission()

  fun onPermissionResult(isGranted: Boolean)
}
