package com.android.shelfLife

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.authentication.SignInScreen
import com.android.shelfLife.ui.camera.BarcodeScannerScreen
import com.android.shelfLife.ui.camera.CameraPermissionHandler
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.newInvitations.InvitationScreen
import com.android.shelfLife.ui.newProfile.ProfileScreen
import com.android.shelfLife.ui.newoverview.AddFoodItemScreen
import com.android.shelfLife.ui.newoverview.EditFoodItemScreen
import com.android.shelfLife.ui.newoverview.HouseHoldCreationScreen
import com.android.shelfLife.ui.newoverview.IndividualFoodItemScreen
import com.android.shelfLife.ui.newoverview.OverviewScreen
import com.android.shelfLife.ui.recipes.GenerateRecipeScreen
import com.android.shelfLife.ui.recipes.IndividualRecipe.IndividualRecipeScreen
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.android.shelfLife.ui.recipes.addRecipe.AddRecipeScreen
import com.example.compose.ShelfLifeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @Inject lateinit var userRepositoryFirestore: UserRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { ShelfLifeTheme { Surface { ShelfLifeApp(userRepositoryFirestore) } } }
  }
}

@Composable
fun ShelfLifeApp(userRepository: UserRepository) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  val context = LocalContext.current

  val isUserLoggedIn = userRepository.isUserLoggedIn.collectAsState().value

  // Observe authentication state changes
  LaunchedEffect(isUserLoggedIn) {
    if (isUserLoggedIn) {
      Log.d("ShelfLifeApp", "User is logged in, calling initializeUserData")
      userRepository.initializeUserData(context)
      navigationActions.navigateToAndClearBackStack(Screen.OVERVIEW)
    } else {
      navigationActions.navigateToAndClearBackStack(Screen.AUTH)
    }
  }

  NavHost(navController = navController, startDestination = Route.AUTH) {
    // Authentication route
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions) }
    }
    navigation(startDestination = Screen.OVERVIEW, route = Route.OVERVIEW) {
      composable(Screen.OVERVIEW) { OverviewScreen(navigationActions) }
      composable(Screen.ADD_FOOD) { AddFoodItemScreen(navigationActions) }
      composable(Screen.EDIT_FOOD) { EditFoodItemScreen(navigationActions) }
      composable(Screen.HOUSEHOLD_CREATION) { HouseHoldCreationScreen(navigationActions) }
      composable(Screen.INDIVIDUAL_FOOD_ITEM) { IndividualFoodItemScreen(navigationActions) }
    }
    navigation(startDestination = Screen.PERMISSION_HANDLER, route = Route.SCANNER) {
      composable(Screen.PERMISSION_HANDLER) { CameraPermissionHandler(navigationActions) }
      composable(Screen.BARCODE_SCANNER) { BarcodeScannerScreen(navigationActions) }
    }
    navigation(
        startDestination = Screen.RECIPES,
        route = Route.RECIPES,
    ) {
      composable(Screen.RECIPES) { RecipesScreen(navigationActions) }
      composable(Screen.INDIVIDUAL_RECIPE) { IndividualRecipeScreen(navigationActions) }
      composable(Screen.ADD_RECIPE) { AddRecipeScreen(navigationActions) }
      composable(Screen.GENERATE_RECIPE) { GenerateRecipeScreen(navigationActions) }
    }
    navigation(startDestination = Screen.PROFILE, route = Route.PROFILE) {
      composable(Screen.PROFILE) { ProfileScreen(navigationActions, context) }
      composable(Route.INVITATIONS) { InvitationScreen(navigationActions) }
    }
  }
}
