package com.android.shelfLife.viewmodel.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing the Profile Screen.
 *
 * This ViewModel handles:
 * - User sign-out functionality.
 * - Managing the theme menu state.
 * - Accessing user invitations and user details.
 *
 * @property invitationRepository The repository for managing user invitations.
 * @property userRepository The repository for managing user data.
 */
@HiltViewModel
class ProfileScreenViewModel
@Inject
constructor(
  private val invitationRepository: InvitationRepository,
  private val userRepository: UserRepository,
) : ViewModel() {

  /** The state of the theme change menu. */
  var changeThemeMenuState = mutableStateOf(false)

  /** A state flow containing the list of invitation UIDs for the current user. */
  val invitationUIDS: StateFlow<List<String>> = userRepository.invitations

  /** A state flow containing the current user's data. */
  val currentUser = userRepository.user

  /**
   * Initializes the ViewModel.
   *
   * This initializer logs the initial state of user invitations for debugging purposes.
   */
  init {
    Log.d("ProfileScreenViewModel", "init" + userRepository.invitations.value)
  }

  /**
   * Signs out the user and revokes their access using Google Sign-In.
   *
   * This method logs out the user from Firebase Authentication and revokes Google Sign-In access.
   * It provides feedback to the user via a toast message indicating the success or failure of the operation.
   *
   * @param context The context required to display toast messages and initialize GoogleSignInClient.
   */
  fun signOut(context: Context) {
    val googleSignInClient: GoogleSignInClient =
      GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)

    // Sign out from Firebase
    FirebaseAuth.getInstance().signOut()

    // Revoke access from Google Sign-In
    googleSignInClient.revokeAccess().addOnCompleteListener { task: Task<Void> ->
      if (task.isSuccessful) {
        Toast.makeText(context, "Sign-out successful", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, "Sign-out failed : ${task.exception}", Toast.LENGTH_SHORT).show()
      }
    }
  }
}