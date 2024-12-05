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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.shelfLife.R
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.authentication.SignInState
import com.android.shelfLife.viewmodel.authentication.SignInViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun SignInScreen(
    navigationActions: NavigationActions,
    signInViewModel: SignInViewModel
) {
  val context = LocalContext.current
  val signInState by signInViewModel.signInState.collectAsState()

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
        ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
          val account = task.getResult(Exception::class.java)
          account?.idToken?.let { idToken -> signInViewModel.signInWithGoogle(idToken) }
              ?: run {
                Toast.makeText(context, "Failed to get ID Token!", Toast.LENGTH_LONG).show()
              }
        } catch (e: Exception) {
          Log.e("SignInScreen", "Google sign-in failed", e)
          Toast.makeText(context, "Google sign-in failed!", Toast.LENGTH_LONG).show()
        }
      }

  val token = stringResource(R.string.default_web_client_id)

  // Handle sign-in states
  LaunchedEffect(signInState) {
    when (signInState) {
      is SignInState.Success -> {
        Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
        // Navigation is handled in ShelfLifeApp based on isUserLoggedIn
      }
      is SignInState.Error -> {
        val message = (signInState as SignInState.Error).message
        Toast.makeText(context, "Login failed: $message", Toast.LENGTH_LONG).show()
      }
      else -> {
        /* No action needed */
      }
    }
  }

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
              style = MaterialTheme.typography.headlineLarge.copy(fontSize = 57.sp),
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("loginTitle"))

          Spacer(modifier = Modifier.height(48.dp))
          GoogleSignInButton(
              onSignInClick = {
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
              },
              modifier = Modifier.testTag("loginButton"))

          if (signInState is SignInState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.testTag("signInLoadingIndicator"))
          }
        }
  }
}

@Composable
fun GoogleSignInButton(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier // Add modifier parameter
) {
  Button(
      onClick = onSignInClick,
      shape = RoundedCornerShape(50),
      border = BorderStroke(1.dp, Color.LightGray),
      modifier = modifier.padding(8.dp).height(48.dp)) {
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
