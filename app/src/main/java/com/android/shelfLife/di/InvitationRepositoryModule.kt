// InvitationRepositoryModule.kt
package com.android.shelfLife.di

import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InvitationRepositoryModule {

  @Singleton
  @Provides
  fun provideInvitationRepository(db: FirebaseFirestore, auth: FirebaseAuth): InvitationRepository {
    return InvitationRepositoryFirestore(db, auth)
  }
}