package com.android.shelfLife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
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
    setContent {
      ShelfLifeTheme {
        ShelfLifeApp() // Start the navigation and app setup
      }
    }
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
