package di

import com.android.shelfLife.di.FoodItemRepositoryModule
import com.android.shelfLife.di.UserRepositoryModule
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [UserRepositoryModule::class]
)
object TestUserRepositoryModule {
    @Singleton
    @Provides
    fun provideUserRepository(): UserRepository {
        return mock(UserRepository::class.java)
    }
}