package di

import android.util.Log
import com.android.shelfLife.di.UserRepositoryModule
import com.android.shelfLife.model.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UserRepositoryModule::class])
object TestUserRepositoryModule {
  @Singleton
  @Provides
  fun provideUserRepository(): UserRepository {
    Log.d("TestUserRepositoryModule", "provideUserRepository")
    val userRepository = mock(UserRepository::class.java)
    val bypassLogin = MutableStateFlow<Boolean>(true)
    whenever(userRepository.bypassLogin).thenReturn(bypassLogin.asStateFlow())
    return userRepository
  }
}
