package com.android.shelfLife.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

/**
 * Object defining the various routes for navigation in the app.
 * These routes represent the different destinations that the app can navigate to.
 */
object Route {
    const val OVERVIEW = "Overview"
    const val SCANNER = "Scanner"
    const val PROFILE = "Profile"
    const val RECIPES = "Recipes"
    const val AUTH = "Auth"
    const val INVITATIONS = "Invitations"
    const val FIRST_TIME_USER = "First Time User"
    const val EASTEREGG = "Easteregg"
    const val RECIPE_EXECUTION = "Recipe Execution"
}

/**
 * Object defining the screens that correspond to the routes in the app.
 * Each screen represents a UI screen that users can navigate to.
 */
object Screen {
    const val LEADERBOARD = "Leaderboard Screen"
    const val OVERVIEW = "Overview Screen"
    const val AUTH = "Auth Screen"
    const val BARCODE_SCANNER = "Barcode Scanner Screen"
    const val PERMISSION_HANDLER = "Permission Handler Screen"
    const val ADD_FOOD = "Add food Screen"
    const val EDIT_FOOD = "Edit food Screen"
    const val RECIPES = "Recipes Screen"
    const val INDIVIDUAL_FOOD_ITEM = "Individual Food Item Screen"
    const val INDIVIDUAL_RECIPE = "Individual Recipe Screen"
    const val ADD_RECIPE = "Add recipe Screen"
    const val GENERATE_RECIPE = "Generate recipe Screen"
    const val PROFILE = "Profile Screen"
    const val HOUSEHOLD_CREATION = "Household Creation Screen"
    const val FIRST_TIME_USER = "First Time User Screen"
    const val RECIPE_EXECUTION = "Recipe Execution Screen"
    const val EASTER_EGG = "Easteregg Screen"
    // Add other screens as needed
}

/**
 * Data class to represent top-level navigation destinations in the app.
 * Each destination includes a route, an icon, and a text label for display.
 */
data class TopLevelDestination(
    val route: String,
    val icon: ImageVector, // Icon for the destination
    val textId: String // Text label for the destination
)

/**
 * Object containing the top-level destinations for the app.
 * Each destination is associated with a specific route and icon for easy navigation.
 */
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
            icon = Icons.Outlined.BookmarkBorder, // Icon for the recipes
            textId = "Recipes" // Text label for the recipes
        )
    val PROFILE =
        TopLevelDestination(
            route = Route.PROFILE,
            icon = Icons.Outlined.Person, // Icon for the profile
            textId = "Profile" // Text label for the profile
        )
}

/**
 * List of top-level destinations to display in the app's main navigation.
 */
val LIST_TOP_LEVEL_DESTINATION =
    listOf(
        TopLevelDestinations.OVERVIEW,
        TopLevelDestinations.SCANNER,
        TopLevelDestinations.RECIPES,
        TopLevelDestinations.PROFILE
    )

/**
 * Class responsible for managing navigation actions in the app.
 * Provides methods to navigate between screens, including navigation to top-level destinations
 * and navigating with a cleared back stack.
 *
 * @param navController The navigation controller used to manage app navigation.
 */
open class NavigationActions(
    private val navController: NavHostController,
) {

    /**
     * Navigate to the specified [TopLevelDestination] and clear the back stack.
     *
     * @param destination The top-level destination to navigate to.
     */
    open fun navigateTo(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.id) { saveState = true } // Save state of the navigation graph
            launchSingleTop = true // Ensure only a single instance of the screen is on top
            restoreState = true // Restore the state when navigating back to this screen
        }
    }

    /**
     * Navigate to a specified screen and clear the back stack.
     *
     * @param screen The screen to navigate to.
     */
    open fun navigateToAndClearBackStack(screen: String) {
        navController.navigate(screen) {
            popUpTo(navController.graph.id) { saveState = true } // Save state of the navigation graph
            launchSingleTop = true // Ensure only a single instance of the screen is on top
            restoreState = true // Restore the state when navigating back to this screen
        }
    }

    /**
     * Navigate to the specified screen.
     *
     * @param screen The screen to navigate to.
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
     * @return The current route as a string.
     */
    open fun currentRoute(): String {
        return navController.currentDestination?.route ?: ""
    }
}