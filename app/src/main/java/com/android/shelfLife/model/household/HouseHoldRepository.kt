package com.android.shelfLife.model.household

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing households within the application.
 *
 * This interface defines the contract for a repository that handles the operations related to
 * households, including adding, updating, deleting, and retrieving households, as well as managing
 * their state.
 */
interface HouseHoldRepository {

  /** A StateFlow that emits the household currently being edited. */
  val householdToEdit: StateFlow<HouseHold?>

  /** A StateFlow that emits the list of households. */
  val households: StateFlow<List<HouseHold>>

  /** A StateFlow that emits the currently selected household. */
  val selectedHousehold: StateFlow<HouseHold?>

  /** Generates a new unique ID for a household. */
  fun getNewUid(): String

  /**
   * Selects a household to edit.
   *
   * @param household The household to edit.
   */
  fun selectHouseholdToEdit(household: HouseHold?)

  /**
   * Selects a household.
   *
   * @param household The household to select.
   */
  fun selectHousehold(household: HouseHold?)

  /**
   * Fetches a household by its UID.
   *
   * @param householdId The UID of the household to fetch.
   */
  suspend fun getHousehold(householdId: String)

  /** Adds a new household to the repository. */
  fun addHousehold(household: HouseHold)

  /**
   * Updates an existing household in the repository.
   *
   * @param household The household to update.
   * @param onSuccess Callback function to be invoked on successful update.
   */
  fun updateHousehold(household: HouseHold, onSuccess: (String) -> Unit = {})

  /**
   * Deletes a household by its unique ID.
   *
   * @param id The unique ID of the household to delete.
   * @param onSuccess Callback function to be invoked on successful deletion.
   */
  fun deleteHouseholdById(id: String, onSuccess: (String) -> Unit = {})

  /**
   * Fetches the list of members for a specific household.
   *
   * @param householdId The UID of the household.
   * @return List of member UIDs in the household.
   */
  suspend fun getHouseholdMembers(householdId: String): List<String>

  /**
   * Initializes households by fetching them from Firestore and updating the local cache.
   *
   * @param householdIds List of household IDs to fetch.
   * @param selectedHouseholdUid The UID of the selected household.
   */
  suspend fun initializeHouseholds(householdIds: List<String>, selectedHouseholdUid: String?)

  /**
   * Checks if a household name already exists in the list of households.
   *
   * @param houseHoldName The name of the household to check.
   * @return True if the household name already exists, false otherwise.
   */
  fun checkIfHouseholdNameExists(houseHoldName: String): Boolean

  /**
   * Updates the stinky points for a household.
   *
   * @param householdId The ID of the household.
   * @param stinkyPoints The updated stinky points.
   */
  fun updateStinkyPoints(householdId: String, stinkyPoints: Map<String, Long>)

  /**
   * Updates the rat points for a household.
   *
   * @param householdId The ID of the household.
   * @param ratPoints The updated rat points.
   */
  fun updateRatPoints(householdId: String, ratPoints: Map<String, Long>)

  /**
   * Deletes a household from the local list.
   *
   * @param householdId The ID of the household to delete.
   */
  fun deleteHouseholdFromLocalList(householdId: String)
}
