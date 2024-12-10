package com.android.shelfLife

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
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
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.android.shelfLife.model.invitations.InvitationViewModel
import com.android.shelfLife.model.recipe.ListRecipesViewModel
import com.android.shelfLife.model.recipe.RecipeGeneratorOpenAIRepository
import com.android.shelfLife.model.recipe.RecipeRepositoryFirestore
import com.android.shelfLife.model.user.UserRepositoryFirestore
import com.android.shelfLife.ui.authentication.SignInScreen
import com.android.shelfLife.ui.camera.BarcodeScannerScreen
import com.android.shelfLife.ui.camera.CameraPermissionHandler
import com.android.shelfLife.ui.invitations.InvitationScreen
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.overview.AddFoodItemScreen
import com.android.shelfLife.ui.overview.EditFoodItemScreen
import com.android.shelfLife.ui.overview.HouseHoldCreationScreen
import com.android.shelfLife.ui.overview.IndividualFoodItemScreen
import com.android.shelfLife.ui.overview.OverviewScreen
import com.android.shelfLife.ui.profile.ProfileScreen
import com.android.shelfLife.ui.recipes.generateRecipe.GenerateRecipeScreen
import com.android.shelfLife.ui.recipes.IndividualRecipe.IndividualRecipeScreen
import com.android.shelfLife.ui.recipes.RecipesScreen
import com.android.shelfLife.ui.recipes.addRecipe.AddRecipeScreen
import com.android.shelfLife.viewmodel.authentication.SignInViewModel
import com.android.shelfLife.viewmodel.recipe.RecipeGenerationViewModel
import com.example.compose.ShelfLifeTheme
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { ShelfLifeTheme { Surface { ShelfLifeApp() } } }
  }
}

@Preview
@Composable
fun ShelfLifeApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val firebaseFirestore = FirebaseFirestore.getInstance()
  val foodItemRepository = FoodItemRepositoryFirestore(firebaseFirestore)
  val listFoodItemViewModel = viewModel { ListFoodItemsViewModel(foodItemRepository) }
  val invitationRepositoryFirestore = InvitationRepositoryFirestore(firebaseFirestore)
  val invitationViewModel = viewModel { InvitationViewModel(invitationRepositoryFirestore) }
  val foodFactsRepository = OpenFoodFactsRepository(OkHttpClient())
  val foodFactsViewModel = viewModel { FoodFactsViewModel(foodFactsRepository) }
  val recipeRepository = RecipeRepositoryFirestore(firebaseFirestore)
  val recipeGeneratorRepository = RecipeGeneratorOpenAIRepository()
  val listRecipesViewModel = viewModel {
    ListRecipesViewModel(recipeRepository, recipeGeneratorRepository)
  }
  val recipeGenerationViewModel = viewModel {
    RecipeGenerationViewModel(recipeRepository, recipeGeneratorRepository)
  }
  val userRepository = UserRepositoryFirestore(firebaseFirestore)
  val signInViewModel = viewModel {
    SignInViewModel(firestore = firebaseFirestore, userRepository = userRepository)
  }

  val context = LocalContext.current

  val barcodeScannerViewModel: BarcodeScannerViewModel = viewModel()

  val isUserLoggedIn by signInViewModel.isUserLoggedIn.collectAsState()
  val signInState by signInViewModel.signInState.collectAsState()

  // Observe authentication state changes
  LaunchedEffect(isUserLoggedIn) {
    if (isUserLoggedIn) {
      navController.navigate(Route.OVERVIEW) { popUpTo(Route.AUTH) { inclusive = true } }
    } else {
      navController.navigate(Route.AUTH) { popUpTo(Route.OVERVIEW) { inclusive = true } }
    }
  }

  // Initialize HouseholdViewModel only if the user is logged in
  val householdViewModel = viewModel {
    HouseholdViewModel(
        houseHoldRepository = HouseholdRepositoryFirestore(firebaseFirestore),
        listFoodItemsViewModel = listFoodItemViewModel,
        invitationRepository = invitationRepositoryFirestore,
        context.dataStore)
  }

  NavHost(navController = navController, startDestination = Route.AUTH) {
    // Authentication route
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions, userRepository) }
    }
    navigation(startDestination = Screen.OVERVIEW, route = Route.OVERVIEW) {
      composable(Screen.OVERVIEW) {
        OverviewScreen(navigationActions, householdViewModel, listFoodItemViewModel)
      }
      composable(Screen.ADD_FOOD) {
        AddFoodItemScreen(
            navigationActions, householdViewModel, listFoodItemViewModel, foodFactsViewModel)
      }
      composable(Screen.EDIT_FOOD) {
        EditFoodItemScreen(navigationActions, householdViewModel, listFoodItemViewModel)
      }
      composable(Screen.HOUSEHOLD_CREATION) {
        HouseHoldCreationScreen(navigationActions, householdViewModel = householdViewModel)
      }
      composable(Screen.INDIVIDUAL_FOOD_ITEM) {
        IndividualFoodItemScreen(
            navigationActions = navigationActions, householdViewModel, listFoodItemViewModel)
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
      composable(Screen.ADD_RECIPE) { AddRecipeScreen(navigationActions, listRecipesViewModel) }
      composable(Screen.GENERATE_RECIPE) {
        GenerateRecipeScreen(navigationActions, recipeRepository, recipeGeneratorRepository)
            /*navigationActions, recipeRepository, recipeGeneratorRepository*/
      }
    }
    navigation(startDestination = Screen.PROFILE, route = Route.PROFILE) {
      composable(Screen.PROFILE) {
        ProfileScreen(navigationActions, invitationViewModel = invitationViewModel)
      }
      composable(Route.INVITATIONS) {
        InvitationScreen(
            navigationActions = navigationActions,
            invitationRepository = invitationRepositoryFirestore)
      }
    }
  }
}
