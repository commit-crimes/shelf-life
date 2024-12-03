package com.android.shelfLife.model.newhousehold

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
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
    private val listFoodItemRepository: FoodItemRepository,
    private val userRepository: UserRepository
) : HouseHoldRepository {

  private val auth = FirebaseAuth.getInstance()
  private val collectionPath = "households"

  // Local cache for households
  private val _households = MutableStateFlow<List<HouseHold>>(emptyList())
  override val households: StateFlow<List<HouseHold>> = _households.asStateFlow()

  private val _householdToEdit = MutableStateFlow<HouseHold?>(null)
  override val householdToEdit: StateFlow<HouseHold?> = _householdToEdit.asStateFlow()

  private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
  override var selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold.asStateFlow()

  // Listener registration for real-time updates
  private var householdsListenerRegistration: ListenerRegistration? = null

  private val KEY = stringPreferencesKey("household_uid")

  /** Save the selected household UID to DataStore. */
  private suspend fun saveSelectedHouseholdUid(uid: String?) {
    Log.d("HouseholdViewModel", "Saving selected household UID: $uid")
    dataStore.edit { preferences -> preferences[KEY] = uid ?: "" }
  }

  /** Load the selected household UID from DataStore. */
  private suspend fun loadSelectedHouseholdUid(): String? {
    return dataStore.data.map { preferences -> preferences[KEY] }.first()
  }

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

  /**
   * Initializes households by fetching them from Firestore and updating the local cache.
   *
   * @param householdIds List of household IDs to fetch.
   */
  override suspend fun initializeHouseholds(householdIds: List<String>) {
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

      val uid = loadSelectedHouseholdUid()
      if (uid != null) {
        Log.d("HouseholdRepositoryFirestore", "Selected household UID: $uid")
        selectHousehold(_households.value.find { it.uid == uid } ?: _households.value.firstOrNull())
      } else {
        selectHousehold(_households.value.firstOrNull())
      }
      updateSelectedHousehold()
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error initializing households", e)
      _households.value = emptyList()
    }
  }

  /**
   * Updates the selected household with the latest data from the list of households using the uid.
   */
  private suspend fun updateSelectedHousehold() {
    Log.d("HouseholdViewModel", "Updating selected household")
    _selectedHousehold.value?.let { selectedHousehold ->
      val updatedHousehold = _households.value.find { it.uid == selectedHousehold.uid }
      selectHousehold(updatedHousehold)
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
      // Add the household UID to the user's list of household UIDs
      userRepository.addHouseholdUID(household.uid)
      // Update local cache
      Log.d("HouseholdRepository", "Added household: $household")
      val currentHouseholds = _households.value.toMutableList()
      currentHouseholds.add(household)
      _households.value = currentHouseholds

      // Update the selected household if necessary
      if (_selectedHousehold.value == null) {
        Log.d("HouseholdViewModel", "Selected household is null")
        selectHousehold(_households.value.firstOrNull()) // Default to the first household
      } else {
        updateSelectedHousehold()
      }
      Log.d("HouseholdRepository", "Households: ${_households.value}")
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
      Log.d("HouseholdRepository", "Updated household: $household")
      _households.value = currentHouseholds
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error updating household", e)
    }
  }

  override suspend fun deleteHouseholdById(id: String) {
    try {
      db.collection(collectionPath).document(id).delete().await()

      // Remove the household UID from the user's list of household UIDs
      userRepository.deleteHouseholdUID(id)

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
