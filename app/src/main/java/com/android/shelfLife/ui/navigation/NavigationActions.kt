package com.android.shelfLife.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

object Route {
  const val OVERVIEW = "Overview"
  const val SCANNER = "Scanner"
  const val PROFILE = "Profile"
  const val RECIPES = "Recipes"
  const val AUTH = "Auth"
}

object Screen {
  const val OVERVIEW = "Overview Screen"
  const val AUTH = "Auth Screen"
  const val BARCODE_SCANNER = "Barcode Scanner Screen"
  const val PERMISSION_HANDLER = "Permission Handler Screen"
  const val PERMISSION_DENIED = "Permission Denied Screen"
  const val ADD_FOOD = "Add food Screen"
  const val RECIPES = "Recipes Screen"
  const val INDIVIDUAL_FOOD_ITEM = "Individual Food Item Screen"
  const val INDIVIDUAL_RECIPE = "Individual Recipe Screen"
  const val PROFILE = "Profile Screen"
  const val HOUSEHOLD_CREATION = "Household Creation Screen"
  // Add other screens as needed
}

data class TopLevelDestination(
    val route: String,
    val icon: ImageVector, // or any other type you use for icons
    val textId: String
)

object TopLevelDestinations {
  val OVERVIEW =
      TopLevelDestination(
          route = Route.OVERVIEW,
          icon = Icons.Outlined.Menu, // Icon for the overview
          textId = "Overview" // Text label for the overview
          )
  val SCANNER =
      TopLevelDestination(
          route = Route.SCANNER,
          icon = Icons.Outlined.Videocam, // Icon for the scanner
          textId = "Scanner" // Text label for the scanner
          )
  val RECIPES =
      TopLevelDestination(
          route = Route.RECIPES,
          icon = Icons.Outlined.BookmarkBorder, // Icon for the profile
          textId = "Recipes" // Text label for the profile
          )
  val PROFILE =
      TopLevelDestination(
          route = Route.PROFILE,
          icon = Icons.Outlined.Person, // Icon for the profile
          textId = "Profile" // Text label for the profile
          )
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(
        TopLevelDestinations.OVERVIEW,
        TopLevelDestinations.SCANNER,
        TopLevelDestinations.RECIPES,
        TopLevelDestinations.PROFILE)

open class NavigationActions(
    private val navController: NavHostController,
) {

  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param destination The top-level destination to navigate to.
   *
   * Clear the back stack when navigating to a new destination.
   */
  open fun navigateTo(destination: TopLevelDestination) {
    navController.navigate(destination.route) {
      popUpTo(navController.graph.id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  open fun navigateToAndClearBackStack(screen: String) {
    navController.navigate(screen) {
      popUpTo(navController.graph.id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  open fun navigateToIndividualFood(foodItemId: String) {
    navController.navigate("${Screen.INDIVIDUAL_FOOD_ITEM}/$foodItemId")
  }

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
