package com.android.shelfLife.ui.authentication

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.TopLevelDestinations
import com.android.shelfLife.viewmodel.authentication.SignInState
import com.android.shelfLife.viewmodel.authentication.SignInViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

/**
 * Composable function for the Sign-In screen.
 *
 * @param navigationActions Actions for navigating between screens.
 * @param signInViewModel ViewModel for handling sign-in logic.
 */
@Composable
fun SignInScreen(
    navigationActions: NavigationActions,
    signInViewModel: SignInViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val isUserLoggedIn by signInViewModel.isUserLoggedIn.collectAsState()
  val signInState by signInViewModel.signInState.collectAsState()
  val bypassLogin by signInViewModel.bypassLogin.collectAsState()

  Log.d("SignInScreen", "bypassLogin: $bypassLogin")
  LaunchedEffect(isUserLoggedIn) {
    if (isUserLoggedIn) {
      Log.d("SignInScreen", "User is already logged in, navigating to overview")
      navigationActions.navigateTo(Route.OVERVIEW)
    }
  }

  // Handle sign-in states
  LaunchedEffect(signInState) {
    Log.d("SignInScreen", "Sign-in state: $signInState")
    when (signInState) {
      is SignInState.Success -> {
        Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
        Log.d("SignInScreen", "Login successful!, navigating to overview")
        navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
      }
      is SignInState.Error -> {
        val message = (signInState as SignInState.Error).message
        Toast.makeText(context, "Login failed: $message", Toast.LENGTH_LONG).show()
      }
      is SignInState.Loading -> {
        // Do nothing
      }
      else -> {
        Log.e("SignInScreen", "Unexpected sign-in state: $signInState")
      }
    }
  }

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
        ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
          val account = task.getResult(Exception::class.java)!!
          account.idToken?.let { idToken -> signInViewModel.signInWithGoogle(idToken, context) }
              ?: run {
                Toast.makeText(context, "Failed to get ID Token!", Toast.LENGTH_LONG).show()
              }
        } catch (e: Exception) {
          Log.e("SignInScreen", "Google sign-in failed", e)
          Toast.makeText(context, "Google sign-in failed!", Toast.LENGTH_LONG).show()
        }
      }

  val token = stringResource(R.string.default_web_client_id)

  Scaffold(modifier = Modifier.fillMaxSize().testTag("signInScreen")) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Image(
              painter = painterResource(id = R.drawable.shelf_life_logo),
              contentDescription = "App Logo",
              modifier = Modifier.size(250.dp))

          Spacer(modifier = Modifier.height(16.dp))

          Text(
              text = stringResource(R.string.app_name),
              style =
                  MaterialTheme.typography.headlineLarge.copy(fontSize = 57.sp, lineHeight = 64.sp),
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("loginTitle"))

          Spacer(modifier = Modifier.height(48.dp))

          GoogleSignInButton(
              onSignInClick = {
                // We now officially got a backdoor in the code :)
                if (bypassLogin) {
                  signInViewModel.setIsUserLoggedInForTesting(true)
                } else {
                  val gso =
                      GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                          .requestIdToken(token)
                          .requestEmail()
                          .build()
                  val googleSignInClient = GoogleSignIn.getClient(context, gso)
                  launcher.launch(googleSignInClient.signInIntent)
                }
              },
              modifier = Modifier.testTag("loginButton"))

          if (signInState is SignInState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.testTag("signInLoadingIndicator"))
          }
        }
  }
}

/**
 * Composable function for the Google Sign-In button.
 *
 * @param onSignInClick Callback function to handle sign-in button click.
 * @param modifier Modifier for styling the button.
 */
@Composable
fun GoogleSignInButton(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier // Add modifier parameter
) {
  Button(
      onClick = onSignInClick,
      shape = RoundedCornerShape(50),
      border = BorderStroke(1.dp, Color.LightGray),
      modifier = modifier.padding(8.dp).height(48.dp).testTag("loginButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier = Modifier.size(30.dp).padding(end = 8.dp))
              Text(text = "Sign in with Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
      }
}
