package com.android.shelfLife.model.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  private val auth = FirebaseAuth.getInstance()
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

  override suspend fun initializeUserData() {
    val currentUser = auth.currentUser ?: throw Exception("User not logged in")
    try {
      // Fetch user data from Firestore
      val snapshot = userCollection.document(currentUser.uid).get().await()
      if (snapshot.exists()) {
        val userData = snapshot.toObject(User::class.java)
        _user.value = userData
        _invitations.value = userData?.invitationUIDs ?: emptyList()
      } else {
        _user.value = null
        _invitations.value = emptyList()
      }
    } catch (e: Exception) {
      Log.e("UserRepository", "Error initializing user data", e)
      _user.value = null
      _invitations.value = emptyList()
      throw e
    }
  }

  override fun startListeningForInvitations() {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      // Remove any existing listener
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

              // Optionally update the invitations in the local _user variable
              val currentUserData =
                  _user.value ?: User(uid = currentUser.uid, username = "", email = "")
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

  // Helper function to update local user data and Firestore
  private suspend fun updateUserField(fieldName: String, value: Any) {
    val currentUser = auth.currentUser ?: throw Exception("User not logged in")
    userCollection.document(currentUser.uid).update(fieldName, value).await()

    // Update local _user variable
    val currentUserData = _user.value ?: User(uid = currentUser.uid, username = "", email = "")
    val updatedUserData =
        when (fieldName) {
          "username" -> currentUserData.copy(username = value as String)
          "email" -> currentUserData.copy(email = value as String)
          else -> currentUserData
        }
    _user.value = updatedUserData
  }

  // Helper function to update array fields and local user data
  private suspend fun updateArrayField(
      fieldName: String,
      value: String,
      operation: ArrayOperation
  ) {
    val currentUser = auth.currentUser ?: throw Exception("User not logged in")
    val updateValue =
        when (operation) {
          ArrayOperation.ADD -> FieldValue.arrayUnion(value)
          ArrayOperation.REMOVE -> FieldValue.arrayRemove(value)
        }
    userCollection.document(currentUser.uid).update(fieldName, updateValue).await()

    // Update local _user variable
    val currentUserData = _user.value ?: User(uid = currentUser.uid, username = "", email = "")
    val updatedArray =
        when (fieldName) {
          "householdUIDs" -> {
            val currentList = currentUserData.householdUIDs
            when (operation) {
              ArrayOperation.ADD -> currentList + value
              ArrayOperation.REMOVE -> currentList - value
            }
          }
          "recipeUIDs" -> {
            val currentList = currentUserData.recipeUIDs
            when (operation) {
              ArrayOperation.ADD -> currentList + value
              ArrayOperation.REMOVE -> currentList - value
            }
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

  override suspend fun addRecipeUID(recipeUID: String) {
    updateArrayField("recipeUIDs", recipeUID, ArrayOperation.ADD)
  }

  override suspend fun deleteRecipeUID(uid: String) {
    updateArrayField("recipeUIDs", uid, ArrayOperation.REMOVE)
  }

  override suspend fun deleteInvitationUID(uid: String) {
    // Update Firestore
    updateArrayField("invitationUIDs", uid, ArrayOperation.REMOVE)
    // No need to update local _user or _invitations, as listener will handle it
  }

  override suspend fun updateUsername(username: String) {
    updateUserField("username", username)
  }

  override suspend fun updateEmail(email: String) {
    val currentUser = auth.currentUser ?: throw Exception("User not logged in")
    // Update email in FirebaseAuth
    currentUser.updateEmail(email).await()
    // Update email in Firestore
    updateUserField("email", email)
  }
}
