package com.android.shelfLife.model.invitations

import com.google.firebase.Timestamp

data class Invitation(
    val invitationId: String,
    val householdId: String,
    val householdName: String,
    val invitedUserId: String,
    val inviterUserId: String,
    val timestamp: Timestamp? = null
)
