package com.android.shelfLife.viewmodel.leaderboard

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.leaderboard.ThemeManager
import com.example.compose.LocalThemeMode
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
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _mode = savedStateHandle.getLiveData("mode", LeaderboardMode.RAT)
    val mode: StateFlow<LeaderboardMode> = _mode.asFlow().stateIn(
        viewModelScope, SharingStarted.Eagerly, LeaderboardMode.RAT
    )

    val selectedHousehold = houseHoldRepository.selectedHousehold
    val currentUserId: String? = userRepository.user.value?.uid

    private val _topLeaders = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
    val topLeaders: StateFlow<List<Pair<String, Long>>> = _topLeaders.asStateFlow()

    // State variables stored in SavedStateHandle
    var isAudioPlaying: Boolean
        get() = savedStateHandle.get("isAudioPlaying") ?: false
        set(value) = savedStateHandle.set("isAudioPlaying", value)

    var currentAudioMode: LeaderboardMode?
        get() = savedStateHandle.get("currentAudioMode")
        set(value) = savedStateHandle.set("currentAudioMode", value)

    var buttonText: String
        get() = savedStateHandle.get("buttonText") ?: "Receive Your Prize"
        set(value) = savedStateHandle.set("buttonText", value)


    var kingUID = _topLeaders.value.firstOrNull()?.first


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

    fun togglePrize(context: Context, isDarkMode: Boolean) {
        // Check if user is king
        val leaders = _topLeaders.value
        val userIsKing = leaders.isNotEmpty() && kingUID == currentUserId

        if (!userIsKing) return // Not king, do nothing

        if (!isAudioPlaying || currentAudioMode != mode.value) {
            // Start audio and change theme
            buttonText = "Stop"
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
            currentAudioMode = mode.value
            isAudioPlaying = true
        } else {
            // Stop audio and revert theme
            buttonText = "Receive Your Prize"
            AudioPlayer.stopAudio()
            ThemeManager.resetMode()
            isAudioPlaying = false
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
        buttonText = if(currentAudioMode != newMode) "Receive Your Prize" else "Stop"
        _mode.value = newMode
    }

}
