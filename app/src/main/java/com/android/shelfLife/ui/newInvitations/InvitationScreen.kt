package com.android.shelfLife.ui.newInvitations

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.shelfLife.model.newInvitations.Invitation
import com.android.shelfLife.model.newInvitations.InvitationRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.viewmodel.InvitationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationScreen(invitationRepository: InvitationRepository, userRepository: UserRepository) {
  val invitationViewModel = viewModel { InvitationViewModel(invitationRepository, userRepository) }
  val invitations by invitationViewModel.invitations.collectAsState()
  Scaffold(topBar = { TopAppBar(title = { Text("Invitations") }) }) { paddingValues ->
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
  val coroutineScope = rememberCoroutineScope()
  Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
    Column(modifier = Modifier.padding(16.dp).testTag("invitationCard")) {
      Text(
          text = "You have been invited to join household: ${invitation.householdName}",
          style = MaterialTheme.typography.titleMedium)
      Spacer(modifier = Modifier.height(8.dp))
      Row {
        Button(
            onClick = {
              coroutineScope.launch {
                invitationViewModel.acceptInvitation(invitation)
                Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.weight(1f)) {
              Text("Accept")
            }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
              coroutineScope.launch {
                invitationViewModel.declineInvitation(invitation)
                Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f)) {
              Text("Decline")
            }
      }
    }
  }
}
