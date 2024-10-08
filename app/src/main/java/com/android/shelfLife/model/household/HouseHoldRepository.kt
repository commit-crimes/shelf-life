package com.android.shelfLife.model.household

interface HouseHoldRepository {
    /**
     * Fetches all households from the repository associated with the current user.
     *
     * @param onSuccess - Called when the list of households is successfully retrieved.
     * @param onFailure - Called when there is an error retrieving the households.
     */
    fun getHouseholds(onSuccess: (List<HouseHold>) -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Adds a new household to the repository.
     *
     * @param household - The household to be added.
     * @param onSuccess - Called when the household is successfully added.
     * @param onFailure - Called when there is an error adding the household.
     */
    fun addHousehold(household: HouseHold, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Updates an existing household in the repository.
     *
     * @param household - The household with updated data.
     * @param onSuccess - Called when the household is successfully updated.
     * @param onFailure - Called when there is an error updating the household.
     */
    fun updateHousehold(household: HouseHold, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Deletes a household by its unique ID.
     *
     * @param id - The unique ID of the household to delete.
     * @param onSuccess - Called when the household is successfully deleted.
     * @param onFailure - Called when there is an error deleting the household.
     */
    fun deleteHouseholdById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}