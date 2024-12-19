package di

import com.android.shelfLife.di.FoodFactsRepositoryModule
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import org.mockito.Mockito.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [FoodFactsRepositoryModule::class])
object TestFoodFactsRepositoryModule {

  @Provides
  @Singleton
  fun provideFoodFactsRepository(): FoodFactsRepository {
    return mock(FoodFactsRepository::class.java)
  }
}
