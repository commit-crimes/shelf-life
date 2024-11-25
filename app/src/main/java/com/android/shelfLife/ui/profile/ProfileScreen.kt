package com.android.shelfLife.ui.profile

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.android.shelfLife.model.invitations.InvitationViewModel
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.utils.DropdownFields
import com.example.compose.LocalThemeMode
import com.example.compose.LocalThemeTogglerProvider
import com.example.compose.ThemeMode
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

@Composable
fun ProfileScreen(
    navigationActions: NavigationActions,
    account: GoogleSignInAccount? = null,
    signOutUser: () -> Unit = {},
    invitationViewModel: InvitationViewModel
) {
  val context = LocalContext.current
  val currentAccount = remember { account ?: getGoogleAccount(context) }

  val invitations by invitationViewModel.invitations.collectAsState()

  var expanded by remember { mutableStateOf(false) }

  // Get the current theme mode and the theme toggler from ShelfLifeTheme
  val currentThemeMode = LocalThemeMode.current
  val themeToggler = LocalThemeTogglerProvider.current

  val options = arrayOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM_DEFAULT)
  val optionLabels =
      mapOf(
          ThemeMode.LIGHT to "Light",
          ThemeMode.DARK to "Dark",
          ThemeMode.SYSTEM_DEFAULT to "System Default")

  val color = MaterialTheme.colorScheme

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
                  color = color.primary,
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

              Spacer(modifier = Modifier.weight(0.2f))

              ////
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(16.dp)) {
                    DropdownFields(
                        label = "App Theme",
                        options = options,
                        selectedOption = currentThemeMode,
                        onOptionSelected = { selectedOption ->
                          themeToggler.toggleTheme(selectedOption)
                          navigationActions.navigateToAndClearBackStack(Route.PROFILE)
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        optionLabel = { option ->
                          (optionLabels[option] ?: "System Default") + " Mode"
                        },
                        modifier = Modifier.padding(horizontal = 16.dp))
                  }

              Spacer(modifier = Modifier.height(16.dp))

              if (invitations.isNotEmpty()) {
                Text(
                    text = "Pending Invitations",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Start))
                Button(
                    onClick = { navigationActions.navigateTo(Route.INVITATIONS) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                      Text("You have ${invitations.size} pending invitations")
                    }
              }

              Spacer(modifier = Modifier.weight(1f))

              // Logout button
              OutlinedButton(
                  onClick = { signOutUser() },
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
