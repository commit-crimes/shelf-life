package com.android.shelfLife.model.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
class UserRepositoryModule {
  @Singleton
  @Provides
  fun provideUserRepository(db: FirebaseFirestore, auth: FirebaseAuth): UserRepository {
    return UserRepositoryFirestore(db, auth)
  }
}
