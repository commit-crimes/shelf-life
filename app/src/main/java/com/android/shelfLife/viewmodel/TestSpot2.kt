package com.android.shelfLife.ui.authentication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom

@Preview
@Composable
fun TestSpot2() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Spotify OAuth Parameters
    val clientId = "e708cc40417e45eca80f877d8fdb4897"
    val redirectUri = "https://wesh.com/spotify"
    val scopes = "user-read-private user-read-email playlist-read-private"

    // Generate PKCE values
    val codeVerifier = generateCodeVerifier()
    val codeChallenge = generateCodeChallenge(codeVerifier)
    val state = generateRandomState()

    // Build authorization URL
    val authUrl = buildAuthorizationUrl(
        clientId = clientId,
        redirectUri = redirectUri,
        codeChallenge = codeChallenge,
        state = state,
        scopes = scopes
    )
    Log.d("SpotifyAuth", "Auth URL: $authUrl")

    // Launcher for handling the OAuth redirect
    val launcher: ManagedActivityResultLauncher<Intent, ActivityResult> =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->val intent = result.data
            Log.d("SpotifyAuth", "Received Intent: $intent")
            val data = intent?.data
            Log.d("SpotifyAuth", "Redirect URI: $data")

            try {
                val code = handleSpotifyRedirect(data, state)
                if (code != null) {
                    coroutineScope.launch {
                        val tokenResponse = fetchAccessToken(
                            code = code,
                            redirectUri = redirectUri,
                            clientId = clientId,
                            codeVerifier = codeVerifier
                        )
                        tokenResponse?.let { json ->
                            val accessToken = json.getString("access_token")
                            fetchUserPlaylists(accessToken, context)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SpotifyAuth", "Error handling redirect: ${e.localizedMessage}")
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    val intent = CustomTabsIntent.Builder().build().intent.apply {
                        data = Uri.parse(authUrl)
                    }
                    launcher.launch(intent)
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text(
                    text = "Sign in with Spotify",
                    fontSize = 18.sp
                )
            }
        }
    }
}

fun generateCodeVerifier(): String {
    val random = SecureRandom()
    val codeVerifier = ByteArray(64)
    random.nextBytes(codeVerifier)
    return Base64.encodeToString(codeVerifier, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

fun generateCodeChallenge(codeVerifier: String): String {
    val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

fun generateRandomState(): String {
    val random = SecureRandom()
    val state = ByteArray(16)
    random.nextBytes(state)
    return Base64.encodeToString(state, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

fun buildAuthorizationUrl(
    clientId: String,
    redirectUri: String,
    codeChallenge: String,
    state: String,
    scopes: String
): String {

    return Uri.Builder()
        .scheme("https")
        .authority("accounts.spotify.com")
        .path("authorize")
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("response_type", "code")
        .appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("code_challenge_method", "S256")
        .appendQueryParameter("code_challenge", codeChallenge)
        .appendQueryParameter("state", state)
        .appendQueryParameter("scope", scopes)
        .build()
        .toString()
}

fun handleSpotifyRedirect(data: Uri?, storedState: String): String? {
    val code = data?.getQueryParameter("code")
    val state = data?.getQueryParameter("state")
    if (state != storedState)
    {
        Log.e("SpotifyAuth", "State mismatch! Sent: $storedState, Received: $state")
        throw IllegalStateException("State mismatch!")
    }
    return code
}

suspend fun fetchAccessToken(
    code: String,
    redirectUri: String,
    clientId: String,
    codeVerifier: String
): JSONObject? {
    val tokenEndpoint = "https://accounts.spotify.com/api/token"
    val client = OkHttpClient()

    val formBody = FormBody.Builder()
        .add("grant_type", "authorization_code")
        .add("code", code)
        .add("redirect_uri", redirectUri)
        .add("client_id", clientId)
        .add("code_verifier", codeVerifier)
        .build()

    val request = Request.Builder()
        .url(tokenEndpoint)
        .post(formBody)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build()

    return try {
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                JSONObject(responseData ?: "{}")
            } else {
                Log.e("SpotifyAuth", "Token request failed with code: ${response.code}")
                null
            }
        }
    } catch (e: IOException) {
        Log.e("SpotifyAuth", "Error fetching access token: ${e.localizedMessage}")
        null
    }
}

fun fetchUserPlaylists(accessToken: String, context: Context) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.spotify.com/v1/me/playlists")
        .addHeader("Authorization", "Bearer $accessToken")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("SpotifyAPI", "Failed to fetch playlists: ${e.localizedMessage}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                Log.d("SpotifyAPI", "Playlists: $responseData")
            } else {
                Log.e("SpotifyAPI", "Playlist request failed with code: ${response.code}")
            }
        }
    })
}
