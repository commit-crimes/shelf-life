package com.android.shelfLife.model.user

import com.android.shelfLife.model.newhousehold.HouseHold
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
  /** Exposes the user data as a StateFlow. */
  val user: StateFlow<User?>

  /** Exposes the invitations list as a StateFlow. */
  val invitations: StateFlow<List<String>>

  val selectedHousehold: StateFlow<HouseHold?>

  /** Generates a new unique ID for a user. */
  fun getNewUid(): String

  /**
   * Fetches the user information from Firestore and initializes local data. This can be called once
   * at startup to load initial data.
   */
  suspend fun initializeUserData()

  /** Starts listening for changes to the invitations field. */
  fun startListeningForInvitations()

  /**
   * Stops listening for changes to the invitations field. Call this when the listener is no longer
   * needed to avoid memory leaks.
   */
  fun stopListeningForInvitations()

  // Other suspend functions for updating user data
  suspend fun addHouseholdUID(householdUID: String)

  suspend fun deleteHouseholdUID(uid: String)

  suspend fun addRecipeUID(recipeUID: String)

  suspend fun deleteRecipeUID(uid: String)

  suspend fun deleteInvitationUID(uid: String)

  suspend fun updateUsername(username: String)

  suspend fun updateEmail(email: String)

  suspend fun updateSelectedHousehold(selectedHouseholdUID: String)

  fun getUserIds(users: Set<String?>, callback: (Map<String, String>) -> Unit)

  fun getUserEmails(userIds: List<String>, callback: (Map<String, String>) -> Unit)

  /**
   * Selects a household and saves it to the user's data. VIEW MODELS NEED TO MANUALLY SELECT THE
   * LIST OF FOOD ITEMS!!!
   *
   * @param household - The household to select.
   */
  suspend fun selectHousehold(household: HouseHold?)
}
