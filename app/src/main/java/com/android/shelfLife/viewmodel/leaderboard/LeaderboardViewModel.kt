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
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Enum class representing the different leaderboard modes. */
enum class LeaderboardMode {
  RAT,
  STINKY
}

/**
 * ViewModel for managing the leaderboard.
 *
 * @property houseHoldRepository Repository for accessing household data.
 * @property userRepository Repository for accessing user data.
 */
@HiltViewModel
class LeaderboardViewModel
@Inject
constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val userRepository: UserRepository
) : ViewModel() {

  private val _mode = MutableStateFlow(LeaderboardMode.RAT)
  val mode: StateFlow<LeaderboardMode> = _mode.asStateFlow()

  val selectedHousehold = houseHoldRepository.selectedHousehold

  val currentUserId: String? = userRepository.user.value?.uid

  // Top leaders state
  private val _topLeaders = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
  val topLeaders: StateFlow<List<Pair<String, Long>>> = _topLeaders.asStateFlow()

  var kingUID = _topLeaders.value.firstOrNull()?.first

  // Audio playing state
  private var isAudioPlaying = userRepository.isAudioPlaying
  private var currentAudioMode = userRepository.currentAudioMode

  val buttonText = mutableStateOf("Receive Your Prize")

  init {
    buttonText.value =
        if (!isAudioPlaying.value || currentAudioMode.value != mode.value) "Receive Your Prize"
        else "Stop"
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
   * Toggles the prize for the current user.
   *
   * @param context The context used for playing audio.
   * @param isDarkMode Boolean indicating if the dark theme should be used.
   */
  fun togglePrize(context: Context, isDarkMode: Boolean) {
    // Check if user is king
    val leaders = _topLeaders.value
    val userIsKing = leaders.isNotEmpty() && kingUID == currentUserId

    if (!userIsKing) return // Not king, do nothing

    if (!isAudioPlaying.value || currentAudioMode.value != mode.value) {
      // Start audio and change theme
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
      // Stop audio and revert theme
      buttonText.value = "Receive Your Prize"
      AudioPlayer.stopAudio()
      ThemeManager.resetMode()
      userRepository.setCurrentAudioMode(null)
      userRepository.setAudioPlaying(false)
    }
  }

  /**
   * Calculates the top five leaders based on the current mode.
   *
   * @param household The household data.
   * @param currentMode The current leaderboard mode.
   * @return A list of pairs containing user IDs and their points.
   */
  private suspend fun calculateTopFive(
      household: HouseHold,
      currentMode: LeaderboardMode
  ): List<Pair<String, Long>> {
    val pointsMap =
        when (currentMode) {
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
   * Switches the leaderboard mode.
   *
   * @param newMode The new leaderboard mode to switch to.
   */
  fun switchMode(newMode: LeaderboardMode) {
    // Switching mode does not affect currently playing audio
    // If audio is playing RAT and user switches to STINKY, audio remains RAT until user toggles off
    // and on again.
    buttonText.value = if (currentAudioMode.value != newMode) "Receive Your Prize" else "Stop"
    _mode.value = newMode
  }
}
