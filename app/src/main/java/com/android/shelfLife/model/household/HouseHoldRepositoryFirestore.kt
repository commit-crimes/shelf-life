package com.android.shelfLife.model.household

import AudioPlayer
import android.util.Log
import com.android.shelfLife.ui.leaderboard.ThemeManager
import com.google.firebase.firestore.*
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository class for managing households in Firestore.
 *
 * @property db The Firestore database instance.
 */
class HouseholdRepositoryFirestore
@Inject
constructor(
    private val db: FirebaseFirestore,
) : HouseHoldRepository {

  private val collectionPath = "households"

  // Local cache for households
  private val _households = MutableStateFlow<List<HouseHold>>(emptyList())
  override val households: StateFlow<List<HouseHold>> = _households.asStateFlow()

  private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
  override val selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold.asStateFlow()

  private val _householdToEdit = MutableStateFlow<HouseHold?>(null)
  override val householdToEdit: StateFlow<HouseHold?> = _householdToEdit.asStateFlow()

  // Listener registration for real-time updates
  private var householdsListenerRegistration: ListenerRegistration? = null

  /** Generates a new unique ID for a household. */
  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Selects a household.
   *
   * @param household The household to select.
   */
  override fun selectHousehold(household: HouseHold?) {
    AudioPlayer.stopAudio()
    ThemeManager.resetMode()
    _selectedHousehold.value = household
  }

  /**
   * Selects a household to edit.
   *
   * @param household The household to edit.
   */
  override fun selectHouseholdToEdit(household: HouseHold?) {
    _householdToEdit.value = household
  }

  /**
   * Checks if a household name already exists in the list of households.
   *
   * @param houseHoldName The name of the household to check.
   * @return True if the household name already exists, false otherwise.
   */
  override fun checkIfHouseholdNameExists(houseHoldName: String): Boolean {
    return _households.value.any { it.name == houseHoldName }
  }

  /**
   * Fetches a household by its UID.
   *
   * @param householdId The UID of the household to fetch.
   */
  override suspend fun getHousehold(householdId: String) {
    try {
      db.collection(collectionPath)
          .whereIn(FieldPath.documentId(), listOf(householdId))
          .addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
              Log.e("HouseholdRepository", "Error getting household", exception)
              return@addSnapshotListener
            }

            val households =
                querySnapshot?.documents?.mapNotNull { convertToHousehold(it) } ?: emptyList()

            if (_households.value.contains(households.first())) {
              _households.value += households.first()
            }
            Log.d("HouseholdRepository", "Households: ${_households.value}")
          }
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error getting household", e)
    }
  }

  /**
   * Initializes households by fetching them from Firestore and updating the local cache.
   *
   * @param householdIds List of household IDs to fetch.
   * @param selectedHouseholdUid The UID of the selected household.
   */
  override suspend fun initializeHouseholds(
      householdIds: List<String>,
      selectedHouseholdUid: String?
  ) {
    if (householdIds.isEmpty()) {
      Log.d("HouseholdRepository", "No household IDs provided")
      _households.value = emptyList()
      _selectedHousehold.value = null
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
      selectHousehold(_households.value.find { it.uid == selectedHouseholdUid })
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error initializing households", e)
    }
  }

  /**
   * Adds a new household to the repository and updates the local cache.
   *
   * @param household The household to add.
   */
  override fun addHousehold(household: HouseHold) {
    val householdData =
        mapOf(
            "name" to household.name,
            "members" to household.members,
            "sharedRecipes" to household.sharedRecipes,
            "ratPoints" to household.ratPoints,
            "stinkyPoints" to household.stinkyPoints)

    // Optimistically update local cache
    val currentHouseholds = _households.value.toMutableList().apply { add(household) }
    _households.value = currentHouseholds

    // Perform Firebase operation
    db.collection(collectionPath).document(household.uid).set(householdData).addOnFailureListener {
        exception ->
      Log.e("HouseholdRepository", "Error adding household", exception)
      // Rollback: Remove the household from the local cache
      _households.value = _households.value.filterNot { it.uid == household.uid }
    }
  }

  /**
   * Updates an existing household in the repository and updates the local cache.
   *
   * @param household The household with updated data.
   * @param onSuccess Callback function to be invoked on successful update.
   */
  override fun updateHousehold(household: HouseHold, onSuccess: (String) -> Unit) {
    var originalItem: HouseHold? = null
    val householdData =
        mapOf(
            "name" to household.name,
            "members" to household.members,
            "sharedRecipes" to household.sharedRecipes,
            "ratPoints" to household.ratPoints,
            "stinkyPoints" to household.stinkyPoints)

    // Optimistically update local cache
    val currentHouseholds = _households.value.toMutableList()
    val index = currentHouseholds.indexOfFirst { it.uid == household.uid }
    if (index != -1) {
      originalItem = currentHouseholds[index]
      println("we here $index")
      currentHouseholds[index] = household
      println("$currentHouseholds")
    } else {
      currentHouseholds.add(household)
      println("we there")
    }
    _households.value = currentHouseholds

    // Perform Firebase operation
    db.collection(collectionPath)
        .document(household.uid)
        .set(householdData)
        .addOnSuccessListener { onSuccess(household.uid) }
        .addOnFailureListener { exception ->
          Log.e("HouseholdRepository", "Error updating household", exception)
          // Rollback: Restore the original item in the local cache
          originalItem?.let {
            val rollbackHouseholds = _households.value.toMutableList()
            val rollbackIndex = rollbackHouseholds.indexOfFirst { it.uid == household.uid }
            if (rollbackIndex != -1) {
              rollbackHouseholds[rollbackIndex] = it
            } else {
              rollbackHouseholds.remove(household)
            }
            _households.value = rollbackHouseholds
          }
        }
  }

  /**
   * Deletes a household by its unique ID.
   *
   * @param id The unique ID of the household to delete.
   * @param onSuccess Callback function to be invoked on successful deletion.
   */
  override fun deleteHouseholdById(id: String, onSuccess: (String) -> Unit) {
    // Find the household to be deleted
    val deletedHouseHold = _households.value.find { it.uid == id }

    // Optimistically update local cache
    val currentHouseholds = _households.value.filterNot { it.uid == id }
    _households.value = currentHouseholds
    // Perform Firebase operation
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener {
          onSuccess(id)
          Log.d("HouseholdRepository", "Successfully deleted household: $id")
        }
        .addOnFailureListener { exception ->
          Log.e("HouseholdRepository", "Error deleting household", exception)
          // Rollback: Restore the deleted household in the local cache
          deletedHouseHold?.let {
            val rollbackHouseholds = _households.value.toMutableList()
            rollbackHouseholds.add(it)
            _households.value = rollbackHouseholds
          }
        }
  }

  /**
   * Fetches the list of members for a specific household.
   *
   * @param householdId The UID of the household.
   * @return List of member UIDs in the household.
   */
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
   * Updates the stinky points for a household.
   *
   * @param householdId The ID of the household.
   * @param stinkyPoints The updated stinky points.
   */
  override fun updateStinkyPoints(householdId: String, stinkyPoints: Map<String, Long>) {
    // Save the original points for rollback
    val originalStinkyPoints = _selectedHousehold.value?.stinkyPoints

    // Optimistically update local cache
    _selectedHousehold.value = _selectedHousehold.value?.copy(stinkyPoints = stinkyPoints)

    // Perform Firebase operation
    db.collection(collectionPath)
        .document(householdId)
        .update("stinkyPoints", stinkyPoints)
        .addOnSuccessListener {
          Log.d(
              "HouseholdRepository",
              "Successfully updated stinky points for household: $householdId")
        }
        .addOnFailureListener { exception ->
          Log.e("HouseholdRepository", "Error updating stinky points", exception)
          // Rollback: Restore the original stinky points
          _selectedHousehold.value =
              _selectedHousehold.value?.copy(stinkyPoints = originalStinkyPoints.orEmpty())
        }
  }

  /**
   * Updates the rat points for a household.
   *
   * @param householdId The ID of the household.
   * @param ratPoints The updated rat points.
   */
  override fun updateRatPoints(householdId: String, ratPoints: Map<String, Long>) {
    // Save the original points for rollback
    val originalRatPoints = _selectedHousehold.value?.ratPoints

    // Optimistically update local cache
    _selectedHousehold.value = _selectedHousehold.value?.copy(ratPoints = ratPoints)

    // Perform Firebase operation
    db.collection(collectionPath)
        .document(householdId)
        .update("ratPoints", ratPoints)
        .addOnSuccessListener {
          Log.d(
              "HouseholdRepository", "Successfully updated rat points for household: $householdId")
        }
        .addOnFailureListener { exception ->
          Log.e("HouseholdRepository", "Error updating rat points", exception)
          // Rollback: Restore the original rat points
          _selectedHousehold.value =
              _selectedHousehold.value?.copy(ratPoints = originalRatPoints.orEmpty())
        }
  }

  /**
   * Deletes a household from the local list.
   *
   * @param householdId The ID of the household to delete.
   */
  override fun deleteHouseholdFromLocalList(householdId: String) {
    val currentHouseholds = _households.value.filterNot { it.uid == householdId }
    _households.value = currentHouseholds
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
      val ratPoints = doc.get("ratPoints") as? Map<String, Long> ?: emptyMap()
      val stinkyPoints = doc.get("stinkyPoints") as? Map<String, Long> ?: emptyMap()

      HouseHold(
          uid = uid,
          name = name,
          members = members,
          sharedRecipes = sharedRecipes,
          ratPoints = ratPoints,
          stinkyPoints = stinkyPoints)
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error converting document to HouseHold", e)
      null
    }
  }
}
