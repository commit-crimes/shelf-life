package com.android.shelfLife.model.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
class FirestoreModule {
  @Singleton
  @Provides
  fun provideFirestore(): FirebaseFirestore {
    return FirebaseFirestore.getInstance()
  }

  @Singleton
  @Provides
  fun provideFirebaseAuth(): FirebaseAuth {
    return FirebaseAuth.getInstance()
  }
}
