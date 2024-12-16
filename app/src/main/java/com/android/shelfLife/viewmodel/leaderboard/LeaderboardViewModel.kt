package com.android.shelfLife.viewmodel.leaderboard

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.leaderboard.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enum class representing the leaderboard modes.
 * - `RAT`: Leaderboard for rat-related points.
 * - `STINKY`: Leaderboard for stinky-related points.
 */
enum class LeaderboardMode {
  RAT,
  STINKY
}

/**
 * ViewModel for managing leaderboard functionality.
 *
 * Handles leaderboard modes, top leader calculations, audio playback control,
 * and theme switching based on the leaderboard mode.
 *
 * @param houseHoldRepository Repository for managing household-related data.
 * @param userRepository Repository for managing user-related data.
 */
@HiltViewModel
class LeaderboardViewModel
@Inject
constructor(
  private val houseHoldRepository: HouseHoldRepository,
  private val userRepository: UserRepository
) : ViewModel() {

  // StateFlow for tracking the current leaderboard mode (RAT or STINKY)
  private val _mode = MutableStateFlow(LeaderboardMode.RAT)
  val mode: StateFlow<LeaderboardMode> = _mode.asStateFlow()

  // Observes the selected household
  val selectedHousehold = houseHoldRepository.selectedHousehold

  // Retrieves the current user's ID
  val currentUserId: String? = userRepository.user.value?.uid

  // Top leaders in the leaderboard (sorted by points)
  private val _topLeaders = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
  val topLeaders: StateFlow<List<Pair<String, Long>>> = _topLeaders.asStateFlow()

  // UID of the current king (leader of the leaderboard)
  var kingUID = _topLeaders.value.firstOrNull()?.first

  // Observes audio playing state and current audio mode
  private var isAudioPlaying = userRepository.isAudioPlaying
  private var currentAudioMode = userRepository.currentAudioMode

  // Button text for toggling audio playback
  val buttonText = mutableStateOf("Receive Your Prize")

  init {
    // Initialize button text based on audio state and mode
    buttonText.value =
      if (!isAudioPlaying.value || currentAudioMode.value != mode.value) "Receive Your Prize"
      else "Stop"

    // Combine household data and leaderboard mode to calculate top leaders
    viewModelScope.launch {
      combine(selectedHousehold, mode) { household, currentMode ->
        if (household == null) {
          emptyList<Pair<String, Long>>()
        } else {
          calculateTopFive(household, currentMode)
        }
      }
        .collect { _topLeaders.value = it }
    }
  }

  /**
   * Toggles the "prize" functionality (audio playback and theme switching).
   *
   * If the user is the king, this toggles the audio and switches the theme.
   * Otherwise, it does nothing.
   *
   * @param context The application context required for audio playback.
   * @param isDarkMode Indicates whether the app is in dark mode for theme adjustments.
   */
  fun togglePrize(context: Context, isDarkMode: Boolean) {
    val leaders = _topLeaders.value
    val userIsKing = leaders.isNotEmpty() && kingUID == currentUserId

    // Only the king can toggle the prize
    if (!userIsKing) return

    if (!isAudioPlaying.value || currentAudioMode.value != mode.value) {
      // Start audio and apply the corresponding theme
      buttonText.value = "Stop"
      when (mode.value) {
        LeaderboardMode.RAT -> {
          AudioPlayer.startRatAudio(context)
          ThemeManager.updateScheme(LeaderboardMode.RAT, isDarkMode)
        }
        LeaderboardMode.STINKY -> {
          AudioPlayer.startStinkyAudio(context)
          ThemeManager.updateScheme(LeaderboardMode.STINKY, isDarkMode)
        }
      }
      userRepository.setCurrentAudioMode(mode.value)
      userRepository.setAudioPlaying(true)
    } else {
      // Stop audio and reset the theme
      buttonText.value = "Receive Your Prize"
      AudioPlayer.stopAudio()
      ThemeManager.resetMode()
      userRepository.setCurrentAudioMode(null)
      userRepository.setAudioPlaying(false)
    }
  }

  /**
   * Calculates the top five leaders in the leaderboard based on the current mode.
   *
   * Fetches the user names for the top five user IDs and returns a sorted list of pairs.
   *
   * @param household The household containing leaderboard data.
   * @param currentMode The current leaderboard mode (RAT or STINKY).
   * @return A list of pairs representing user names and their points.
   */
  private suspend fun calculateTopFive(
    household: HouseHold,
    currentMode: LeaderboardMode
  ): List<Pair<String, Long>> {
    val pointsMap = when (currentMode) {
      LeaderboardMode.RAT -> household.ratPoints
      LeaderboardMode.STINKY -> household.stinkyPoints
    }

    val sortedLeaders =
      pointsMap.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }

    if (sortedLeaders.isEmpty()) return emptyList()

    kingUID = sortedLeaders.first().first

    val userIds = sortedLeaders.map { it.first }
    val userIdToName = userRepository.getUserNames(userIds)

    return sortedLeaders.map { (userId, points) ->
      val username = userIdToName[userId] ?: userId
      username to points
    }
  }

  /**
   * Switches the leaderboard mode (RAT or STINKY).
   *
   * Switching the mode does not affect currently playing audio.
   * The audio remains associated with the previous mode until toggled off and on again.
   *
   * @param newMode The new leaderboard mode to switch to.
   */
  fun switchMode(newMode: LeaderboardMode) {
    buttonText.value = if (currentAudioMode.value != newMode) "Receive Your Prize" else "Stop"
    _mode.value = newMode
  }
}