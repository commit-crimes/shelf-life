package com.android.shelfLife.model.invitations

import com.google.firebase.Timestamp

/**
 * Data class representing an invitation in the Shelf Life app.
 *
 * This class models an invitation sent to a user to join a household.
 *
 * @property invitationId Unique identifier for the invitation.
 * @property householdId Unique identifier of the household that the user is invited to join.
 * @property householdName Name of the household that the user is invited to join.
 * @property invitedUserId ID of the user who is invited to join the household.
 * @property inviterUserId ID of the user who sent the invitation.
 * @property timestamp The time when the invitation was created. Defaults to `null` if not specified.
 */
data class Invitation(
    val invitationId: String, // Unique identifier for the invitation
    val householdId: String, // ID of the household the invitation is associated with
    val householdName: String, // Name of the household
    val invitedUserId: String, // ID of the invited user
    val inviterUserId: String, // ID of the user who sent the invitation
    val timestamp: Timestamp? = null // Timestamp for when the invitation was created, optional
)