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
 * This screen allows the user to sign in using Google authentication and handles navigation
 * based on the user's authentication state.
 *
 * @param navigationActions Actions to handle navigation within the app.
 */
@Composable
fun SignInScreen(navigationActions: NavigationActions) {
    val context = LocalContext.current
    val signInViewModel = hiltViewModel<SignInViewModel>()
    val isUserLoggedIn by signInViewModel.isUserLoggedIn.collectAsState()
    val signInState by signInViewModel.signInState.collectAsState()

    // Navigate to the overview screen when the user is logged in.
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            navigationActions.navigateTo(Route.OVERVIEW)
        }
    }

    // Handle sign-in state changes.
    LaunchedEffect(signInState) {
        when (signInState) {
            is SignInState.Success -> {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
                Log.d("SignInScreen", "Login successful! Navigating to overview.")
                navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
            }
            is SignInState.Error -> {
                val message = (signInState as SignInState.Error).message
                Toast.makeText(context, "Login failed: $message", Toast.LENGTH_LONG).show()
            }
            is SignInState.Loading -> {
                // Do nothing during loading state.
            }
            else -> {
                Log.e("SignInScreen", "Unexpected sign-in state: $signInState")
            }
        }
    }

    // Google Sign-In launcher.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)!!
            account.idToken?.let { idToken ->
                signInViewModel.signInWithGoogle(idToken, context)
            } ?: run {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo.
            Image(
                painter = painterResource(id = R.drawable.shelf_life_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App title.
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 57.sp, lineHeight = 64.sp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("loginTitle")
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Google Sign-In button.
            GoogleSignInButton(
                onSignInClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.testTag("loginButton")
            )

            // Show a loading indicator during sign-in.
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
 * @param onSignInClick Callback function triggered when the button is clicked.
 * @param modifier Modifier for customizing the button layout and styling.
 */
@Composable
fun GoogleSignInButton(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onSignInClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = modifier
            .padding(8.dp)
            .height(48.dp)
            .testTag("loginButton")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Google logo.
            Image(
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 8.dp)
            )

            // Button text.
            Text(text = "Sign in with Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}