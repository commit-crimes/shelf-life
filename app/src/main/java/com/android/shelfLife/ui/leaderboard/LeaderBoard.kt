package com.android.shelfLife.ui.leaderboard

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.CustomTopAppBar
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardMode
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * Composable function that displays the leaderboard screen.
         *
         * This screen shows the leaderboard, where users can toggle between the RAT and STINKY leaderboards.
         * The top leader is highlighted, and users can interact with buttons to switch between modes.
         * If there is no leaderboard data, a message is displayed.
         *
         * @param navigationActions The navigation actions to handle screen transitions.
         * @param viewModel The [LeaderboardViewModel] responsible for handling the leaderboard data.
         */
fun LeaderboardScreen(
    navigationActions: NavigationActions,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val mode by viewModel.mode.collectAsState() // Current leaderboard mode (RAT or STINKY)
    val topLeaders by viewModel.topLeaders.collectAsState() // List of top leaders
    val currentUserId = viewModel.currentUserId // Current logged-in user's ID
    val context = LocalContext.current

    // Scaffold for layout with top bar and content area
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onClick = { navigationActions.goBack() }, // Go back to previous screen
                title = "Leaderboards",
                titleTestTag = "Leaderboards"
            )
        }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Toggle to switch between RAT and STINKY modes
            ModeToggle(currentMode = mode, onModeChange = { viewModel.switchMode(it) })

            Spacer(modifier = Modifier.height(16.dp))

            // Display message if no leaderboard data is available
            if (topLeaders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data available")
                }
            } else {
                // Display the first leader and remaining leaders in the list
                val firstLeader = topLeaders.first()
                val kingUID = viewModel.kingUID
                Log.d("LeaderboardScreen", "First leader: ${firstLeader.first}")
                val userIsKing = kingUID == currentUserId
                val isDark = isSystemInDarkTheme()

                // Highlight first place
                HighlightFirstPlace(
                    viewModel = viewModel,
                    leader = firstLeader,
                    mode = mode,
                    userIsKing = userIsKing,
                    onTogglePrize = { viewModel.togglePrize(context, isDark) })

                Spacer(modifier = Modifier.height(32.dp))

                // Display remaining leaders
                val remainingLeaders = topLeaders.drop(1)
                LeaderboardList(leaders = remainingLeaders)
            }
        }
    }
}

@Composable
        /**
         * Composable function to highlight the first place in the leaderboard.
         *
         * This function displays the first-place leader with their image (king gif), their name, and points.
         * If the current user is the king, a button is displayed to toggle the prize.
         *
         * @param viewModel The [LeaderboardViewModel] responsible for managing leaderboard data.
         * @param leader The top leader as a [Pair] of username and points.
         * @param mode The current leaderboard mode (RAT or STINKY).
         * @param userIsKing Boolean indicating if the current user is the king.
         * @param onTogglePrize A callback function triggered when the prize button is clicked.
         */
fun HighlightFirstPlace(
    viewModel: LeaderboardViewModel,
    leader: Pair<String, Long>,
    mode: LeaderboardMode,
    userIsKing: Boolean,
    onTogglePrize: () -> Unit
) {
    val (username, points) = leader

    // Select the correct GIF based on the leaderboard mode (RAT or STINKY)
    val kingGif = when (mode) {
        LeaderboardMode.RAT -> R.drawable.king_rat
        LeaderboardMode.STINKY -> R.drawable.king_stinky
    }

    // Column displaying the first place leader and prize toggle button (if the user is the king)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = kingGif,
            contentDescription = "${mode.name} King GIF",
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$username 👑",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${points.toInt()} points",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Show prize button if the current user is the king
        if (userIsKing) {
            Spacer(modifier = Modifier.height(16.dp))
            val buttonText = viewModel.buttonText.value
            Button(onClick = onTogglePrize) { Text(buttonText) }
        }
    }
}

@Composable
        /**
         * Composable function to display a list of leaderboard entries.
         *
         * This function takes a list of leaders (excluding the first place) and displays them with their rank
         * and points.
         *
         * @param leaders The list of top leaders (excluding the first place).
         */
fun LeaderboardList(leaders: List<Pair<String, Long>>) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        leaders.forEachIndexed { index, leader ->
            val rank = index + 2 // Start rank from 2 as the first place is already handled
            LeaderboardItem(rank = rank, memberId = leader.first, points = leader.second.toInt())
        }
    }
}

@Composable
        /**
         * Composable function to display a single leaderboard item.
         *
         * Displays the rank, user ID, and points for each leaderboard entry. Special emojis are displayed
         * for second and third places.
         *
         * @param rank The rank of the user on the leaderboard.
         * @param memberId The user ID of the leaderboard member.
         * @param points The points of the leaderboard member.
         */
fun LeaderboardItem(rank: Int, memberId: String, points: Int) {
    val emoji = when (rank) {
        2 -> "🥈"
        3 -> "🥉"
        else -> ""
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$rank. ", style = MaterialTheme.typography.headlineSmall)
            Text("$memberId $emoji", style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            "$points", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
        /**
         * Composable function to toggle between RAT and STINKY leaderboards.
         *
         * This function displays two buttons that allow users to switch between the RAT and STINKY leaderboard modes.
         *
         * @param currentMode The current mode (RAT or STINKY) of the leaderboard.
         * @param onModeChange A callback function that switches the leaderboard mode when a button is clicked.
         */
fun ModeToggle(currentMode: LeaderboardMode, onModeChange: (LeaderboardMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { onModeChange(LeaderboardMode.RAT) },
            colors =
            if (currentMode == LeaderboardMode.RAT) {
                ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("Rat Leaderboard")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { onModeChange(LeaderboardMode.STINKY) },
            colors =
            if (currentMode == LeaderboardMode.STINKY) {
                ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("Stinky Leaderboard")
        }
    }
}