package com.android.shelfLife.viewmodel

import android.content.Context
import android.widget.Toast
import com.android.shelfLife.model.newInvitations.Invitation
import com.android.shelfLife.model.newInvitations.InvitationRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.StateFlow

class ProfileScreenViewModel (private val invitationRepository: InvitationRepository,
                              private val userRepository : UserRepository,
                              private val context: Context) {

    val invitations : StateFlow<List<Invitation>> = invitationRepository.invitations
    val currentUser = userRepository.user

    /**
     * Signs out the user.
     */
    fun signOut() {
        val googleSignInClient: GoogleSignInClient =
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
        FirebaseAuth.getInstance().signOut()

        googleSignInClient.signOut().addOnCompleteListener { task: Task<Void> ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Sign-out successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Sign-out failed : ${task.exception}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}