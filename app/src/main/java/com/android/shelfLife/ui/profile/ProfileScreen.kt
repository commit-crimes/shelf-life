package com.android.shelfLife.ui.profile

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

@Composable
fun ProfileScreen(
    navigationActions: NavigationActions,
    account: GoogleSignInAccount? = null,
    signOutUser: () -> Unit = {}
) {
  val context = LocalContext.current
  val currentAccount = remember { account ?: getGoogleAccount(context) }

  Scaffold(
      modifier = Modifier.testTag("profileScaffold"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { destination -> navigationActions.navigateTo(destination) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .testTag("profileColumn"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              // Profile picture using Coil to load the image from a URL
              val profileImageUrl = currentAccount?.photoUrl?.toString()
              if (profileImageUrl == null) {
                Image(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp).clip(CircleShape).testTag("profilePicture"),
                    contentScale = ContentScale.Crop)
              } else {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp).clip(CircleShape).testTag("profilePicture"),
                    contentScale = ContentScale.Crop)
              }
              Spacer(modifier = Modifier.height(32.dp))
              Text(
                  text = "Hello, ${currentAccount?.email ?: "Guest"}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 28.sp,
                  textAlign = TextAlign.Left,
                  modifier = Modifier.testTag("greetingText"))

              Spacer(modifier = Modifier.weight(1f))

              // Logout button
              OutlinedButton(
                  onClick = {
                    // Sign out the user
                    signOutUser()
                  },
                  modifier = Modifier.fillMaxWidth().testTag("logoutButton"),
                  border = BorderStroke(1.dp, Color.Red) // Outline color matches the current status
                  ) {
                    Text(text = "Log out", color = Color.Red)
                  }
            }
      }
}

// Function to get the signed-in Google account
fun getGoogleAccount(context: Context): GoogleSignInAccount? {
  return GoogleSignIn.getLastSignedInAccount(context)
}
