package com.android.shelfLife.ui.invitations

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.shelfLife.model.household.HouseholdViewModel
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationScreen(viewModel: HouseholdViewModel, navigationActions: NavigationActions) {
  val invitations by viewModel.invitations.collectAsState()

  Scaffold(topBar = { TopAppBar(title = { Text("Invitations") }) }) { paddingValues ->
    if (invitations.isEmpty()) {
      Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center) {
            Text("No pending invitations")
          }
    } else {
      // Show list of invitations
      Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
        invitations.forEach { invitation ->
          InvitationCard(invitation, viewModel, navigationActions)
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
fun InvitationCard(
    invitation: Invitation,
    viewModel: HouseholdViewModel,
    navigationActions: NavigationActions
) {
  Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
          text = "You have been invited to join household: ${invitation.householdName}",
          style = MaterialTheme.typography.titleMedium)
      Spacer(modifier = Modifier.height(8.dp))
      Row {
        Button(
            onClick = {
              viewModel.acceptInvitation(invitation)
              navigationActions.navigateTo(Screen.PROFILE)
            },
            modifier = Modifier.weight(1f)) {
              Text("Accept")
            }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { viewModel.declineInvitation(invitation) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f)) {
              Text("Decline")
            }
      }
    }
  }
}
