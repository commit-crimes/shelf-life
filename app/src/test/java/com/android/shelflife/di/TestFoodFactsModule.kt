package com.android.shelflife.di

import com.android.shelfLife.di.FoodFactsRepositoryModule
import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.OpenFoodFactsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [FoodFactsRepositoryModule::class])
object TestFoodFactsModule {

  @Provides
  fun provideFoodFactsRepository(): FoodFactsRepository {
    return OpenFoodFactsRepository(mock(), mock())
  }
}
