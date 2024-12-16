package com.android.shelfLife.model.user

import android.content.Context
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardMode
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing user data and interactions in the Shelf Life app.
 *
 * This repository provides methods to handle user data, including updating and fetching user
 * details, managing households, recipes, invitations, and handling audio playback modes.
 */
interface UserRepository {

  /** Exposes the user data as a [StateFlow]. */
  val user: StateFlow<User?>

  /** Exposes the list of invitation UIDs as a [StateFlow]. */
  val invitations: StateFlow<List<String>>

  /** Represents whether audio is currently playing as a [StateFlow]. */
  var isAudioPlaying: StateFlow<Boolean>

  /** Represents the current audio mode (e.g., leaderboard sounds) as a [StateFlow]. */
  var currentAudioMode: StateFlow<LeaderboardMode?>

  /**
   * Generates a new unique ID for a user.
   *
   * @return A unique string representing a new user ID.
   */
  fun getNewUid(): String

  /**
   * Fetches the user information from Firestore and initializes local data.
   *
   * This should be called once during app startup to load the user's initial data.
   *
   * @param context The [Context] required for initializing user data.
   */
  suspend fun initializeUserData(context: Context)

  /**
   * Sets the audio playback state.
   *
   * @param isPlaying Whether audio is currently playing.
   */
  fun setAudioPlaying(isPlaying: Boolean)

  /**
   * Sets the current audio mode for leaderboard sounds.
   *
   * @param mode The [LeaderboardMode] to set, or `null` to disable audio mode.
   */
  fun setCurrentAudioMode(mode: LeaderboardMode?)

  /**
   * Adds a household UID to the user's household list.
   *
   * @param householdUID The UID of the household to add.
   */
  suspend fun addHouseholdUID(householdUID: String)

  /**
   * Deletes a household UID from the user's household list.
   *
   * @param uid The UID of the household to delete.
   */
  suspend fun deleteHouseholdUID(uid: String)

  /**
   * Updates the currently selected household UID for the user.
   *
   * @param householdUID The UID of the household to set as selected.
   */
  suspend fun updateSelectedHouseholdUID(householdUID: String)

  /**
   * Adds a recipe UID to the user's recipe list.
   *
   * @param recipeUID The UID of the recipe to add.
   */
  suspend fun addRecipeUID(recipeUID: String)

  /**
   * Deletes a recipe UID from the user's recipe list.
   *
   * @param uid The UID of the recipe to delete.
   */
  suspend fun deleteRecipeUID(uid: String)

  /**
   * Deletes an invitation UID from the user's invitation list.
   *
   * @param uid The UID of the invitation to delete.
   */
  suspend fun deleteInvitationUID(uid: String)

  /**
   * Updates the username for the user.
   *
   * @param username The new username to set.
   */
  suspend fun updateUsername(username: String)

  /**
   * Updates the user's profile image URL.
   *
   * @param url The new profile image URL to set.
   */
  suspend fun updateImage(url: String)

  /**
   * Updates the user's email address.
   *
   * @param email The new email address to set.
   */
  suspend fun updateEmail(email: String)

  /**
   * Updates the currently selected household for the user.
   *
   * @param selectedHouseholdUID The UID of the household to set as selected.
   */
  suspend fun updateSelectedHousehold(selectedHouseholdUID: String)

  /**
   * Fetches user IDs for a given set of email addresses.
   *
   * @param userEmails The set of user email addresses to look up.
   * @return A map of email addresses to their corresponding user IDs.
   */
  suspend fun getUserIds(userEmails: Set<String?>): Map<String, String>

  /**
   * Fetches email addresses for a given list of user IDs.
   *
   * @param userIds The list of user IDs to look up.
   * @return A map of user IDs to their corresponding email addresses.
   */
  suspend fun getUserEmails(userIds: List<String>): Map<String, String>

  /**
   * Adds the current user to a specific household.
   *
   * @param householdUID The UID of the household to add the user to.
   * @param userUID The UID of the user to add.
   */
  suspend fun addCurrentUserToHouseHold(householdUID: String, userUID: String)

  /**
   * Selects a household and saves it to the user's data.
   *
   * **Note**: ViewModels need to manually select the list of food items associated with
   * the selected household.
   *
   * @param householdUid The UID of the household to select, or `null` to deselect.
   */
  suspend fun selectHousehold(householdUid: String?)

  /**
   * Fetches usernames for a given list of user IDs.
   *
   * @param userIds The list of user IDs to look up.
   * @return A map of user IDs to their corresponding usernames.
   */
  suspend fun getUserNames(userIds: List<String>): Map<String, String>
}