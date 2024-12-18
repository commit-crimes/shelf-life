package helpers

import android.util.Log
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardMode
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mockito.kotlin.whenever

class UserRepositoryTestHelper(private val userRepository: UserRepository) {
  private val user = MutableStateFlow<User?>(null)

  private val invitations = MutableStateFlow<List<String>>(emptyList())

  private val isAudioPlaying = MutableStateFlow<Boolean>(false)

  private val currentAudioMode = MutableStateFlow<LeaderboardMode?>(null)

  private var userEmails = emptyMap<String, String>()
  private var userIds = emptyMap<String, String>()

  init {
    every { userRepository.user } returns user.asStateFlow()
    every { userRepository.invitations } returns invitations.asStateFlow()
    every { userRepository.isAudioPlaying } returns isAudioPlaying.asStateFlow()
    every { userRepository.currentAudioMode } returns currentAudioMode.asStateFlow()
    coEvery { userRepository.getUserEmails(any()) } returns userEmails
    coEvery { userRepository.getUserIds(any()) } returns userIds
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

    fun setUserEmails(emails: Map<String, String>) {
        this.userEmails = emails
    }

    fun setUserIds(ids: Map<String, String>) {
        this.userIds = ids
    }
}
