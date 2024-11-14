package com.android.shelfLife.ui.profile

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.compose.LocalThemeMode
import com.example.compose.LocalThemeToggler
import com.example.compose.LocalThemeTogglerProvider
import com.example.compose.ThemeMode
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

  var expanded by remember { mutableStateOf(false) }

  // Get the current theme mode and the theme toggler from ShelfLifeTheme
  val currentThemeMode = LocalThemeMode.current
  val themeToggler = LocalThemeTogglerProvider.current
  val themeModeLabel = when (currentThemeMode) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM_DEFAULT -> "System Default"
  }

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
                  text = currentAccount?.displayName ?: "Guest",
                  fontWeight = FontWeight.Bold,
                  fontSize = 28.sp,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag("profileNameText"))

              if (currentAccount?.email != null) {
                Text(
                    text = currentAccount.email!!,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("profileEmailText"))
              }



          //
          Spacer(modifier = Modifier.weight(0.2f))



          Text("App Theme:")

          // Dropdown menu
          // Dropdown menu
          Box {
            // Dropdown menu trigger
            Text(
              text = themeModeLabel,
              modifier = Modifier
                .padding(8.dp)
                .clickable { expanded = true }
                .background(Color.LightGray)
                .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Dropdown menu content
            DropdownMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("Light") },
                onClick = {
                  themeToggler.toggleTheme(ThemeMode.LIGHT)
                  expanded = false
                }
              )
              DropdownMenuItem(
                text = { Text("Dark") },
                onClick = {
                  themeToggler.toggleTheme(ThemeMode.DARK)
                  expanded = false
                }
              )
              DropdownMenuItem(
                text = { Text("System Default") },
                onClick = {
                  themeToggler.toggleTheme(ThemeMode.SYSTEM_DEFAULT)
                  expanded = false
                }
              )
            }
          }
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
