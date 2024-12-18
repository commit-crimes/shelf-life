package com.android.shelflife.di

import com.android.shelfLife.di.InvitationRepositoryModule
import com.android.shelfLife.model.invitations.InvitationRepository
import com.android.shelfLife.model.invitations.InvitationRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [InvitationRepositoryModule::class])
object TestInvitationModule {

  @Provides
  @Singleton
  fun provideInvitationRepository(db: FirebaseFirestore, auth: FirebaseAuth): InvitationRepository {
    return InvitationRepositoryFirestore(db, auth)
  }
}
