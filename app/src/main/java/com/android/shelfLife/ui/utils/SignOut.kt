package com.android.shelfLife.ui.utils

import android.content.Context
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

fun signOutUser(context: Context, onSignOutComplete: () -> Unit) {
  val googleSignInClient: GoogleSignInClient =
      GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)

  googleSignInClient.revokeAccess().addOnCompleteListener { task ->
    if (task.isSuccessful) {
      FirebaseAuth.getInstance().signOut()
      onSignOutComplete()
    } else {
      Toast.makeText(context, "Sign-out failed: ${task.exception}", Toast.LENGTH_SHORT).show()
    }
  }
}
