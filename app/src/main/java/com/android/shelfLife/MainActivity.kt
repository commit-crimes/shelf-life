package com.android.shelfLife

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.shelfLife.ui.authentication.SignInScreen
import com.android.shelfLife.ui.camera.BarcodeScannerScreen
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.theme.ShelfLifeTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent { ShelfLifeTheme { ShelfLifeApp() } }

    checkCameraPermission()
  }

  private fun checkCameraPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
        PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
    }
  }

  // Handle permission result
  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == 101) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        // Proceed to show camera
      } else {
        Toast.makeText(
                this,
                "Camera permission denied. Please grant permission to use the camera.",
                Toast.LENGTH_LONG)
            .show()
        // Inform the user that they cannot use the feature without the permission
        showPermissionDeniedMessage()
      }
    }
  }

  private fun showPermissionDeniedMessage() {
    // You can show a dialog or use a Composable to explain why the permission is needed
    Toast.makeText(this, "Camera permission is required to scan barcodes.", Toast.LENGTH_LONG)
        .show()
  }
}

@Composable
fun ShelfLifeApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  NavHost(navController = navController, startDestination = Route.AUTH) {
    // Authentication route
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions) }
    }
    navigation(startDestination = Screen.OVERVIEW, route = Route.OVERVIEW) {
      composable(Screen.OVERVIEW) { BarcodeScannerScreen(navigationActions) }
      // Barcode Scanner route
      composable(Screen.OVERVIEW) {
        BarcodeScannerScreen(navigationActions) // Show Barcode Scanner after sign-in
      }
    }
  }
}
