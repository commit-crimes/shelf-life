package com.android.shelfLife.model.user

import android.content.Context
import android.util.Log
import com.android.shelfLife.model.newhousehold.HouseHold
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryFirestore @Inject constructor(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    private val userCollection = db.collection("users")

    // Local variable to store user data
    private val _user = MutableStateFlow<User?>(null)
    override val user: StateFlow<User?> = _user.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    override val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    // Invitations StateFlow
    private val _invitations = MutableStateFlow<List<String>>(emptyList())
    override val invitations: StateFlow<List<String>> = _invitations.asStateFlow()

    private val _selectedHousehold = MutableStateFlow<HouseHold?>(null)
    override var selectedHousehold: StateFlow<HouseHold?> = _selectedHousehold.asStateFlow()

    // Listener for invitations
    private var invitationsListenerRegistration: ListenerRegistration? = null

    override fun getNewUid(): String {
        return userCollection.document().id
    }

    override suspend fun initializeUserData(context: Context) {
        val currentUser = firebaseAuth.currentUser ?: throw Exception("User not logged in")
        try {
            val snapshot = userCollection.document(currentUser.uid).get().await()
            if (snapshot.exists()) {
                val userData = convertToUser(snapshot)
                _user.value = userData
                _invitations.value = userData?.invitationUIDs ?: emptyList()
            } else {
                val currentAccount = GoogleSignIn.getLastSignedInAccount(context)
                val name = currentAccount?.displayName ?: "Guest"
                val email = currentAccount?.email ?: ""
                val photoUrl = currentAccount?.photoUrl.toString()
                _user.value = User(
                    uid = currentUser.uid,
                    username = name,
                    email = email,
                    photoUrl = photoUrl,
                    selectedHouseholdUID = ""
                )
                val userDoc = userCollection.document(currentUser.uid)
                val userData = mapOf(
                    "username" to name,
                    "email" to email,
                    "photoURL" to photoUrl,
                    "selectedHouseholdUID" to "",
                    "householdUIDs" to emptyList<String>(),
                    "recipeUIDs" to emptyList<String>(),
                    "invitationUIDs" to emptyList<String>()
                )
                userDoc.set(userData, SetOptions.merge())
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error initializing user data", e)
            _user.value = null
            _invitations.value = emptyList()
            throw e
        }
    }

    override fun startListeningForInvitations() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            invitationsListenerRegistration?.remove()
            invitationsListenerRegistration =
                userCollection.document(currentUser.uid).addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("UserRepository", "Error fetching invitations", error)
                        _invitations.value = emptyList()
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val invitationsList = snapshot.get("invitationUIDs") as? List<String> ?: emptyList()
                        _invitations.value = invitationsList
                        val currentUserData = _user.value ?: User(
                            uid = currentUser.uid,
                            username = "",
                            email = "",
                            selectedHouseholdUID = ""
                        )
                        _user.value = currentUserData.copy(invitationUIDs = invitationsList)
                    } else {
                        _invitations.value = emptyList()
                    }
                }
        } else {
            _invitations.value = emptyList()
        }
    }

    override fun stopListeningForInvitations() {
        invitationsListenerRegistration?.remove()
        invitationsListenerRegistration = null
    }

    private suspend fun updateUserField(fieldName: String, value: Any) {
        val currentUser = firebaseAuth.currentUser ?: throw Exception("User not logged in")
        userCollection.document(currentUser.uid).update(fieldName, value).await()

        val currentUserData = _user.value
            ?: User(uid = currentUser.uid, username = "", email = "", selectedHouseholdUID = "")
        val updatedUserData = when (fieldName) {
            "username" -> currentUserData.copy(username = value as String)
            "photoURL" -> currentUserData.copy(photoUrl = value as String)
            "email" -> currentUserData.copy(email = value as String)
            "selectedHouseholdUID" -> currentUserData.copy(selectedHouseholdUID = value as String)
            else -> currentUserData
        }
        _user.value = updatedUserData
    }

    private suspend fun updateArrayField(fieldName: String, value: String, operation: ArrayOperation) {
        val currentUser = firebaseAuth.currentUser ?: throw Exception("User not logged in")
        val updateValue = when (operation) {
            ArrayOperation.ADD -> FieldValue.arrayUnion(value)
            ArrayOperation.REMOVE -> FieldValue.arrayRemove(value)
        }
        userCollection.document(currentUser.uid).update(fieldName, updateValue).await()

        val currentUserData = _user.value
            ?: User(uid = currentUser.uid, username = "", email = "", selectedHouseholdUID = "")
        val updatedArray = when (fieldName) {
            "householdUIDs" -> {
                val currentList = currentUserData.householdUIDs
                if (operation == ArrayOperation.ADD) currentList + value else currentList - value
            }
            "recipeUIDs" -> {
                val currentList = currentUserData.recipeUIDs
                if (operation == ArrayOperation.ADD) currentList + value else currentList - value
            }
            else -> emptyList()
        }

        val updatedUserData = when (fieldName) {
            "householdUIDs" -> currentUserData.copy(householdUIDs = updatedArray)
            "recipeUIDs" -> currentUserData.copy(recipeUIDs = updatedArray)
            else -> currentUserData
        }
        _user.value = updatedUserData
    }

    override fun setUserLoggedInStatus(isLoggedIn: Boolean) {
        _isUserLoggedIn.value = isLoggedIn
    }

    private enum class ArrayOperation {
        ADD,
        REMOVE
    }

    override suspend fun addHouseholdUID(householdUID: String) {
        updateArrayField("householdUIDs", householdUID, ArrayOperation.ADD)
    }

    override suspend fun deleteHouseholdUID(uid: String) {
        updateArrayField("householdUIDs", uid, ArrayOperation.REMOVE)
    }

    override suspend fun updateSelectedHouseholdUID(householdUID: String) {
        updateUserField("selectedHouseholdUID", householdUID)
    }

    override suspend fun addRecipeUID(recipeUID: String) {
        updateArrayField("recipeUIDs", recipeUID, ArrayOperation.ADD)
    }

    override suspend fun deleteRecipeUID(uid: String) {
        updateArrayField("recipeUIDs", uid, ArrayOperation.REMOVE)
    }

    override suspend fun deleteInvitationUID(uid: String) {
        updateArrayField("invitationUIDs", uid, ArrayOperation.REMOVE)
    }

    override suspend fun updateUsername(username: String) {
        updateUserField("username", username)
    }

    override suspend fun updateImage(url: String) {
        updateUserField("photoURL", url)
    }

    override suspend fun updateEmail(email: String) {
        val currentUser = firebaseAuth.currentUser ?: throw Exception("User not logged in")
        currentUser.updateEmail(email).await()
        updateUserField("email", email)
    }

    override suspend fun updateSelectedHousehold(selectedHouseholdUID: String) {
        updateUserField("selectedHouseholdUID", selectedHouseholdUID)
    }

    override fun getUserIds(userEmails: Set<String?>, callback: (Map<String, String>) -> Unit) {
        if (userEmails.isEmpty()) {
            callback(emptyMap())
            return
        }
        val emailBatches = userEmails.filterNotNull().chunked(10)
        val emailToUserId = mutableMapOf<String, String>()
        var batchesProcessed = 0

        for (emailBatch in emailBatches) {
            db.collection("users")
                .whereIn("email", emailBatch)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (doc in querySnapshot.documents) {
                        val email = doc.getString("email")
                        val userId = doc.id
                        if (email != null) {
                            emailToUserId[email] = userId
                        }
                    }
                    batchesProcessed++
                    if (batchesProcessed == emailBatches.size) {
                        callback(emailToUserId)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HouseholdRepository", "Error fetching user IDs by emails", exception)
                    batchesProcessed++
                    if (batchesProcessed == emailBatches.size) {
                        callback(emailToUserId)
                    }
                }
        }
    }

    override fun getUserEmails(userIds: List<String>, callback: (Map<String, String>) -> Unit) {
        if (userIds.isEmpty()) {
            callback(emptyMap())
            return
        }

        val uidBatches = userIds.chunked(10)
        val uidToEmail = mutableMapOf<String, String>()
        var batchesProcessed = 0

        for (uidBatch in uidBatches) {
            db.collection("users")
                .whereIn(FieldPath.documentId(), uidBatch)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (doc in querySnapshot.documents) {
                        val email = doc.getString("email")
                        val userId = doc.id
                        if (email != null) {
                            uidToEmail[userId] = email
                        }
                    }
                    batchesProcessed++
                    if (batchesProcessed == uidBatches.size) {
                        callback(uidToEmail)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HouseholdRepository", "Error fetching emails by user IDs", exception)
                    batchesProcessed++
                    if (batchesProcessed == uidBatches.size) {
                        callback(uidToEmail)
                    }
                }
        }
    }

    override suspend fun selectHousehold(household: HouseHold?) {
        _selectedHousehold.value = household
        household?.let { updateSelectedHousehold(it.uid) }
    }

    private fun convertToUser(doc: DocumentSnapshot): User? {
        return try {
            val uid = doc.id
            val username = doc.getString("username") ?: ""
            val email = doc.getString("email") ?: ""
            val photoURL = doc.getString("photoURL") ?: ""
            val selectedHouseholdUID = doc.getString("selectedHouseholdUID") ?: ""
            val householdUIDs = doc.get("householdUIDs") as? List<String> ?: emptyList()
            val recipeUIDs = doc.get("recipeUIDs") as? List<String> ?: emptyList()
            val invitationUIDs = doc.get("invitationUIDs") as? List<String> ?: emptyList()

            User(
                uid = uid,
                username = username,
                email = email,
                photoUrl = photoURL,
                selectedHouseholdUID = selectedHouseholdUID,
                householdUIDs = householdUIDs,
                recipeUIDs = recipeUIDs,
                invitationUIDs = invitationUIDs
            )
        } catch (e: Exception) {
            Log.e("HouseholdRepository", "Error converting document to User", e)
            null
        }
    }
}