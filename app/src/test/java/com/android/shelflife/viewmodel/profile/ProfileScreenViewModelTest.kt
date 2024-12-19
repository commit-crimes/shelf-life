package com.android.shelflife.viewmodel.profile

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.profile.ProfileScreenViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(sdk = [34])
class ProfileScreenViewModelTest {

  private lateinit var userRepository: UserRepository
  private lateinit var viewModel: ProfileScreenViewModel

  // We'll define these mocks once so they're reused across tests.
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockGoogleSignInClient: GoogleSignInClient
  private lateinit var mockToast: Toast

  @Before
  fun setup() {
    userRepository = mockk(relaxed = true)

    // Mock static classes
    mockkStatic(Log::class)
    mockkStatic(GoogleSignIn::class)
    mockkStatic(FirebaseAuth::class)
    mockkStatic(GoogleSignInOptions::class)
    mockkStatic(Toast::class)
    mockkStatic(TextUtils::class)

    // Default behavior for logging
    every { Log.d(any(), any()) } returns 0
    every { Log.e(any(), any(), any()) } returns 0

    // Avoid issues with TextUtils
    every { TextUtils.isEmpty(any()) } returns false

    // Mock the GoogleSignInClient
    mockGoogleSignInClient = mockk(relaxed = true)
    every { GoogleSignIn.getClient(any<Context>(), any<GoogleSignInOptions>()) } returns
        mockGoogleSignInClient

    // Mock FirebaseAuth
    mockAuth = mockk(relaxed = true)
    every { FirebaseAuth.getInstance() } returns mockAuth

    // Mock the task returned by revokeAccess
    val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
    every { mockGoogleSignInClient.revokeAccess() } returns mockTask

    // Mock Toast
    mockToast = mockk(relaxed = true)
    every { Toast.makeText(any(), any<String>(), any()) } returns mockToast

    // Mock user invitations flow
    val mockInvitationsFlow: StateFlow<List<String>> =
        MutableStateFlow(listOf("invite1", "invite2"))
    every { userRepository.invitations } returns mockInvitationsFlow

    // Initialize ViewModel
    viewModel = ProfileScreenViewModel(userRepository)
  }

  @After
  fun tearDown() {
    unmockkStatic(Log::class)
    unmockkStatic(GoogleSignIn::class)
    unmockkStatic(FirebaseAuth::class)
    unmockkStatic(GoogleSignInOptions::class)
    unmockkStatic(Toast::class)
    unmockkStatic(TextUtils::class)
  }

  @Test
  fun `initialization logs invitations`() {
    val expectedInvitations = listOf("invite1", "invite2")
    assertEquals(expectedInvitations, viewModel.invitationUIDS.value)
  }

  @Test
  fun `signOut calls GoogleSignInClient revokeAccess and FirebaseAuth signOut`() {
    val mockContext = mockk<Context>(relaxed = true)

    viewModel.signOut(mockContext)

    // Verify that signOut was called on the mockAuth instance (not on the static FirebaseAuth)
    verify { mockAuth.signOut() }

    // Verify that revokeAccess was called on the mockGoogleSignInClient
    verify { mockGoogleSignInClient.revokeAccess() }
  }
}
