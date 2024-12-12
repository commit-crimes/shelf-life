package com.android.shelfLife.model.user

import android.content.Context
import com.android.shelfLife.model.newhousehold.HouseHold
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardMode
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

  /** Exposes the user data as a StateFlow. */
  val user: StateFlow<User?>

  /** Exposes the invitations list as a StateFlow. */
  val invitations: StateFlow<List<String>>

  var isAudioPlaying : StateFlow<Boolean>

  var currentAudioMode: StateFlow<LeaderboardMode?>

  /** Generates a new unique ID for a user. */
  fun getNewUid(): String

  /**
   * Fetches the user information from Firestore and initializes local data. This can be called once
   * at startup to load initial data.
   */
  suspend fun initializeUserData(context: Context)

  /** Starts listening for changes to the invitations field. */
  fun startListeningForInvitations()

  /**
   * Stops listening for changes to the invitations field. Call this when the listener is no longer
   * needed to avoid memory leaks.
   */
  fun stopListeningForInvitations()

  fun setAudioPlaying(isPlaying: Boolean)

  fun setCurrentAudioMode(mode: LeaderboardMode?)


  // Other suspend functions for updating user data
  suspend fun addHouseholdUID(householdUID: String)

  suspend fun deleteHouseholdUID(uid: String)

  suspend fun updateSelectedHouseholdUID(householdUID: String)

  suspend fun addRecipeUID(recipeUID: String)

  suspend fun deleteRecipeUID(uid: String)

  suspend fun deleteInvitationUID(uid: String)

  suspend fun updateUsername(username: String)

  suspend fun updateImage(url: String)

  suspend fun updateEmail(email: String)

  suspend fun updateSelectedHousehold(selectedHouseholdUID: String)

  /** @param userEmails - The set of user emails to get the user IDs for. */
  suspend fun getUserIds(userEmails: Set<String?>): Map<String, String>

  /** @param userIds - The list of user IDs to get the emails for. */
  suspend fun getUserEmails(userIds: List<String>): Map<String, String>

  /**
   * Selects a household and saves it to the user's data. VIEW MODELS NEED TO MANUALLY SELECT THE
   * LIST OF FOOD ITEMS!!!
   *
   * @param household - The household to select.
   */
  suspend fun selectHousehold(householdUid: String?)
    suspend fun getUserNames(userIds: List<String>): Map<String, String>
}
