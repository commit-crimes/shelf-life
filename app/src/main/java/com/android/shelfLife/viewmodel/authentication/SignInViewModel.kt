package com.android.shelfLife.viewmodel.authentication

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.user.UserRepository
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel for handling user authentication and managing sign-in state.
 *
 * This ViewModel integrates with Firebase Authentication and Google Sign-In to manage user login.
 * It initializes user data and updates repositories after successful sign-in.
 *
 * @param firebaseAuth Instance of FirebaseAuth for handling authentication.
 * @param userRepository Repository for managing user data.
 * @param householdRepository Repository for managing household data.
 * @param recipeRepository Repository for managing recipe data.
 * @param foodItemRepository Repository for managing food items.
 * @param appContext Application context for initializing data.
 */
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

    // StateFlow for tracking the sign-in process state
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
    val signInState: StateFlow<SignInState> = _signInState

    // StateFlow for tracking whether the user is logged in
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    // Firebase AuthStateListener to monitor changes in user authentication state
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        Log.d("SignInViewModel", "AuthStateListener triggered, user: ${auth.currentUser}")
        if (auth.currentUser == null) {
            _isUserLoggedIn.value = false
        }
    }

    init {
        // Add the auth state listener during initialization
        firebaseAuth.addAuthStateListener(authStateListener)

        // If the user is already signed in, populate model data
        if (firebaseAuth.currentUser != null) {
            viewModelScope.launch {
                populateModelData(appContext)
                _isUserLoggedIn.value = true
            }
        }
    }

    /**
     * Initializes user data and populates repositories with the latest information.
     *
     * @param context The application context required for initialization.
     */
    private suspend fun populateModelData(context: Context) {
        userRepository.initializeUserData(context)
        householdRepository.initializeHouseholds(
            userRepository.user.value?.householdUIDs ?: emptyList(),
            userRepository.user.value?.selectedHouseholdUID
        )
        userRepository.user.value?.selectedHouseholdUID?.let { foodItemRepository.getFoodItems(it) }
        userRepository.user.value?.let { recipeRepository.initializeRecipes(it.recipeUIDs, null) }
    }

    /**
     * Removes the auth state listener when the ViewModel is cleared.
     */
    override fun onCleared() {
        Log.d("SignInViewModel", "onCleared called")
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    /**
     * Handles sign-in with Google credentials.
     *
     * @param idToken The ID token retrieved from Google Sign-In.
     * @param context The application context required for initializing data.
     */
    fun signInWithGoogle(idToken: String, context: Context) {
        Log.d("SignInViewModel", "signInWithGoogle called")
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        viewModelScope.launch {
            _signInState.value = SignInState.Loading
            try {
                // Authenticate with Firebase using Google credentials
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                Log.d("SignInViewModel", "AuthResult : $authResult calling initializeUserData")

                // Populate repositories with user data after successful sign-in
                viewModelScope.launch {
                    populateModelData(context)
                    _signInState.value = SignInState.Success(authResult)
                }
            } catch (e: Exception) {
                // Handle errors during the sign-in process
                _signInState.value = SignInState.Error(e.message ?: "Unknown error occurred.")
            }
        }
    }
}

/**
 * Represents the different states of the sign-in process.
 */
sealed class SignInState {
    /**
     * Idle state when no sign-in process is ongoing.
     */
    object Idle : SignInState()

    /**
     * Loading state when the sign-in process is in progress.
     */
    object Loading : SignInState()

    /**
     * Success state after a successful sign-in.
     *
     * @param authResult The result of the Firebase authentication process.
     */
    data class Success(val authResult: AuthResult) : SignInState()

    /**
     * Error state when the sign-in process fails.
     *
     * @param message A message describing the error.
     */
    data class Error(val message: String) : SignInState()
}