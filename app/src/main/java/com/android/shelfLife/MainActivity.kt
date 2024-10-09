package com.android.shelfLife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.ui.authentication.SignInScreen
import com.android.shelfLife.ui.camera.BarcodeScannerScreen
import com.android.shelfLife.ui.camera.CameraPermissionHandler
import com.android.shelfLife.ui.camera.PermissionDeniedScreen
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.recipes.IndividualRecipeScreen
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.android.shelfLife.ui.theme.ShelfLifeTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { ShelfLifeTheme { ShelfLifeApp() } }
  }
}

@Composable
fun ShelfLifeApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val listRecipesViewModel = ListRecipesViewModel()

  NavHost(navController = navController, startDestination = Route.AUTH) {
    // Authentication route
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions) }
    }

    navigation(startDestination = Screen.PERMISSION_HANDLER, route = Route.SCANNER) {
      composable(Screen.PERMISSION_HANDLER) { CameraPermissionHandler(navigationActions) }
      composable(Screen.BARCODE_SCANNER) { BarcodeScannerScreen(navigationActions) }
      composable(Screen.PERMISSION_DENIED) {
        PermissionDeniedScreen(navigationActions)
      } // For handling denied permission
    }

    navigation(
      startDestination = Screen.RECIPES,
      route = Route.RECIPES,
    ){
      composable(Screen.RECIPES){ RecipesScreen(navigationActions, listRecipesViewModel) }
      composable(Screen.INDIVIDUAL_RECIPE){ IndividualRecipeScreen(navigationActions, listRecipesViewModel) }
    }
  }
}
