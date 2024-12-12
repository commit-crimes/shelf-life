package com.android.shelfLife.viewmodel.leaderboard

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LeaderboardMode {
    RAT,
    STINKY
}

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
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
    private var isAudioPlaying = false
    private var currentAudioMode: LeaderboardMode? = null

    val buttonText = mutableStateOf( "Receive Your Prize")

    init {
        viewModelScope.launch {
            combine(selectedHousehold, mode) { household, currentMode ->
                if (household == null) {
                    emptyList<Pair<String, Long>>()
                } else {
                    calculateTopFive(household, currentMode)
                }
            }.collect {
                _topLeaders.value = it
            }
        }
    }

    fun togglePrize(context: Context) {
        // Check if user is king
        val leaders = _topLeaders.value
        val userIsKing = leaders.isNotEmpty() && kingUID == currentUserId

        if (!userIsKing) return // Not king, do nothing

        val currentMode = _mode.value
        if (!isAudioPlaying || currentAudioMode != currentMode) {
            // Start audio based on the current mode
            buttonText.value = "Stop"
            when (currentMode) {
                LeaderboardMode.RAT -> AudioPlayer.startRatAudio(context)
                LeaderboardMode.STINKY -> AudioPlayer.startStinkyAudio(context)
            }
            isAudioPlaying = true
            currentAudioMode = currentMode
        } else {
            buttonText.value = "Receive Your Prize"
            // Audio is playing, stop it
            AudioPlayer.stopAudio()
            isAudioPlaying = false
            currentAudioMode = null
        }
    }

    private suspend fun calculateTopFive(household: HouseHold, currentMode: LeaderboardMode): List<Pair<String, Long>> {
        val pointsMap = when (currentMode) {
            LeaderboardMode.RAT -> household.ratPoints
            LeaderboardMode.STINKY -> household.stinkyPoints
        }

        val sortedLeaders = pointsMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        if (sortedLeaders.isEmpty()) return emptyList()

        kingUID = sortedLeaders.first().first

        val userIds = sortedLeaders.map { it.first }
        val userIdToName = userRepository.getUserNames(userIds)

        return sortedLeaders.map { (userId, points) ->
            val username = userIdToName[userId] ?: userId
            username to points
        }
    }

    fun switchMode(newMode: LeaderboardMode) {
        // Switching mode does not affect currently playing audio
        // If audio is playing RAT and user switches to STINKY, audio remains RAT until user toggles off and on again.
        buttonText.value = if(currentAudioMode != newMode) "Receive Your Prize" else "Stop"
        _mode.value = newMode
    }

}
