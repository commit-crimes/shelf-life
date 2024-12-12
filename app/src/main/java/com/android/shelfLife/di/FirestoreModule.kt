// FirestoreModule.kt
package com.android.shelfLife.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

  @Singleton @Provides fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

  @Singleton @Provides fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
