package com.android.shelfLife.model.invitations

import com.google.firebase.Timestamp

/**
 * Data class representing an invitation.
 *
 * @property invitationId The unique ID of the invitation.
 * @property householdId The ID of the household associated with the invitation.
 * @property householdName The name of the household associated with the invitation.
 * @property invitedUserId The ID of the user who is invited.
 * @property inviterUserId The ID of the user who sent the invitation.
 * @property timestamp The timestamp of when the invitation was created, optional.
 */
data class Invitation(
    val invitationId: String, // The unique ID of the invitation
    val householdId: String, // The ID of the household associated with the invitation
    val householdName: String, // The name of the household associated with the invitation
    val invitedUserId: String, // The ID of the user who is invited
    val inviterUserId: String, // The ID of the user who sent the invitation
    val timestamp: Timestamp? = null // The timestamp of when the invitation was created, optional
)