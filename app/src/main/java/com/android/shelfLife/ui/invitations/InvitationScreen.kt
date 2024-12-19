package com.android.shelfLife.ui.invitations

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.shelfLife.model.invitations.Invitation
import com.android.shelfLife.ui.navigation.NavigationActions
import com.android.shelfLife.viewmodel.invitations.InvitationViewModel
import kotlinx.coroutines.launch

/**
 * Composable function to display the Invitation Screen.
 *
 * @param navigationActions Actions for navigation.
 * @param viewModel ViewModel for the invitations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationScreen(
    navigationActions: NavigationActions,
    viewModel: InvitationViewModel = hiltViewModel()
) {
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
      LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
        items(invitations) { invitation ->
          InvitationCard(invitation, viewModel, navigationActions)
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

/**
 * Composable function to display an individual invitation card.
 *
 * @param invitation The invitation data.
 * @param invitationViewModel ViewModel for the invitations.
 * @param navigationActions Actions for navigation.
 */
@Composable
fun InvitationCard(
    invitation: Invitation,
    invitationViewModel: InvitationViewModel,
    navigationActions: NavigationActions
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
                navigationActions.goBack()
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
                navigationActions.goBack()
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
