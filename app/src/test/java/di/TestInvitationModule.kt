package di

import com.android.shelfLife.model.newInvitations.InvitationRepository
import com.android.shelfLife.model.newInvitations.InvitationRepositoryFirestore
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
object TestInvitationModule {

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
  fun provideInvitationRepository(db: FirebaseFirestore, auth: FirebaseAuth): InvitationRepository {
    return InvitationRepositoryFirestore(db, auth)
  }
}
