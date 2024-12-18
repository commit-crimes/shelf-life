package helpers

import android.util.Log
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mockito.kotlin.whenever

class UserRepositoryTestHelper(private val userRepository: UserRepository) {
  private val user = MutableStateFlow<User?>(null)

  private val invitations = MutableStateFlow<List<String>>(emptyList())

  private val isAudioPlaying = MutableStateFlow<Boolean>(false)

  private val currentAudioMode = MutableStateFlow<LeaderboardMode?>(null)

  init {
    whenever(userRepository.user).thenReturn(user.asStateFlow())
    whenever(userRepository.invitations).thenReturn(invitations.asStateFlow())
    whenever(userRepository.isAudioPlaying).thenReturn(isAudioPlaying.asStateFlow())
    whenever(userRepository.currentAudioMode).thenReturn(currentAudioMode.asStateFlow())
    Log.d("UserRepositoryTestHelper", "UserRepositoryTestHelper initialized")
  }

  fun setUser(user: User?) {
    this.user.value = user
  }

  fun setInvitations(invitations: List<String>) {
    this.invitations.value = invitations
  }

  fun setIsAudioPlaying(isPlaying: Boolean) {
    this.isAudioPlaying.value = isPlaying
  }

  fun setCurrentAudioMode(mode: LeaderboardMode) {
    this.currentAudioMode.value = mode
  }
}
