package com.android.shelfLife.model.invitations

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
class InvitationRepositoryModule {
  @Singleton
  @Provides
  fun provideInvitationRepository(db: FirebaseFirestore, auth: FirebaseAuth): InvitationRepository {
    return InvitationRepositoryFirestore(db, auth)
  }
}
