package com.android.shelfLife.ui.profile

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.android.shelfLife.ui.navigation.BottomNavigationMenu
import com.android.shelfLife.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Route
import com.android.shelfLife.ui.navigation.Screen
import com.android.shelfLife.ui.utils.DropdownFields
import com.android.shelfLife.viewmodel.profile.ProfileScreenViewModel
import com.example.compose.LocalThemeMode
import com.example.compose.LocalThemeTogglerProvider
import com.example.compose.ThemeMode

/**
 * Composable function to display the Profile screen.
 *
 * @param navigationActions The navigation actions to be used in the screen.
 * @param context The context of the application.
 * @param profileViewModel The ViewModel for managing the state of the profile screen.
 */
@Composable
fun ProfileScreen(
    navigationActions: NavigationActions,
    context: Context,
    profileViewModel: ProfileScreenViewModel = hiltViewModel()
) {
  Log.d("ProfileScreen", profileViewModel.hashCode().toString())
  val currentUser = profileViewModel.currentUser.collectAsState()
  val invitations by profileViewModel.invitationUIDS.collectAsState()
  val currentThemeMode = LocalThemeMode.current
  val themeToggler = LocalThemeTogglerProvider.current

  val options = arrayOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM_DEFAULT)
  val optionLabels =
      mapOf(
          ThemeMode.LIGHT to "Light",
          ThemeMode.DARK to "Dark",
          ThemeMode.SYSTEM_DEFAULT to "System Default")

  val color = MaterialTheme.colorScheme

  // Variables to track clicks and reset the counter after a time interval
  var clickCount by remember { mutableStateOf(0) }
  var lastClickTime by remember { mutableStateOf(0L) }

  val thresholdTime = 500 // Time in milliseconds between successive clicks

  /**
   * Function to handle the click count for the easter egg. Navigates to the Easter Egg screen if
   * clicked 5 times within the threshold time.
   */
  fun easterEggOnClickCount() {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastClickTime <= thresholdTime) {
      // Increment the click count for quick successive clicks
      clickCount++
    } else {
      // Reset the count if time exceeded threshold
      clickCount = 1
    }
    lastClickTime = currentTime
    // Check if the count has reached 5
    if (clickCount == 5) {
      navigationActions.navigateTo(Screen.EASTER_EGG)
      clickCount = 0 // Reset the counter after navigating
    }
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
              if (currentUser.value?.photoUrl == null) {
                Image(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier =
                        Modifier.size(100.dp)
                            .clip(CircleShape)
                            .testTag("profilePicture")
                            .clickable { easterEggOnClickCount() },
                    contentScale = ContentScale.Crop)
              } else {
                AsyncImage(
                    model = currentUser.value?.photoUrl,
                    contentDescription = "Profile Picture",
                    modifier =
                        Modifier.size(100.dp)
                            .clip(CircleShape)
                            .testTag("profilePicture")
                            .clickable { easterEggOnClickCount() },
                    contentScale = ContentScale.Crop)
              }
              Spacer(modifier = Modifier.height(32.dp))
              Text(
                  text = currentUser.value?.username ?: "Guest",
                  fontWeight = FontWeight.Bold,
                  color = color.primary,
                  fontSize = 28.sp,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag("profileNameText"))

              Text(
                  text = currentUser.value?.email ?: "",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag("profileEmailText"))

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
                        expanded = profileViewModel.changeThemeMenuState.value,
                        onExpandedChange = { profileViewModel.changeThemeMenuState.value = it },
                        optionLabel = { option ->
                          (optionLabels[option] ?: "System Default") + " Mode"
                        },
                        modifier = Modifier.padding(horizontal = 16.dp).testTag("themeToggler"))
                  }
              Spacer(modifier = Modifier.height(16.dp))
              // Show pending invitations
              if (invitations.isNotEmpty()) {
                Text(
                    text = "Pending Invitations",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Start).testTag("pendingInvitations"))
                Button(
                    onClick = { navigationActions.navigateTo(Route.INVITATIONS) },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("pendingInvitationsButton")) {
                      Text("You have ${invitations.size} pending invitations")
                    }
              }

              Spacer(modifier = Modifier.weight(1f))

              // Logout button
              OutlinedButton(
                  onClick = {
                    profileViewModel.signOut(context)
                    navigationActions.navigateToAndClearBackStack(Screen.AUTH)
                  },
                  modifier = Modifier.fillMaxWidth().testTag("logoutButton"),
                  border = BorderStroke(1.dp, Color.Red) // Outline color matches the current status
                  ) {
                    Text(text = "Log out", color = Color.Red)
                  }
            }
      }
}
