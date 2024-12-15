package com.android.shelflife.di

import com.android.shelfLife.di.FirestoreModule
import com.android.shelfLife.di.UserRepositoryModule
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirestoreModule::class]
)
object TestFirestoreModule {

    @Provides
    @Singleton
    fun provideMockFirestore(): FirebaseFirestore {
        val mockFirestore = mock(FirebaseFirestore::class.java)
        val mockCollection = mock(CollectionReference::class.java)
        val mockDocument = mock(DocumentReference::class.java)
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)
        val mockSnapshot = mock(DocumentSnapshot::class.java)


        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockFirestore.collection("invitations")).thenReturn(mockCollection)
        `when`(mockFirestore.collection("households")).thenReturn(mockCollection)

        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
        `when`(mockCollection.document()).thenAnswer {
            val mockNewDocument = mock(DocumentReference::class.java)
            `when`(mockNewDocument.id).thenReturn("generatedMockDocId")
            `when`(mockNewDocument.set(any())).thenReturn(Tasks.forResult(null))
            mockNewDocument
        }

        `when`(mockCollection.whereIn(eq(FieldPath.documentId()), anyList())).thenReturn(mockQuery)

        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockSnapshot))

        doReturn(Tasks.forResult(mockSnapshot)).`when`(mockDocument).get()
        `when`(mockDocument.id).thenReturn("mockDocId")

        `when`(mockSnapshot.exists()).thenReturn(true)
        `when`(mockSnapshot.getString("invitationId")).thenReturn("mockInvitationId")
        `when`(mockSnapshot.getString("householdId")).thenReturn("mockHouseholdId")
        `when`(mockSnapshot.getString("householdName")).thenReturn("Mock Household Name")
        `when`(mockSnapshot.getString("invitedUserId")).thenReturn("mockInvitedUserId")
        `when`(mockSnapshot.getString("inviterUserId")).thenReturn("mockInviterUserId")
        `when`(mockSnapshot.getTimestamp("timestamp")).thenReturn(Timestamp.now())

        val mockUpdateTask = Tasks.forResult<Void>(null)
        `when`(mockDocument.update(anyString(), any())).thenReturn(mockUpdateTask)
        `when`(mockDocument.set(any(), any())).thenReturn(mockUpdateTask)
        `when`(mockSnapshot.exists()).thenReturn(true)
        `when`(mockSnapshot.getData()).thenReturn(mapOf(
            "username" to "Test User",
            "email" to "test@example.com"
        ))
        return mockFirestore
    }

    @Provides
    @Singleton
    fun provideMockFirebaseAuth(): FirebaseAuth {
        val firebaseAuth = mock(FirebaseAuth::class.java)
        val mockFirebaseUser = mock(FirebaseUser::class.java)
        `when`(mockFirebaseUser.uid).thenReturn("testUserId")
        `when`(firebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        return firebaseAuth
    }
}