package com.android.shelfLife.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

object Route {
  const val OVERVIEW = "Overview"
  const val MAP = "Map"
  const val AUTH = "Auth"
}

object Screen {
  const val OVERVIEW = "Overview Screen"
  const val ADD_TODO = "Add ToDo Screen"
  const val AUTH = "Auth Screen"
  const val MAP = "Map Screen"
  const val EDIT_TODO = "Edit ToDo Screen"
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
  val MAP =
      TopLevelDestination(
          route = Route.MAP,
          icon = Icons.Outlined.Language, // Icon for the map
          textId = "Map" // Text label for the map
          )
}

val LIST_TOP_LEVEL_DESTINATION = listOf(TopLevelDestinations.OVERVIEW, TopLevelDestinations.MAP)

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
      popUpTo(navController.graph.startDestinationId) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
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
