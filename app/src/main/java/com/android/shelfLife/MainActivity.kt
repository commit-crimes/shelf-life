package com.android.shelfLife

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.shelfLife.model.camera.BarcodeScannerViewModel
import com.android.shelfLife.model.foodFacts.FoodFactsViewModel
import com.android.shelfLife.model.foodFacts.OpenFoodFactsRepository
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
import com.android.shelfLife.ui.overview.OverviewScreen
import com.android.shelfLife.ui.profile.ProfileScreen
import com.android.shelfLife.ui.recipes.IndividualRecipeScreen
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.example.compose.ShelfLifeTheme
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient

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
  val listRecipesViewModel: ListRecipesViewModel = viewModel()
  val firebaseFirestore = FirebaseFirestore.getInstance()
  val foodItemRepository = FoodItemRepositoryFirestore(firebaseFirestore)
  val listFoodItemViewModel = ListFoodItemsViewModel(foodItemRepository)
  val householdViewModel =
      HouseholdViewModel(HouseholdRepositoryFirestore(firebaseFirestore), listFoodItemViewModel)
  val foodFactsRepository = OpenFoodFactsRepository(OkHttpClient())
  val foodFactsViewModel = FoodFactsViewModel(foodFactsRepository)

  val barcodeScannerViewModel: BarcodeScannerViewModel = viewModel()

  NavHost(navController = navController, startDestination = Route.AUTH) {
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
      composable(Screen.PROFILE) { ProfileScreen(navigationActions) }
    }
  }
}
