import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.viewmodel.recipes.ExecuteRecipeViewModel

/**
 * Composable function to display the instruction screen for executing a recipe.
 *
 * @param navigationActions The actions to handle navigation.
 * @param viewModel The ViewModel for managing the state of the recipe execution.
 * @param onFinish Callback function to handle the finish action.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun InstructionScreen(
    navigationActions: NavigationActions,
    viewModel: ExecuteRecipeViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val currentInstruction by viewModel.currentInstruction.collectAsState()

    Scaffold(
        modifier = Modifier.testTag("instructionScreen"),
        topBar = {
            TopAppBar(
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                modifier = Modifier.testTag("topBar"),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Log.d("InstructionScreen", "TopAppBar Back button clicked")
                            navigationActions.goBack()
                        },
                        modifier = Modifier.testTag("goBackArrow")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                title = {
                    Text(
                        text = "Instructions",
                        style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 24.sp, fontWeight = FontWeight.Bold))
                })
        },
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { destination ->
                    navigationActions.navigateTo(destination)
                    Log.d("InstructionScreen", "BottomNavigationMenu: Navigated to $destination")
                },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.RECIPES)
        }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally) {
            // Display the current instruction with animation
            AnimatedContent(
                targetState = currentInstruction,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut() // Define fade animations
                },
                modifier = Modifier.weight(1f).padding(16.dp)) { instruction ->
                Text(
                    modifier = Modifier.testTag("instructionText"),
                    text = instruction ?: "No instructions available",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
            }

            // Navigation Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)) {
                // Back button
                if (viewModel.hasPreviousInstructions()) {
                    Button(
                        onClick = {
                            viewModel.previousInstruction()
                            Log.d(
                                "InstructionScreen",
                                "Back button clicked. Current Index: ${viewModel.currentInstructionIndex.value}")
                        },
                        modifier = Modifier.testTag("backButton")) {
                        Text("Back")
                    }
                }

                // Next/Finish button
                if (viewModel.hasMoreInstructions()) {
                    Button(
                        onClick = {
                            viewModel.nextInstruction()
                            Log.d(
                                "InstructionScreen",
                                "Next button clicked. Current Index: ${viewModel.currentInstructionIndex.value}")
                        },
                        modifier = Modifier.testTag("nextButton")) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = {
                            onFinish()
                            Log.d("InstructionScreen", "Finish button clicked.")
                        },
                        modifier = Modifier.testTag("finishButton")) {
                        Text("Finish")
                    }
                }
            }
        }
    }
}