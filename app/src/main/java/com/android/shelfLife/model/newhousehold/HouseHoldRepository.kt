package com.android.shelfLife.model.newhousehold

import kotlinx.coroutines.flow.StateFlow

interface HouseHoldRepository {

  val householdToEdit: StateFlow<HouseHold?>
  val households: StateFlow<List<HouseHold>>

  /** Generates a new unique ID for a household. */
  fun getNewUid(): String

  /**
   * Selects a household to edit
   *
   * @param household - The household to edit.
   */
  fun selectHouseholdToEdit(household: HouseHold?)

  /**
   * Fetches households by their UIDs.
   *
   * @param listOfHouseHoldUid - List of household UIDs to fetch.
   * @return List of households corresponding to the provided UIDs.
   */
  suspend fun getHouseholds(listOfHouseHoldUid: List<String>): List<HouseHold>

  /** Adds a new household to the repository. */
  suspend fun addHousehold(household: HouseHold)

  /** Updates an existing household in the repository. */
  suspend fun updateHousehold(household: HouseHold)

  /** Deletes a household by its unique ID. */
  suspend fun deleteHouseholdById(id: String)

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
   * @param selectedHouseholdId The ID of the selected household.
   */
  suspend fun initializeHouseholds(householdIds: List<String>, selectedHouseholdUid: String)

  /**
   * Checks if a household name already exists in the list of households.
   *
   * @param houseHoldName - The name of the household to check.
   * @return True if the household name already exists, false otherwise.
   */
  fun checkIfHouseholdNameExists(houseHoldName: String): Boolean
}
