package com.android.shelfLife.viewmodel.authentication

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.R
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newRecipe.RecipeRepository
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class SignInViewModel
@Inject
constructor(
  private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
  private val userRepository: UserRepository,
  private val householdRepository: HouseHoldRepository,
  private val recipeRepository: RecipeRepository,
  private val foodItemRepository: FoodItemRepository,
  @ApplicationContext private val appContext: Context

) : ViewModel() {

  private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
  val signInState: StateFlow<SignInState> = _signInState


  private val _isUserLoggedIn = MutableStateFlow(false)
  val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

  private val authStateListener =
      FirebaseAuth.AuthStateListener { auth ->
        Log.d("SignInViewModel", "AuthStateListener triggered, user: ${auth.currentUser}")
        if (auth.currentUser == null) {
          _isUserLoggedIn.value = false
        }
      }

  init {
    firebaseAuth.addAuthStateListener(authStateListener)
    if (firebaseAuth.currentUser != null) {
      viewModelScope.launch {
        populateModelData(appContext)
        _isUserLoggedIn.value = true
      }
    }
  }

  private suspend fun populateModelData(context: Context) {
    userRepository.initializeUserData(context)
    householdRepository.initializeHouseholds(
      userRepository.user.value?.householdUIDs ?: emptyList(),
      userRepository.user.value?.selectedHouseholdUID
    )
    userRepository.user.value?.selectedHouseholdUID?.let { foodItemRepository.getFoodItems(it) }
    userRepository.user.value?.let { recipeRepository.getRecipes(it.recipeUIDs) }
  }

  override fun onCleared() {
    Log.d("SignInViewModel", "onCleared called")
    super.onCleared()
    firebaseAuth.removeAuthStateListener(authStateListener)
  }



  fun signInWithGoogle(idToken: String, context: Context) {
    Log.d("SignInViewModel", "signInWithGoogle called")

    val credential = GoogleAuthProvider.getCredential(idToken, null)
    viewModelScope.launch {
      _signInState.value = SignInState.Loading
      try {
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        Log.d("SignInViewModel", "AuthResult : $authResult calling initializeUserData")
        viewModelScope.launch {
          populateModelData(context)
          _signInState.value = SignInState.Success(authResult)
        }
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
