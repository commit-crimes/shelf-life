package com.android.shelfLife.model.user

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

@Singleton
class UserRepositoryFirestore
@Inject
constructor(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : UserRepository {

  private val userCollection = db.collection("users")

  // Local variable to store user data
  private val _user = MutableStateFlow<User?>(null)
  override val user: StateFlow<User?> = _user.asStateFlow()

  // Invitations StateFlow
  private val _invitations = MutableStateFlow<List<String>>(emptyList())
  override val invitations: StateFlow<List<String>> = _invitations.asStateFlow()

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
        _user.value =
            User(
                uid = currentUser.uid,
                username = name,
                email = email,
                photoUrl = photoUrl,
                selectedHouseholdUID = "")
        val userDoc = userCollection.document(currentUser.uid)
        val userData =
            mapOf(
                "username" to name,
                "email" to email,
                "photoURL" to photoUrl,
                "selectedHouseholdUID" to "",
                "householdUIDs" to emptyList<String>(),
                "recipeUIDs" to emptyList<String>(),
                "invitationUIDs" to emptyList<String>())
        userDoc.set(userData, SetOptions.merge())
      }
    } catch (e: Exception) {
      Log.e("UserRepository", "Error initializing user data", e)
      _user.value = null
      _invitations.value = emptyList()
      throw e
    }
    Log.d("User Repo", "User data initialized, ${user.value}")
  }

  override suspend fun getUserIds(userEmails: Set<String?>): Map<String, String> {
    if (userEmails.isEmpty()) return emptyMap()

    val emailToUserId = mutableMapOf<String, String>()
    val chunks = userEmails.filterNotNull().chunked(10)

    for (chunk in chunks) {
      val query = db.collection("users").whereIn("email", chunk).get().await()
      for (doc in query.documents) {
        val email = doc.getString("email")
        val userId = doc.id
        if (email != null) emailToUserId[email] = userId
      }
    }

    return emailToUserId
  }

  override suspend fun getUserEmails(userIds: List<String>): Map<String, String> {
    if (userIds.isEmpty()) return emptyMap()

    val uidToEmail = mutableMapOf<String, String>()
    val chunks = userIds.chunked(10)

    for (chunk in chunks) {
      val query = db.collection("users").whereIn(FieldPath.documentId(), chunk).get().await()
      for (doc in query.documents) {
        val email = doc.getString("email")
        val userId = doc.id
        if (email != null) uidToEmail[userId] = email
      }
    }
    return uidToEmail
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
              val currentUserData =
                  _user.value
                      ?: User(
                          uid = currentUser.uid,
                          username = "",
                          email = "",
                          selectedHouseholdUID = "")
              _user.value = currentUserData.copy(invitationUIDs = invitationsList)
              Log.d("user repo", "start listening for invitations, user: ${user.value}")
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

    val currentUserData =
        _user.value
            ?: User(uid = currentUser.uid, username = "", email = "", selectedHouseholdUID = "")
    val updatedUserData =
        when (fieldName) {
          "username" -> currentUserData.copy(username = value as String)
          "photoURL" -> currentUserData.copy(photoUrl = value as String)
          "email" -> currentUserData.copy(email = value as String)
          "selectedHouseholdUID" -> currentUserData.copy(selectedHouseholdUID = value as String)
          else -> currentUserData
        }
    _user.value = updatedUserData
  }

  private suspend fun updateArrayField(
      fieldName: String,
      value: String,
      operation: ArrayOperation
  ) {
    val currentUser = firebaseAuth.currentUser ?: throw Exception("User not logged in")
    val updateValue =
        when (operation) {
          ArrayOperation.ADD -> FieldValue.arrayUnion(value)
          ArrayOperation.REMOVE -> FieldValue.arrayRemove(value)
        }

    val currentUserData =
        _user.value
            ?: User(uid = currentUser.uid, username = "", email = "", selectedHouseholdUID = "")
    val updatedArray =
        when (fieldName) {
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

    val updatedUserData =
        when (fieldName) {
          "householdUIDs" -> currentUserData.copy(householdUIDs = updatedArray)
          "recipeUIDs" -> currentUserData.copy(recipeUIDs = updatedArray)
          else -> currentUserData
        }
    _user.value = updatedUserData
    userCollection.document(currentUser.uid).update(fieldName, updateValue).await()
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

  override suspend fun selectHousehold(householdUid: String?) {
    householdUid?.let { updateSelectedHousehold(it) }
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
          invitationUIDs = invitationUIDs)
    } catch (e: Exception) {
      Log.e("HouseholdRepository", "Error converting document to User", e)
      null
    }
  }
}
