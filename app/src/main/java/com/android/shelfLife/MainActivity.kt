package com.android.shelfLife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.shelfLife.ui.authentication.SignInScreen
import com.android.shelfLife.ui.camera.BarcodeScannerScreen
import com.android.shelfLife.ui.camera.CameraPermissionHandler
import com.android.shelfLife.ui.easteregg.EasterEggScreen
import com.android.shelfLife.ui.invitations.InvitationScreen
import com.android.shelfLife.ui.leaderboard.LeaderboardScreen
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.overview.AddFoodItemScreen
import com.android.shelfLife.ui.overview.ChooseFoodItem
import com.android.shelfLife.ui.overview.EditFoodItemScreen
import com.android.shelfLife.ui.overview.FirstFoodItem
import com.android.shelfLife.ui.overview.FirstTimeWelcomeScreen
import com.android.shelfLife.ui.overview.HouseHoldCreationScreen
import com.android.shelfLife.ui.overview.IndividualFoodItemScreen
import com.android.shelfLife.ui.overview.OverviewScreen
import com.android.shelfLife.ui.profile.ProfileScreen
import com.android.shelfLife.ui.recipes.AddRecipeScreen
import com.android.shelfLife.ui.recipes.IndividualRecipe.IndividualRecipeScreen
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.android.shelfLife.ui.recipes.execution.RecipeExecutionScreen
import com.android.shelfLife.ui.recipes.generateRecipe.GenerateRecipeScreen
import com.example.compose.ShelfLifeTheme
import dagger.hilt.android.AndroidEntryPoint

/** Main activity for the ShelfLife application. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  /**
   * Called when the activity is starting. This is where most initialization should go.
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   *   down then this Bundle contains the data it most recently supplied in
   *   onSaveInstanceState(Bundle).
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { ShelfLifeTheme { Surface { ShelfLifeApp() } } }
  }
}

/** Composable function that sets up the navigation for the ShelfLife application. */
@Composable
fun ShelfLifeApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  val context = LocalContext.current

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
      composable(Screen.FIRST_TIME_USER) { FirstTimeWelcomeScreen(navigationActions) }
      composable(Screen.ADD_FOOD) { AddFoodItemScreen(navigationActions) }
      composable(Screen.EDIT_FOOD) { EditFoodItemScreen(navigationActions) }
      composable(Screen.HOUSEHOLD_CREATION) { HouseHoldCreationScreen(navigationActions) }
      composable(Screen.FIRST_FOOD_ITEM) { FirstFoodItem(navigationActions) }
      composable(Screen.CHOOSE_FOOD_ITEM) { ChooseFoodItem(navigationActions) }
      composable(Screen.INDIVIDUAL_FOOD_ITEM) { IndividualFoodItemScreen(navigationActions) }
      composable(Screen.LEADERBOARD) { LeaderboardScreen(navigationActions) }
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

    navigation(startDestination = Screen.RECIPE_EXECUTION, route = Route.RECIPE_EXECUTION) {
      composable(Screen.RECIPE_EXECUTION) { RecipeExecutionScreen(navigationActions) }
    }

    navigation(startDestination = Screen.PROFILE, route = Route.PROFILE) {
      composable(Screen.PROFILE) { ProfileScreen(navigationActions, context) }
      composable(Route.INVITATIONS) { InvitationScreen(navigationActions) }
    }
    navigation(startDestination = Screen.EASTER_EGG, route = Route.EASTEREGG) {
      composable(Screen.EASTER_EGG) { EasterEggScreen(navigationActions) }
    }
  }
}
