package com.android.shelfLife.model.newhousehold

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HouseholdRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val dataStore: DataStore<Preferences>,
    private val listFoodItemRepository: FoodItemRepository
) : HouseHoldRepository {

  private val auth = FirebaseAuth.getInstance()
  private val collectionPath = "households"

  // Local cache for households
  private val _households = MutableStateFlow<List<HouseHold>>(emptyList())
  override val households: StateFlow<List<HouseHold>> = _households.asStateFlow()

  private val _householdToEdit = MutableStateFlow<HouseHold?>(null)
  override val householdToEdit: StateFlow<HouseHold?> = _householdToEdit.asStateFlow()

  private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
  var selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold.asStateFlow()

  // Listener registration for real-time updates
  private var householdsListenerRegistration: ListenerRegistration? = null

  private val KEY = stringPreferencesKey("household_uid")

  /** Save the selected household UID to DataStore. */
  private suspend fun saveSelectedHouseholdUid(uid: String?) {
    Log.d("HouseholdViewModel", "Saving selected household UID: $uid")
    dataStore.edit { preferences -> preferences[KEY] = uid ?: "" }
  }

  /** Load the selected household UID from DataStore. */
  private suspend fun loadSelectedHouseholdUid(callback: (String?) -> Unit) {
    val uid = dataStore.data.map { preferences -> preferences[KEY] }.first()
    callback(uid)
  }

  /**
   * Generates a new unique ID for a household.
   *
   * @return A new unique ID.
   */
  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Selects a household to edit
   *
   * @param household - The household to edit.
   */
  fun selectHouseholdToEdit(household: HouseHold?) {
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

  /**
   * Initializes households by fetching them from Firestore and updating the local cache.
   *
   * @param householdIds List of household IDs to fetch.
   */
  suspend fun initializeHouseholds(householdIds: List<String>) {
    if (householdIds.isEmpty()) {
      _households.value = emptyList()
      return
    }
    try {
      // Fetch households from Firestore
      val querySnapshot =
          db.collection(collectionPath).whereIn(FieldPath.documentId(), householdIds).get().await()

      val fetchedHouseholds = querySnapshot.documents.mapNotNull { convertToHousehold(it) }
      _households.value = fetchedHouseholds
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error initializing households", e)
      _households.value = emptyList()
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
      db.collection(collectionPath)
          .document(household.uid) // Use the household UID as the document ID
          .set(householdData)
          .await()
      // Update local cache
      val currentHouseholds = _households.value.toMutableList()
      currentHouseholds.add(household)
      _households.value = currentHouseholds
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error adding household", e)
    }
  }

  /**
   * Updates an existing household in the repository and updates the local cache.
   *
   * @param household The household with updated data.
   */
  override suspend fun updateHousehold(household: HouseHold) {
    val householdData =
        mapOf(
            "name" to household.name,
            "members" to household.members,
            "sharedRecipes" to household.sharedRecipes)
    try {
      db.collection(collectionPath).document(household.uid).set(householdData).await()

      // Update local cache
      val currentHouseholds = _households.value.toMutableList()
      val index = currentHouseholds.indexOfFirst { it.uid == household.uid }
      if (index != -1) {
        currentHouseholds[index] = household
      } else {
        currentHouseholds.add(household)
      }
      _households.value = currentHouseholds
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error updating household", e)
    }
  }

  override suspend fun deleteHouseholdById(id: String) {
    try {
      db.collection(collectionPath).document(id).delete().await()

      // Update local cache
      val currentHouseholds = _households.value.filterNot { it.uid == id }
      _households.value = currentHouseholds

      if (_selectedHousehold.value == null || id == _selectedHousehold.value!!.uid) {
        // If the deleted household was selected, deselect it
        selectHousehold(_households.value.firstOrNull())
      }
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error deleting household", e)
    }
  }

  override suspend fun selectHousehold(household: HouseHold?) {
    // Save the selected household UID to DataStore
    if (_selectedHousehold.value == null || _selectedHousehold.value!!.uid != household?.uid) {
      saveSelectedHouseholdUid(household?.uid)
    }
    _selectedHousehold.value = household
    household?.let { listFoodItemRepository.getFoodItems(it.uid) }
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
