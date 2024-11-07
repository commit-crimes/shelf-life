package com.android.shelfLife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.OpenFoodFactsRepository
import com.android.shelfLife.model.foodItem.FoodItem
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.android.shelfLife.model.foodItem.ListFoodItemsViewModel
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.ui.authentication.SignInScreen
import com.android.shelfLife.ui.camera.BarcodeScannerScreen
import com.android.shelfLife.ui.camera.CameraPermissionHandler
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.overview.AddFoodItemScreen
import com.android.shelfLife.ui.overview.IndividualFoodItemScreen
import com.android.shelfLife.ui.overview.OverviewScreen
import com.android.shelfLife.ui.profile.ProfileScreen
import com.android.shelfLife.ui.recipes.IndividualRecipeScreen
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.android.shelfLife.ui.utils.signOutUser
import com.example.compose.ShelfLifeTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { ShelfLifeTheme { Surface { ShelfLifeApp() } } }
  }
}

@Composable
fun ShelfLifeApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val listRecipesViewModel: ListRecipesViewModel = viewModel()
  val firebaseFirestore = FirebaseFirestore.getInstance()
  val foodItemRepository = FoodItemRepositoryFirestore(firebaseFirestore)
  val listFoodItemViewModel = ListFoodItemsViewModel(foodItemRepository)
  val foodFactsRepository = OpenFoodFactsRepository(OkHttpClient())
  val foodFactsViewModel = FoodFactsViewModel(foodFactsRepository)
  val context = LocalContext.current

  val barcodeScannerViewModel: BarcodeScannerViewModel = viewModel()

  // Checks if user is logged in and selects correct screen
  val firebaseUser = FirebaseAuth.getInstance().currentUser
  val startingRoute =
      if (firebaseUser == null) {
        Route.AUTH
      } else {
        Route.OVERVIEW
      }

  // Initialize HouseholdViewModel only if the user is logged in
  val householdViewModel =
      HouseholdViewModel(HouseholdRepositoryFirestore(firebaseFirestore), listFoodItemViewModel)

  NavHost(navController = navController, startDestination = startingRoute) {
    // Authentication route
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions) }
    }
    navigation(startDestination = Screen.OVERVIEW, route = Route.OVERVIEW) {
      composable(Screen.OVERVIEW) { OverviewScreen(navigationActions, householdViewModel) }
      composable(Screen.ADD_FOOD) {
        AddFoodItemScreen(navigationActions, householdViewModel, listFoodItemViewModel)
      }
        composable("${Screen.INDIVIDUAL_FOOD_ITEM}/{foodItemId}") {backStackEntry ->
            val foodItemId = backStackEntry.arguments?.getString("foodItemId")
                IndividualFoodItemScreen(
                    foodItemId = foodItemId,
                    navigationActions = navigationActions,
                    householdViewModel = householdViewModel
                )
        }
    }
    navigation(startDestination = Screen.PERMISSION_HANDLER, route = Route.SCANNER) {
      composable(Screen.PERMISSION_HANDLER) {
        CameraPermissionHandler(navigationActions, barcodeScannerViewModel)
      }
      composable(Screen.BARCODE_SCANNER) {
        BarcodeScannerScreen(
            navigationActions,
            barcodeScannerViewModel,
            foodFactsViewModel,
            householdViewModel,
            listFoodItemViewModel)
      }
    }
    navigation(
        startDestination = Screen.RECIPES,
        route = Route.RECIPES,
    ) {
      composable(Screen.RECIPES) {
        RecipesScreen(navigationActions, listRecipesViewModel, householdViewModel)
      }
      composable(Screen.INDIVIDUAL_RECIPE) {
        IndividualRecipeScreen(navigationActions, listRecipesViewModel, householdViewModel)
      }
    }
    navigation(startDestination = Screen.PROFILE, route = Route.PROFILE) {
      composable(Screen.PROFILE) {
        ProfileScreen(
            navigationActions,
            signOutUser = {
              signOutUser(context) {
                // Navigate to the authentication screen
                navigationActions.navigateToAndClearBackStack(Route.AUTH)
              }
            })
      }
    }
  }
}
