package com.android.shelfLife.model.newhousehold

interface HouseHoldRepository {

  /** Generates a new unique ID for a household. */
  fun getNewUid(): String

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
}
