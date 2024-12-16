package com.android.shelfLife.model.invitations

import com.android.shelfLife.model.household.HouseHold
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Repository interface for managing invitations in the Shelf Life app.
 *
 * Provides methods to handle invitation-related operations, including sending, accepting,
 * declining, and retrieving invitations.
 */
interface InvitationRepository {

  /**
   * Declines an invitation.
   *
   * @param invitation The invitation to decline.
   * The invitation will be marked as declined and no further action will be taken.
   */
  suspend fun declineInvitation(invitation: Invitation)

  /**
   * Accepts an invitation.
   *
   * @param invitation The invitation to accept.
   * Accepting the invitation typically adds the user to the associated household.
   */
  suspend fun acceptInvitation(invitation: Invitation)

  /**
   * Sends an invitation to a user to join a household.
   *
   * @param household The household to which the user is being invited.
   * @param invitedUserID The unique ID of the user to invite.
   * The invitation will be sent and stored in the system for the user to respond.
   */
  fun sendInvitation(household: HouseHold, invitedUserID: String)

  /**
   * Retrieves a batch of invitations using their unique IDs.
   *
   * @param invitationUIDs A list of unique IDs for the invitations to retrieve.
   * @return A list of [Invitation] objects corresponding to the provided IDs.
   */
  suspend fun getInvitationsBatch(invitationUIDs: List<String>): List<Invitation>

  /**
   * Retrieves a specific invitation by its unique ID.
   *
   * @param uid The unique ID of the invitation to retrieve.
   * @return The [Invitation] object if found, or `null` if no such invitation exists.
   */
  suspend fun getInvitation(uid: String): Invitation?

  /**
   * Converts a Firestore [DocumentSnapshot] to an [Invitation] object.
   *
   * @param doc The Firestore document snapshot representing an invitation.
   * @return The converted [Invitation] object, or `null` if the conversion fails.
   */
  fun convertToInvitation(doc: DocumentSnapshot): Invitation?
}