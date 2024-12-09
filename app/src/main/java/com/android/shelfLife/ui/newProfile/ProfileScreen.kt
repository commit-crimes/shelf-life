package com.android.shelfLife.ui.newProfile

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
import com.android.shelfLife.ui.newnavigation.BottomNavigationMenu
import com.android.shelfLife.ui.newnavigation.LIST_TOP_LEVEL_DESTINATION
import com.android.shelfLife.ui.newnavigation.NavigationActions
import com.android.shelfLife.ui.newnavigation.Route
import com.android.shelfLife.ui.utils.DropdownFields
import com.android.shelfLife.viewmodel.ProfileScreenViewModel
import com.example.compose.LocalThemeMode
import com.example.compose.LocalThemeTogglerProvider
import com.example.compose.ThemeMode

@Composable
fun ProfileScreen(navigationActions: NavigationActions, context: Context) {
  val profileViewModel = hiltViewModel<ProfileScreenViewModel>()
  val currentUser = profileViewModel.currentUser.collectAsState()
  val invitations by profileViewModel.invitations.collectAsState()
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
              if (currentUser.value?.photoUrl == null) {
                Image(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp).clip(CircleShape).testTag("profilePicture"),
                    contentScale = ContentScale.Crop)
              } else {
                AsyncImage(
                    model = currentUser.value?.photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp).clip(CircleShape).testTag("profilePicture"),
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
                        modifier = Modifier.padding(horizontal = 16.dp))
                  }
              Spacer(modifier = Modifier.height(16.dp))
              // Show pending invitations
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
                  onClick = { profileViewModel.signOut(context) },
                  modifier = Modifier.fillMaxWidth().testTag("logoutButton"),
                  border = BorderStroke(1.dp, Color.Red) // Outline color matches the current status
                  ) {
                    Text(text = "Log out", color = Color.Red)
                  }
            }
      }
}
