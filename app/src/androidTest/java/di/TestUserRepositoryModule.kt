package di

import android.util.Log
import com.android.shelfLife.di.UserRepositoryModule
import com.android.shelfLife.model.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UserRepositoryModule::class])
object TestUserRepositoryModule {
  @Singleton
  @Provides
  fun provideUserRepository(): UserRepository {
    Log.d("TestUserRepositoryModule", "provideUserRepository")
    val userRepository = mockk<UserRepository>(relaxed = true)
    val bypassLogin = MutableStateFlow(true)
    every { userRepository.bypassLogin } returns bypassLogin.asStateFlow()
    return userRepository
  }
}
