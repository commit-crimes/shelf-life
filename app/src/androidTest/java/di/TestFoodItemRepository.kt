package di

import com.android.shelfLife.di.FoodItemRepositoryModule
import com.android.shelfLife.model.foodItem.FoodItemRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import org.mockito.Mockito.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [FoodItemRepositoryModule::class])
object TestFoodItemRepository {
  @Singleton
  @Provides
  fun provideFoodItemRepository(): FoodItemRepository {
    return mock(FoodItemRepository::class.java)
  }
}
