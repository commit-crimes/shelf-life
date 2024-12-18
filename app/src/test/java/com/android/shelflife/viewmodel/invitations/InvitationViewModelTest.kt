package com.android.shelflife.viewmodel.invitations

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.User
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.invitations.InvitationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class InvitationViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  private lateinit var userRepository: UserRepository
  private lateinit var invitationRepository: InvitationRepository
  private lateinit var invitationViewModel: InvitationViewModel
  private lateinit var houseHoldRepository: HouseHoldRepository

  private val userInvitations = MutableStateFlow<List<String>>(emptyList())
  private val user = MutableStateFlow<User?>(null)

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    invitationRepository = mock(InvitationRepository::class.java)
    houseHoldRepository = mock(HouseHoldRepository::class.java)

    `when`(userRepository.invitations).thenReturn(userInvitations)
    `when`(userRepository.user).thenReturn(user)

    // Simulate the effect of deleteInvitationUID on userInvitations for suspend function
    doAnswer { invocation ->
          val uid = invocation.arguments[0] as String
          runBlocking { userInvitations.value = userInvitations.value.filter { it != uid } }
          null
        }
        .`when`(userRepository)
        .deleteInvitationUID(anyString())

    invitationViewModel = InvitationViewModel(invitationRepository, userRepository, houseHoldRepository)
  }

  @Test
  fun `init collects invitations from user repository`() = runTest {
    val invitationUIDs = listOf("invitation1", "invitation2")
    val invitations =
        listOf(
            Invitation("invitation1", "household1", "h1", "user1", "u1"),
            Invitation("invitation2", "household2", "h2", "user2", "u1"))
    `when`(invitationRepository.getInvitationsBatch(invitationUIDs)).thenReturn(invitations)
    userInvitations.value = invitationUIDs
    advanceUntilIdle()

    assertEquals(invitations, invitationViewModel.invitations.value)
  }

  @Test
  fun `acceptInvitation updates repositories and refreshes invitations`() = runTest {
    val invitation = Invitation("invitation1", "household1", "h1", "user1", "u1")
    val refreshedInvitations = listOf(Invitation("invitation2", "household2", "h2", "user2", "u1"))

    userInvitations.value = listOf("invitation1", "invitation2")
    `when`(invitationRepository.getInvitationsBatch(listOf("invitation2")))
        .thenReturn(refreshedInvitations)

    invitationViewModel.acceptInvitation(invitation)
    advanceUntilIdle()

    verify(userRepository).deleteInvitationUID("invitation1")
    verify(invitationRepository).acceptInvitation(invitation)
    verify(userRepository).addCurrentUserToHouseHold("household1", "user1")
    assertEquals(refreshedInvitations, invitationViewModel.invitations.value)
  }

  @Test
  fun `declineInvitation updates repositories and refreshes invitations`() = runTest {
    val invitation = Invitation("invitation1", "household1", "h1", "user1", "u1")
    val refreshedInvitations = listOf(Invitation("invitation2", "household2", "h2", "user2", "u1"))

    userInvitations.value = listOf("invitation1", "invitation2")
    `when`(invitationRepository.getInvitationsBatch(listOf("invitation2")))
        .thenReturn(refreshedInvitations)

    invitationViewModel.declineInvitation(invitation)
    advanceUntilIdle()

    verify(userRepository).deleteInvitationUID("invitation1")
    verify(invitationRepository).declineInvitation(invitation)
    assertEquals(refreshedInvitations, invitationViewModel.invitations.value)
  }

  @Test
  fun `refreshInvitations handles errors gracefully`() = runTest {
    val invitationUIDs = listOf("invitation1", "invitation2")
    userInvitations.value = invitationUIDs

    `when`(invitationRepository.getInvitationsBatch(invitationUIDs))
        .thenThrow(RuntimeException("Error fetching invitations"))

    invitationViewModel.refreshInvitations()
    advanceUntilIdle()

    // Should now be empty due to the catch block setting emptyList()
    assertEquals(emptyList<Invitation>(), invitationViewModel.invitations.value)
  }

  @Test
  fun `refreshInvitations updates invitations on success`() = runBlocking {
    val invitationUIDs = listOf("invitation1", "invitation2")
    val invitations =
        listOf(
            Invitation("invitation1", "household1", "h1", "user1", "u1"),
            Invitation("invitation2", "household2", "h2", "user2", "u1"))

    // Mock repository behavior
    userInvitations.value = invitationUIDs
    `when`(invitationRepository.getInvitationsBatch(invitationUIDs)).thenReturn(invitations)
    invitationViewModel.refreshInvitations()
    assertEquals(invitations, invitationViewModel.invitations.value)
  }
}
