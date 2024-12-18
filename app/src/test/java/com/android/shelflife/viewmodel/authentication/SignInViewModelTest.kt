package com.android.shelflife.viewmodel.authentication

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.authentication.SignInState
import com.android.shelfLife.viewmodel.authentication.SignInViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class)
class SignInViewModelTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val instantExecutorRule = InstantTaskExecutorRule()
  @Inject lateinit var firebaseAuth: FirebaseAuth
  @Inject lateinit var userRepository: UserRepository
  @Inject lateinit var householdRepository: HouseHoldRepository
  @Inject lateinit var recipeRepository: RecipeRepository
  @Inject lateinit var foodItemRepository: FoodItemRepository

  private lateinit var viewModel: SignInViewModel
  private lateinit var appContext: Context

  @Before
  fun setUp() {
    hiltRule.inject()
    appContext = ApplicationProvider.getApplicationContext()
    viewModel =
        SignInViewModel(
            firebaseAuth = firebaseAuth,
            userRepository = userRepository,
            householdRepository = householdRepository,
            recipeRepository = recipeRepository,
            foodItemRepository = foodItemRepository,
            appContext = appContext)
  }

  @Test
  fun initialSignInState_isIdle() = runTest {
    val initialState = viewModel.signInState.first()
    assertTrue(initialState is SignInState.Idle)
  }

  @Test
  fun userLoggedInOnInitializationIfAuthHasUser() = runTest {
    whenever(firebaseAuth.currentUser).thenReturn(null)
    val mockUser = mock(FirebaseUser::class.java)
    whenever(firebaseAuth.currentUser).thenReturn(mockUser)
    val loggedIn = viewModel.isUserLoggedIn.first()
    assertTrue(loggedIn)
  }

  @Test
  fun initialIsUserLoggedIn_DependsOnFirebaseAuthMock() = runTest {
    val isLoggedIn = viewModel.isUserLoggedIn.first()
    assertTrue(isLoggedIn)
  }

  @Test
  fun signInWithGoogle_successfulSignIn_updatesStateToSuccess() = runTest {
    val mockAuthResult = mock(AuthResult::class.java)
    whenever(firebaseAuth.signInWithCredential(any())).thenReturn(Tasks.forResult(mockAuthResult))

    viewModel.signInWithGoogle("mockIdToken", appContext)

    val state = viewModel.signInState.first { it !is SignInState.Idle }
    assertTrue(state is SignInState.Success)
    val isLoggedIn = viewModel.isUserLoggedIn.first()
    assertTrue(isLoggedIn)
  }

  @Test
  fun signInWithGoogle_failure_updatesStateToError() = runTest {
    whenever(firebaseAuth.signInWithCredential(any()))
        .thenReturn(Tasks.forException(Exception("Sign-in failed")))

    viewModel.signInWithGoogle("mockIdToken", appContext)

    val state = viewModel.signInState.first { it !is SignInState.Idle }
    assertTrue(state is SignInState.Error)
    state as SignInState.Error
    assertEquals("Sign-in failed", state.message)
  }

  @Test
  fun signInWithGoogle_unknownErrorMessage() = runTest {
    whenever(firebaseAuth.signInWithCredential(any()))
        .thenReturn(Tasks.forException(NullPointerException()))

    viewModel.signInWithGoogle("mockIdToken", appContext)

    val state = viewModel.signInState.first { it !is SignInState.Idle }
    assertTrue(state is SignInState.Error)
    state as SignInState.Error
    assertEquals("Unknown error occurred.", state.message)
  }

  @Test
  fun setSignInStateForTesting_coversAllStates() = runTest {
    viewModel.setSignInSuccessStateForTesting(SignInState.Loading)
    assertTrue(viewModel.signInState.first() is SignInState.Loading)

    val mockAuthResult = mock(AuthResult::class.java)
    viewModel.setSignInSuccessStateForTesting(SignInState.Success(mockAuthResult))
    val current = viewModel.signInState.first()
    assertTrue(current is SignInState.Success)

    viewModel.setSignInSuccessStateForTesting(SignInState.Error("Test error"))
    val errorState = viewModel.signInState.first()
    assertTrue(errorState is SignInState.Error)
    assertEquals("Test error", (errorState as SignInState.Error).message)

    viewModel.setSignInSuccessStateForTesting(SignInState.Idle)
    assertTrue(viewModel.signInState.first() is SignInState.Idle)
  }
}
