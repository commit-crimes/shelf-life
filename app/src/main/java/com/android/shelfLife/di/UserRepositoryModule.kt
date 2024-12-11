// UserRepositoryModule.kt
package com.android.shelfLife.di

import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.model.user.UserRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserRepositoryModule {

  @Singleton
  @Provides
  fun provideUserRepository(db: FirebaseFirestore, auth: FirebaseAuth,scope: CoroutineScope): UserRepository {
    return UserRepositoryFirestore(db, auth, scope)
  }
}
