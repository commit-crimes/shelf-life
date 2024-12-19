package com.android.shelfLife.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

/**
 * Object containing route constants for navigation.
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
 * Object containing screen constants for navigation.
 */
object Screen {
    const val LEADERBOARD = "Leaderboard Screen"
    const val OVERVIEW = "Overview Screen"
    const val AUTH = "Auth Screen"
    const val BARCODE_SCANNER = "Barcode Scanner Screen"
    const val PERMISSION_HANDLER = "Permission Handler Screen"
    const val FIRST_FOOD_ITEM = "First Add Food Item Screen"
    const val CHOOSE_FOOD_ITEM = "Choose Image for Food Item Screen"
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
    const val SERVINGS_SCREEN = "Servings Screen"
    const val FOOD_ITEM_SELECTION = "Food Item Selection Screen"
    const val INSTRUCTION_SCREEN = "Instruction Screen"
    const val RECIPE_EXECUTION = "Recipe Execution Screen"
    const val EASTER_EGG = "Easteregg Screen"
    // Add other screens as needed
}

/**
 * Data class representing a top-level destination in the navigation.
 *
 * @param route The route of the destination.
 * @param icon The icon associated with the destination.
 * @param textId The text label for the destination.
 */
data class TopLevelDestination(
    val route: String,
    val icon: ImageVector, // or any other type you use for icons
    val textId: String
)

/**
 * Object containing top-level destinations for navigation.
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

/**
 * List of top-level destinations.
 */
val LIST_TOP_LEVEL_DESTINATION =
    listOf(
        TopLevelDestinations.OVERVIEW,
        TopLevelDestinations.SCANNER,
        TopLevelDestinations.RECIPES,
        TopLevelDestinations.PROFILE)

/**
 * Class containing navigation actions.
 *
 * @param navController The navigation controller.
 */
open class NavigationActions(
    private val navController: NavHostController,
) {

    /**
     * Navigate to the specified [TopLevelDestination].
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

    /**
     * Navigate to the specified screen and clear the back stack.
     *
     * @param screen The screen to navigate to.
     */
    open fun navigateToAndClearBackStack(screen: String) {
        navController.navigate(screen) {
            popUpTo(navController.graph.id) { saveState = true }
            launchSingleTop = true
            restoreState = true
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
     * @return The current route.
     */
    open fun currentRoute(): String {
        return navController.currentDestination?.route ?: ""
    }
}