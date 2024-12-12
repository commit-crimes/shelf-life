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
fun LeaderboardScreen(
    navigationActions: NavigationActions,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
  val mode by viewModel.mode.collectAsState()
  val topLeaders by viewModel.topLeaders.collectAsState()
  val currentUserId = viewModel.currentUserId
  val context = LocalContext.current

  Scaffold(
      topBar = {
        CustomTopAppBar(
            onClick = { navigationActions.goBack() },
            title = "Leaderboards",
            titleTestTag = "Leaderboards",
        )
      }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
          ModeToggle(currentMode = mode, onModeChange = { viewModel.switchMode(it) })

          Spacer(modifier = Modifier.height(16.dp))

          if (topLeaders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("No data available")
            }
          } else {
            val firstLeader = topLeaders.first()
            val kingUID = viewModel.kingUID
            Log.d("LeaderboardScreen", "First leader: ${firstLeader.first}")
            val userIsKing = kingUID == currentUserId
            val isDark = isSystemInDarkTheme()

            HighlightFirstPlace(
                viewModel = viewModel,
                leader = firstLeader,
                mode = mode,
                userIsKing = userIsKing,
                onTogglePrize = { viewModel.togglePrize(context, isDark) })

            Spacer(modifier = Modifier.height(32.dp))

            val remainingLeaders = topLeaders.drop(1)
            LeaderboardList(leaders = remainingLeaders)
          }
        }
      }
}

@Composable
fun HighlightFirstPlace(
    viewModel: LeaderboardViewModel,
    leader: Pair<String, Long>,
    mode: LeaderboardMode,
    userIsKing: Boolean,
    onTogglePrize: () -> Unit
) {
  val (username, points) = leader

  val kingGif =
      when (mode) {
        LeaderboardMode.RAT -> R.drawable.king_rat
        LeaderboardMode.STINKY -> R.drawable.king_stinky
      }

  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    // Display the image
    AsyncImage(
        model = kingGif,
        contentDescription = "${mode.name} King GIF",
        modifier = Modifier.fillMaxWidth().height(200.dp),
    )

    // King info below the image
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "$username 👑",
        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface)
    Text(
        text = "${points.toInt()} points",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface)

    // Prize Button
    if (userIsKing) {
      Spacer(modifier = Modifier.height(16.dp))
      val buttonText = viewModel.buttonText.value
      Button(onClick = onTogglePrize) { Text(buttonText) }
    }
  }
}

@Composable
fun LeaderboardList(leaders: List<Pair<String, Long>>) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
    leaders.forEachIndexed { index, leader ->
      val rank = index + 2
      LeaderboardItem(rank = rank, memberId = leader.first, points = leader.second.toInt())
    }
  }
}

@Composable
fun LeaderboardItem(rank: Int, memberId: String, points: Int) {
  val emoji =
      when (rank) {
        2 -> "🥈"
        3 -> "🥉"
        else -> ""
      }

  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("$rank. ", style = MaterialTheme.typography.headlineSmall)
          Text("$memberId $emoji", style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            "$points", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      }
}

@Composable
fun ModeToggle(currentMode: LeaderboardMode, onModeChange: (LeaderboardMode) -> Unit) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = { onModeChange(LeaderboardMode.RAT) },
            colors =
                if (currentMode == LeaderboardMode.RAT) {
                  ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                } else {
                  ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
                },
            modifier = Modifier.weight(1f)) {
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
            modifier = Modifier.weight(1f)) {
              Text("Stinky Leaderboard")
            }
      }
}
