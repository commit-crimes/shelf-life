// SignInViewModel.kt
package com.android.shelfLife.viewmodel.authentication

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

  private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
  val signInState: StateFlow<SignInState> = _signInState

  private val _isUserLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
  val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

  private val authStateListener =
      FirebaseAuth.AuthStateListener { auth -> _isUserLoggedIn.value = auth.currentUser != null }

  init {
    firebaseAuth.addAuthStateListener(authStateListener)
  }

  override fun onCleared() {
    super.onCleared()
    firebaseAuth.removeAuthStateListener(authStateListener)
  }

  fun signInWithGoogle(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    viewModelScope.launch {
      _signInState.value = SignInState.Loading
      try {
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val user = authResult.user
        user?.let {
          val userDoc = firestore.collection("users").document(it.uid)
          val userData = mapOf("email" to it.email, "name" to it.displayName)
          userDoc.set(userData, SetOptions.merge()).await()
          _signInState.value = SignInState.Success(authResult)
        } ?: run { _signInState.value = SignInState.Error("User is null after sign-in.") }
      } catch (e: Exception) {
        _signInState.value = SignInState.Error(e.message ?: "Unknown error occurred.")
      }
    }
  }

  fun signOutUser(context: Context, onSignOutComplete: () -> Unit) {
    val googleSignInClient =
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build())

    googleSignInClient.signOut().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        firebaseAuth.signOut()
        onSignOutComplete()
      } else {
        Toast.makeText(context, "Sign-out failed: ${task.exception}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun setSignInStateForTesting(state: SignInState) {
    _signInState.value = state
  }
}

sealed class SignInState {
  object Idle : SignInState()

  object Loading : SignInState()

  data class Success(val authResult: AuthResult) : SignInState()

  data class Error(val message: String) : SignInState()
}
