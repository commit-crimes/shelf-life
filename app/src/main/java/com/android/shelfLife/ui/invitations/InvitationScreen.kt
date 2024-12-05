package com.android.shelfLife.ui.invitations

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.invitations.InvitationViewModel
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.ui.utils.CustomTopAppBar


@Composable
fun InvitationScreen(
    navigationActions: NavigationActions,
    invitationRepository: InvitationRepository
) {

    val invitationViewModel = viewModel{
        InvitationViewModel(invitationRepository = invitationRepository)
    }
  val invitations by invitationViewModel.invitations.collectAsState()

  Scaffold(
      topBar = {
          CustomTopAppBar(
              onClick = {navigationActions.goBack()},
              title = "Invitations",
              titleTestTag = "invitationsTitle")
      }
  ) { paddingValues ->
    if (invitations.isEmpty()) {
      Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center) {
            Text("No pending invitations")
          }
    } else {
      // Show list of invitations
      LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
        items(invitations) { invitation ->
          InvitationCard(invitation, invitationViewModel)
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
fun InvitationCard(
    invitation: Invitation,
    invitationViewModel: InvitationViewModel,
) {
  val context = LocalContext.current
  Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
    Column(modifier = Modifier.padding(16.dp).testTag("invitationCard")) {
      Text(
          text = "You have been invited to join household: ${invitation.householdName}",
          style = MaterialTheme.typography.titleMedium)
      Spacer(modifier = Modifier.height(8.dp))
      Row {
        Button(
            onClick = {
              invitationViewModel.acceptInvitation(invitation)
              Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f)) {
              Text("Accept")
            }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
              invitationViewModel.declineInvitation(invitation)
              Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f)) {
              Text("Decline")
            }
      }
    }
  }
}
