package com.android.shelfLife.di

import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.OpenFoodFactsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton


@Module
@InstallIn(ActivityComponent::class)
class FoodFactsRepositoryModule {
  @Singleton
  @Provides
  fun provideFoodFactsRepository(client : OkHttpClient): FoodFactsRepository {
    return OpenFoodFactsRepository(client)
  }
}