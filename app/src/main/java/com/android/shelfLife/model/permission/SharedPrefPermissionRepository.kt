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

class SharedPrefPermissionRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
) : PermissionRepository {
  private val application = context as Application
  private val sharedPreferences =
      application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

  private val _permissionGranted =
      MutableStateFlow(
          ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
              PackageManager.PERMISSION_GRANTED)

  override val permissionGranted = _permissionGranted.asStateFlow()

  private val _permissionRequested =
      MutableStateFlow(sharedPreferences.getBoolean("permissionRequested", false))

  override val permissionRequested = _permissionRequested.asStateFlow()

  override fun checkCameraPermission() {
    _permissionGranted.value =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
  }

  override fun onPermissionResult(isGranted: Boolean) {
    _permissionGranted.value = isGranted
    _permissionRequested.value = true
    sharedPreferences.edit().putBoolean("permissionRequested", true).apply()
  }
}
