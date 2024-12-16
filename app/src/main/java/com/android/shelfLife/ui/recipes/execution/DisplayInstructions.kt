package com.android.shelfLife.ui.recipes.execution

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
        /**
         * Composable function to display the instruction screen of a recipe.
         *
         * This screen displays the step-by-step instructions for executing a recipe. The user can navigate
         * through the instructions using "Back" and "Next" buttons. If there are no more instructions,
         * the "Finish" button will appear, allowing the user to finish the recipe process. The screen includes
         * a top app bar for navigation, a bottom navigation menu, and animated transitions for instructions.
         *
         * @param navigationActions The navigation actions to handle screen transitions.
         * @param viewModel The [ExecuteRecipeViewModel] responsible for managing the recipe data and instruction flow.
         * @param onFinish Lambda function to call when the user finishes the recipe.
         */
fun InstructionScreen(
    navigationActions: NavigationActions,
    viewModel: ExecuteRecipeViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val currentInstruction by viewModel.currentInstruction.collectAsState() // Current instruction to display

    // Scaffold to provide the basic structure of the screen with top and bottom bars
    Scaffold(
        modifier = Modifier.testTag("instructionScreen"),
        topBar = {
            // Top AppBar with a close button to navigate back
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.testTag("topBar"),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Log.d("InstructionScreen", "TopAppBar Back button clicked")
                            navigationActions.goBack() // Go back on close button click
                        },
                        modifier = Modifier.testTag("goBackArrow")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                title = {
                    // Display the recipe name as the title
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        },
        bottomBar = {
            // Bottom navigation menu
            BottomNavigationMenu(
                onTabSelect = { destination ->
                    navigationActions.navigateTo(destination)
                    Log.d("InstructionScreen", "BottomNavigationMenu: Navigated to $destination")
                },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.RECIPES
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the current instruction with animated transition
            AnimatedContent(
                targetState = currentInstruction,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut() // Fade-in and fade-out transition for instructions
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) { instruction ->
                Text(
                    text = instruction ?: "No instructions available", // Default text if no instruction is available
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                )
            }

            // Row for navigation buttons (Back, Next, Finish)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                // Back button, only visible if there are previous instructions
                if (viewModel.hasPreviousInstructions()) {
                    Button(
                        onClick = {
                            viewModel.previousInstruction()
                            Log.d("InstructionScreen", "Back button clicked. Current Index: ${viewModel.currentInstructionIndex.value}")
                        },
                        modifier = Modifier.testTag("backButton")
                    ) {
                        Text("Back")
                    }
                }

                // Next button, visible if there are more instructions
                if (viewModel.hasMoreInstructions()) {
                    Button(
                        onClick = {
                            viewModel.nextInstruction()
                            Log.d("InstructionScreen", "Next button clicked. Current Index: ${viewModel.currentInstructionIndex.value}")
                        },
                        modifier = Modifier.testTag("nextButton")
                    ) {
                        Text("Next")
                    }
                } else {
                    // Finish button, visible if there are no more instructions
                    Button(
                        onClick = {
                            onFinish() // Finish the recipe process
                            Log.d("InstructionScreen", "Finish button clicked.")
                        },
                        modifier = Modifier.testTag("finishButton")
                    ) {
                        Text("Finish")
                    }
                }
            }
        }
    }
}