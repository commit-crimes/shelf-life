package com.android.shelflife.viewmodel.leaderboard

import AudioPlayer
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.household.HouseHold
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.ui.leaderboard.ThemeManager
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardMode
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class LeaderboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LeaderboardViewModel
    private lateinit var houseHoldRepository: HouseHoldRepository
    private lateinit var userRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    private val selectedHousehold = MutableStateFlow<HouseHold?>(null)
    private val userFlow = MutableStateFlow<User?>(null)
    private val isAudioPlaying = MutableStateFlow(false)
    private val currentAudioMode = MutableStateFlow<LeaderboardMode?>(null)

    private lateinit var audioPlayerMock: MockedStatic<AudioPlayer>
    private lateinit var themeManagerMock: MockedStatic<ThemeManager>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        houseHoldRepository = mock(HouseHoldRepository::class.java)
        userRepository = mock(UserRepository::class.java)

        `when`(houseHoldRepository.selectedHousehold).thenReturn(selectedHousehold)
        `when`(userRepository.user).thenReturn(userFlow)
        `when`(userRepository.isAudioPlaying).thenReturn(isAudioPlaying)
        `when`(userRepository.currentAudioMode).thenReturn(currentAudioMode)

        audioPlayerMock = mockStatic(AudioPlayer::class.java)
        themeManagerMock = mockStatic(ThemeManager::class.java)

        // By default user is logged in and has an UID
        userFlow.value = User(uid = "currentUserId", username = "Current User", selectedHouseholdUID = "h1", email = "test@example.com")

        viewModel = LeaderboardViewModel(houseHoldRepository, userRepository)
    }

    @After
    fun tearDown() {
        audioPlayerMock.close()
        themeManagerMock.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is RAT mode and empty leaders`() = runTest {
        // No household = empty leaders
        advanceUntilIdle()
        assertEquals(LeaderboardMode.RAT, viewModel.mode.value)
        assertTrue(viewModel.topLeaders.value.isEmpty())
        assertEquals("Receive Your Prize", viewModel.buttonText.value)
    }

    @Test
    fun `calculateTopFive sets leaders and king correctly`() = runTest {
        // Setup household with RAT points
        val ratPoints = mapOf("user1" to 10L, "user2" to 5L, "user3" to 2L)
        val household = HouseHold("h1","Household", emptyList(), emptyList(), ratPoints, emptyMap())
        selectedHousehold.value = household

        // Mock userRepository.getUserNames
        `when`(userRepository.getUserNames(listOf("user1", "user2", "user3")))
            .thenReturn(mapOf("user1" to "User One", "user2" to "User Two"))

        advanceUntilIdle()

        val leaders = viewModel.topLeaders.value
        assertEquals(3, leaders.size)
        assertEquals("User One" to 10L, leaders[0]) // highest points first
        assertEquals("User Two" to 5L, leaders[1])
        // user3 not found by getUserNames => username = userId
        assertEquals("user3" to 2L, leaders[2])

        // King is user1
        assertEquals("user1", viewModel.kingUID)
    }

    @Test
    fun `switchMode updates mode and buttonText based on audio state`() = runTest {
        // Initially RAT mode, no audio
        assertEquals("Receive Your Prize", viewModel.buttonText.value)

        // Switch to STINKY mode
        viewModel.switchMode(LeaderboardMode.STINKY)
        advanceUntilIdle()
        assertEquals(LeaderboardMode.STINKY, viewModel.mode.value)
        // Audio not playing and currentAudioMode null => "Receive Your Prize"
        assertEquals("Receive Your Prize", viewModel.buttonText.value)

        // Simulate audio playing with STINKY mode
        isAudioPlaying.value = true
        currentAudioMode.value = LeaderboardMode.STINKY

        viewModel.switchMode(LeaderboardMode.STINKY)
        advanceUntilIdle()
        assertEquals("Stop", viewModel.buttonText.value)
    }

    @Test
    fun `togglePrize does nothing if user is not king`() = runTest {
        // Top leaders empty => no king
        viewModel.togglePrize(mock(Context::class.java), false)
        advanceUntilIdle()
        // No audio actions
        audioPlayerMock.verifyNoInteractions()
        themeManagerMock.verifyNoInteractions()
    }

    @Test
    fun `togglePrize starts audio and updates theme if user is king and no audio playing`() = runTest {
        // Make current user king
        val ratPoints = mapOf("currentUserId" to 20L, "user2" to 10L)
        val household = HouseHold("h1", "Household", emptyList(), emptyList(), ratPoints, emptyMap())
        `when`(userRepository.getUserNames(listOf("currentUserId","user2")))
            .thenReturn(mapOf("currentUserId" to "King", "user2" to "U2"))
        selectedHousehold.value = household

        advanceUntilIdle()

        val ctx = mock(Context::class.java)

        // Currently mode is RAT (default), no audio playing, user is king
        viewModel.togglePrize(ctx, isDarkMode = false)
        advanceUntilIdle()

        assertEquals("Stop", viewModel.buttonText.value)
        AudioPlayer.startRatAudio(ctx)
        ThemeManager.updateScheme(LeaderboardMode.RAT, false)
        verify(userRepository).setCurrentAudioMode(LeaderboardMode.RAT)
        verify(userRepository).setAudioPlaying(true)
    }

    @Test
    fun `togglePrize stops audio and resets theme if user is king and audio playing same mode`() = runTest {
        // Make current user king with RAT points
        val ratPoints = mapOf("currentUserId" to 20L)
        val household = HouseHold("h1","Household", emptyList(), emptyList(), ratPoints, emptyMap())
        `when`(userRepository.getUserNames(listOf("currentUserId")))
            .thenReturn(mapOf("currentUserId" to "King"))
        selectedHousehold.value = household
        advanceUntilIdle()

        // Simulate audio playing RAT
        isAudioPlaying.value = true
        currentAudioMode.value = LeaderboardMode.RAT
        viewModel.buttonText.value = "Stop"

        val ctx = mock(Context::class.java)
        viewModel.togglePrize(ctx, isDarkMode = true)
        advanceUntilIdle()
        AudioPlayer.stopAudio()
        ThemeManager.resetMode()
        verify(userRepository).setCurrentAudioMode(null)
        verify(userRepository).setAudioPlaying(false)
        assertEquals("Receive Your Prize", viewModel.buttonText.value)
    }

    @Test
    fun `togglePrize starts new mode audio if user is king but different audio mode was playing`() = runTest {
        // Make user king in STINKY mode scenario
        viewModel.switchMode(LeaderboardMode.STINKY)
        val stinkyPoints = mapOf("currentUserId" to 15L)
        val household = HouseHold("h1","Household",emptyList(), emptyList(), emptyMap(), stinkyPoints)
        `when`(userRepository.getUserNames(listOf("currentUserId")))
            .thenReturn(mapOf("currentUserId" to "King"))
        selectedHousehold.value = household
        advanceUntilIdle()

        // Audio is playing RAT mode currently
        isAudioPlaying.value = true
        currentAudioMode.value = LeaderboardMode.RAT

        val ctx = mock(Context::class.java)
        viewModel.togglePrize(ctx, isDarkMode = false)
        advanceUntilIdle()
        AudioPlayer.startStinkyAudio(ctx)
        ThemeManager.updateScheme(LeaderboardMode.STINKY, false)
        verify(userRepository).setCurrentAudioMode(LeaderboardMode.STINKY)
        verify(userRepository).setAudioPlaying(true)
        assertEquals("Stop", viewModel.buttonText.value)
    }

    @Test
    fun `no household means no leaders`() = runTest {
        selectedHousehold.value = null
        advanceUntilIdle()
        assertTrue(viewModel.topLeaders.value.isEmpty())
    }

    @Test
    fun `empty points means empty leaders`() = runTest {
        val household = HouseHold("h1","Household", emptyList(), emptyList(), emptyMap(), emptyMap())
        selectedHousehold.value = household
        advanceUntilIdle()
        assertTrue(viewModel.topLeaders.value.isEmpty())
        assertEquals(null, viewModel.kingUID)
    }

    @Test
    fun `if user name not found, userId is used`() = runTest {
        val ratPoints = mapOf("uX" to 50L)
        val household = HouseHold("h1","Household",emptyList(), emptyList(), ratPoints, emptyMap())
        `when`(userRepository.getUserNames(listOf("uX"))).thenReturn(emptyMap())

        selectedHousehold.value = household
        advanceUntilIdle()

        val leaders = viewModel.topLeaders.value
        assertEquals(1, leaders.size)
        assertEquals("uX" to 50L, leaders[0])
        assertEquals("uX", viewModel.kingUID)
    }

    @Test
    fun `refreshing topLeaders when household changes mode`() = runTest {
        val ratPoints = mapOf("u1" to 30L)
        val stinkyPoints = mapOf("u2" to 40L)
        val household = HouseHold("h1","Household", emptyList(), emptyList(), ratPoints, stinkyPoints)
        `when`(userRepository.getUserNames(listOf("u1"))).thenReturn(mapOf("u1" to "RatLeader"))
        `when`(userRepository.getUserNames(listOf("u2"))).thenReturn(mapOf("u2" to "StinkyLeader"))

        selectedHousehold.value = household
        advanceUntilIdle()

        // In RAT mode currently
        assertEquals(listOf("RatLeader" to 30L), viewModel.topLeaders.value)
        assertEquals("u1", viewModel.kingUID)

        // Switch to STINKY
        viewModel.switchMode(LeaderboardMode.STINKY)
        advanceUntilIdle()
        assertEquals(listOf("StinkyLeader" to 40L), viewModel.topLeaders.value)
        assertEquals("u2", viewModel.kingUID)
    }
}