package com.android.shelfLife.model.newhousehold

import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class HouseholdRepositoryFirestore(
    private val db: FirebaseFirestore,
) : HouseHoldRepository {

  private val collectionPath = "households"

  // Local cache for households
  private val _households = MutableStateFlow<List<HouseHold>>(emptyList())
  override val households: StateFlow<List<HouseHold>> = _households.asStateFlow()

  private val _householdToEdit = MutableStateFlow<HouseHold?>(null)
  override val householdToEdit: StateFlow<HouseHold?> = _householdToEdit.asStateFlow()

  // Listener registration for real-time updates
  private var householdsListenerRegistration: ListenerRegistration? = null

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun selectHouseholdToEdit(household: HouseHold?) {
    _householdToEdit.value = household
  }

  override fun checkIfHouseholdNameExists(houseHoldName: String): Boolean {
    return _households.value.any { it.name == houseHoldName }
  }

  override suspend fun getHouseholds(listOfHouseHoldUid: List<String>): List<HouseHold> {
    if (listOfHouseHoldUid.isEmpty()) {
      return emptyList()
    }
    return try {
      val querySnapshot =
          db.collection(collectionPath)
              .whereIn(FieldPath.documentId(), listOfHouseHoldUid)
              .get()
              .await()

      val fetchedHouseholds = querySnapshot.documents.mapNotNull { convertToHousehold(it) }
      fetchedHouseholds
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error fetching households", e)
      emptyList()
    }
  }

  override suspend fun initializeHouseholds(
      householdIds: List<String>,
      selectedHouseholdUid: String
  ) {
    if (householdIds.isEmpty()) {
      Log.d("HouseholdRepository", "No household IDs provided")
      _households.value = emptyList()
      return
    }
    try {
      // Fetch households from Firestore
      val querySnapshot =
          db.collection(collectionPath).whereIn(FieldPath.documentId(), householdIds).get().await()

      Log.d("HouseholdRepository", "Fetched households: ${querySnapshot.documents}")
      val fetchedHouseholds = querySnapshot.documents.mapNotNull { convertToHousehold(it) }
      _households.value = fetchedHouseholds
      Log.d("HouseholdRepository", "Households: ${_households.value}")

      Log.d("HouseholdRepositoryFirestore", "Selected household UID: $selectedHouseholdUid")
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error initializing households", e)
    }
  }

  /**
   * Adds a new household to the repository and updates the local cache.
   *
   * @param household The household to add.
   */
  override suspend fun addHousehold(household: HouseHold) {
    val householdData =
        mapOf(
            "name" to household.name,
            "members" to household.members,
            "sharedRecipes" to household.sharedRecipes)
    try {
      val currentHouseholds = _households.value.toMutableList()
      currentHouseholds.add(household)
      _households.value = currentHouseholds

      db.collection(collectionPath)
          .document(household.uid) // Use the household UID as the document ID
          .set(householdData)
          .await()
      // Update local cache
      Log.d("HouseholdRepository", "Added household: $household")
    } catch (e: Exception) {
      val updatedHouseHolds = _households.value.filterNot { it.uid == household.uid }
      _households.value = updatedHouseHolds
      Log.e("HouseholdRepository", "Error adding household", e)
    }
  }

  /**
   * Updates an existing household in the repository and updates the local cache.
   *
   * @param household The household with updated data.
   */
  override suspend fun updateHousehold(household: HouseHold) {
    var originalItem: HouseHold? = null
    val householdData =
        mapOf(
            "name" to household.name,
            "members" to household.members,
            "sharedRecipes" to household.sharedRecipes)
    try {
      // Update local cache
      val currentHouseholds = _households.value.toMutableList()
      val index = currentHouseholds.indexOfFirst { it.uid == household.uid }
      if (index != -1) {
        originalItem = currentHouseholds[index]
        currentHouseholds[index] = household
      } else {
        currentHouseholds.add(household)
      }
      Log.d("HouseholdRepository", "Updated household: $household")
      _households.value = currentHouseholds

      db.collection(collectionPath).document(household.uid).set(householdData).await()
    } catch (e: Exception) {
        // Rollback: Restore the original item in the local cache
        originalItem?.let {
          val currentHouseholds = _households.value.toMutableList()
          val index = currentHouseholds.indexOfFirst { it.uid == household.uid }
          if (index != -1) {
            currentHouseholds[index] = it
            _households.value = currentHouseholds
          } else if (index == currentHouseholds.lastIndex) {
            currentHouseholds.removeAt(index)
            _households.value = currentHouseholds
          }
        }
      Log.e("HouseholdRepository", "Error updating household", e)
    }
  }

  override suspend fun deleteHouseholdById(id: String) {
    var deletedHouseHold: HouseHold? = null
    try {
        // Find the household to be deleted
        deletedHouseHold = _households.value.find { it.uid == id }
      // Update local cache
      val currentHouseholds = _households.value.filterNot { it.uid == id }
      _households.value = currentHouseholds

      db.collection(collectionPath).document(id).delete().await()
    } catch (e: Exception) {
        // Rollback: Restore the deleted household in the local cache
        deletedHouseHold?.let {
          val currentHouseholds = _households.value.toMutableList()
          currentHouseholds.add(it)
          _households.value = currentHouseholds
        }
      Log.e("HouseholdRepository", "Error deleting household", e)
    }
  }

  override suspend fun getHouseholdMembers(householdId: String): List<String> {
    val household = _households.value.find { it.uid == householdId }
    return household?.members ?: emptyList()
  }

  /**
   * Starts listening for real-time updates to the households collection.
   *
   * @param householdIds List of household IDs to listen to.
   */
  fun startListeningForHouseholds(householdIds: List<String>) {
    // Remove any existing listener
    householdsListenerRegistration?.remove()

    if (householdIds.isEmpty()) {
      _households.value = emptyList()
      return
    }

    householdsListenerRegistration =
        db.collection(collectionPath)
            .whereIn(FieldPath.documentId(), householdIds)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                Log.e("HouseholdRepository", "Error fetching households", error)
                _households.value = emptyList()
                return@addSnapshotListener
              }
              if (snapshot != null) {
                val updatedHouseholds = snapshot.documents.mapNotNull { convertToHousehold(it) }
                _households.value = updatedHouseholds
              }
            }
  }

  /** Stops listening for real-time updates. */
  fun stopListeningForHouseholds() {
    householdsListenerRegistration?.remove()
    householdsListenerRegistration = null
  }

  /**
   * Converts a Firestore document to a HouseHold object.
   *
   * @param doc The Firestore document to convert.
   * @return A HouseHold object or null if conversion fails.
   */
  private fun convertToHousehold(doc: DocumentSnapshot): HouseHold? {
    return try {
      val uid = doc.id // Use the document ID as the UID
      val name = doc.getString("name") ?: return null
      val members = doc.get("members") as? List<String> ?: emptyList()
      val sharedRecipes = doc.get("sharedRecipes") as? List<String> ?: emptyList()

      HouseHold(uid = uid, name = name, members = members, sharedRecipes = sharedRecipes)
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error converting document to HouseHold", e)
      null
    }
  }
}
