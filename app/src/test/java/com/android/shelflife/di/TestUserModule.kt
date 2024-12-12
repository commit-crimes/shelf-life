package com.android.shelflife.di

import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.model.user.UserRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.mockito.Mockito.*

@Module
@InstallIn(SingletonComponent::class)
object TestUserModule {

  @Provides
  @Singleton
  fun provideMockFirestore(): FirebaseFirestore {
    return mock(FirebaseFirestore::class.java)
  }

  @Provides
  @Singleton
  fun provideMockFirebaseAuth(): FirebaseAuth {
    return mock(FirebaseAuth::class.java)
  }

  @Provides
  @Singleton
  fun provideUserRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): UserRepository {
    return UserRepositoryFirestore(firestore, auth)
  }
}
