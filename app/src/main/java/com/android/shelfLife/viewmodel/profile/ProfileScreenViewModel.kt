package com.android.shelfLife.viewmodel.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
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
 * ViewModel for managing the profile screen.
 *
 * @property userRepository Repository for accessing user data.
 */
@HiltViewModel
class ProfileScreenViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
  /** State for managing the visibility of the change theme menu. */
  var changeThemeMenuState = mutableStateOf(false)

  /** StateFlow containing the list of invitation UIDs. */
  val invitationUIDS: StateFlow<List<String>> = userRepository.invitations

  /** StateFlow containing the current user data. */
  val currentUser = userRepository.user

  /**
   * Signs out the user.
   *
   * @param context The context used to display Toast messages.
   */
  fun signOut(context: Context) {
    val googleSignInClient: GoogleSignInClient =
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
    FirebaseAuth.getInstance().signOut()

    googleSignInClient.revokeAccess().addOnCompleteListener { task: Task<Void> ->
      if (task.isSuccessful) {
        Toast.makeText(context, "Sign-out successful", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, "Sign-out failed : ${task.exception}", Toast.LENGTH_SHORT).show()
      }
    }
  }
}
