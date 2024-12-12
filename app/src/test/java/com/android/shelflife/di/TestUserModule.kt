package com.android.shelflife.di

import com.android.shelfLife.di.UserRepositoryModule
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.model.user.UserRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [UserRepositoryModule::class]
)
object TestUserModule {

  @Provides
  @Singleton
  fun provideUserRepository(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
  ): UserRepository {
    return UserRepositoryFirestore(firestore, auth)
  }
}
