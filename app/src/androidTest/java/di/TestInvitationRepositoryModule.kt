package di

import com.android.shelfLife.di.InvitationRepositoryModule
import com.android.shelfLife.model.invitations.InvitationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import org.mockito.Mockito.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [InvitationRepositoryModule::class])
object TestInvitationRepositoryModule {
  @Singleton
  @Provides
  fun provideInvitationRepository(): InvitationRepository {
    return mock(InvitationRepository::class.java)
  }
}
