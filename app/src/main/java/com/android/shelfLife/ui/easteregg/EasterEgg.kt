package com.android.shelfLife.ui.easteregg

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.CustomTopAppBar

/**
 * Composable function for displaying the Easter Egg screen.
 *
 * This screen contains an Easter egg image and a message that should not normally appear in the app.
 * It features a custom top app bar with a back button, and an error message to indicate the absence
 * of a selected recipe, which is unexpected behavior.
 *
 * @param navigationActions The navigation actions used to navigate back to the previous screen.
 */
@Composable
fun EasterEggScreen(navigationActions: NavigationActions) {

    // Scaffold serves as the layout container for the screen
    Scaffold(
        modifier = Modifier.fillMaxSize(), // Ensure Scaffold takes up the full size of the screen
        topBar = {
            // Custom top app bar with a back button and title
            CustomTopAppBar(
                onClick = { navigationActions.goBack() }, // Navigates back on back button click
                title = stringResource(id = R.string.easteregg_title), // Title for the screen
                titleTestTag = "eastereggTitle", // Test tag for the title
            )
        },
        content = { paddingValues ->
            // Column for content inside the Scaffold
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues), // Apply padding from Scaffold
                horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
                verticalArrangement = Arrangement.Center // Center content vertically
            ) {
                // Easter egg image displayed in the center of the screen
                Image(
                    painter = painterResource(id = R.drawable.how_did_we_get_here), // Image resource
                    contentDescription = "How did we get here?", // Image description for accessibility
                    modifier = Modifier.fillMaxWidth(), // Make image fill the width of the screen
                    contentScale = ContentScale.Fit) // Ensure image fits within bounds

                // Spacer for some space between the image and the text
                Spacer(modifier = Modifier.size(16.dp))

                // Error message text displayed below the image
                Text(
                    text = "No recipe selected. Should not happen", // Error message text
                    modifier = Modifier.testTag("noRecipeSelectedMessage"), // Test tag for UI tests
                    color = Color.Red) // Display message in red to indicate an error
            }
        })
}