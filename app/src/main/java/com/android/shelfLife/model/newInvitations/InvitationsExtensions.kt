package com.android.shelfLife.model.newInvitations

fun Invitation.toMap(): Map<String, Any?> {
  return mapOf(
      "invitationId" to invitationId,
      "householdId" to householdId,
      "householdName" to householdName,
      "invitedUserId" to invitedUserId,
      "inviterUserId" to inviterUserId,
      "timestamp" to timestamp)
}