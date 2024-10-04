package com.android.shelfLife

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.shelfLife.ui.camera.CameraPreviewView
import com.android.shelfLife.ui.camera.startCamera
import com.android.shelfLife.ui.theme.ShelfLifeTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Check for camera permissions
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
        PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
    } else {
      showBarcodeScannerScreen()
    }
  }

  // Handle the permission result
  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 101 &&
        grantResults.isNotEmpty() &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      showBarcodeScannerScreen() // Permission granted, start the camera
    } else {
      // Permission denied: Handle it here (e.g., show a message to the user)
    }
  }

  // Set the content to show the barcode scanner screen
  private fun showBarcodeScannerScreen() {
    setContent { ShelfLifeTheme { BarcodeScannerScreen() } }
  }
}

@Composable
fun BarcodeScannerScreen() {
  val context = LocalContext.current

  CameraPreviewView(modifier = Modifier.fillMaxSize()) { previewView ->
    startCamera(context, previewView) // This will initialize CameraX
  }
}
