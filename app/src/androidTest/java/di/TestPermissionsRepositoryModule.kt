package di

import com.android.shelfLife.di.PermissionRepositoryModule
import com.android.shelfLife.model.permission.PermissionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import org.mockito.Mockito.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [PermissionRepositoryModule::class])
object TestPermissionsRepositoryModule {

  @Provides
  @Singleton
  fun providePermissionRepository(): PermissionRepository {
    return mock(PermissionRepository::class.java)
  }
}
