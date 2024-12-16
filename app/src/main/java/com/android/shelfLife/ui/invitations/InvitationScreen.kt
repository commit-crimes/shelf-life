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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * Composable function to display the invitation screen.
         *
         * This screen shows a list of pending invitations. If there are no invitations,
         * it displays a message stating "No pending invitations". If there are invitations,
         * each invitation is displayed in a card with options to accept or decline the invitation.
         *
         * @param navigationActions The navigation actions to handle screen transitions.
         * @param viewModel The [InvitationViewModel] responsible for handling invitations logic.
         */
fun InvitationScreen(
    navigationActions: NavigationActions,
    viewModel: InvitationViewModel = hiltViewModel()
) {
    // Observe the list of invitations from the viewModel
    val invitations by viewModel.invitations.collectAsState()

    // Scaffold for the layout, includes top bar and content area
    Scaffold(topBar = { TopAppBar(title = { Text("Invitations") }) }) { paddingValues ->

        // If there are no invitations, display a message
        if (invitations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No pending invitations")
            }
        } else {
            // Display a list of invitations
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                items(invitations) { invitation ->
                    // Display each invitation in a card
                    InvitationCard(invitation, viewModel, navigationActions)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
        /**
         * Composable function to display a single invitation in a card.
         *
         * Each invitation shows the household name and has two buttons: Accept and Decline.
         * Clicking on Accept will accept the invitation, while Decline will decline it.
         *
         * @param invitation The [Invitation] to display in the card.
         * @param invitationViewModel The [InvitationViewModel] for handling invitation logic.
         * @param navigationActions The navigation actions to go back after accepting/declining.
         */
fun InvitationCard(
    invitation: Invitation,
    invitationViewModel: InvitationViewModel,
    navigationActions: NavigationActions
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Card displaying the invitation details
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
        Column(modifier = Modifier.padding(16.dp).testTag("invitationCard")) {
            // Display household name in the invitation
            Text(
                text = "You have been invited to join household: ${invitation.householdName}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Buttons to accept or decline the invitation
            Row {
                // Accept button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            invitationViewModel.acceptInvitation(invitation)
                            Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show()
                        }
                        navigationActions.goBack() // Go back after accepting
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Decline button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            invitationViewModel.declineInvitation(invitation)
                            Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show()
                            navigationActions.goBack() // Go back after declining
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Decline")
                }
            }
        }
    }
}