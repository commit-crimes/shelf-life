package com.android.shelflife.di

import com.android.shelfLife.di.FirestoreModule
import com.android.shelfLife.di.UserRepositoryModule
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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
        val mockSnapshot = mock(DocumentSnapshot::class.java)

        // More explicit mocking
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)

        // Use doReturn instead of when for more reliable mocking
        doReturn(Tasks.forResult(mockSnapshot)).`when`(mockDocument).get()
        `when`(mockDocument.id).thenReturn("mockDocId")
        val mockUpdateTask = Tasks.forResult<Void>(null)
        `when`(mockDocument.update(anyString(), any())).thenReturn(mockUpdateTask)
        `when`(mockDocument.set(any(), any())).thenReturn(mockUpdateTask)
        // Ensure snapshot returns expected values
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