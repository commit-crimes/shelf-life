// FoodFactsRepositoryModule.kt
package com.android.shelfLife.di

import com.android.shelfLife.model.foodFacts.FoodFactsRepository
import com.android.shelfLife.model.foodFacts.OpenFoodFactsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FoodFactsRepositoryModule {

  @Singleton
  @Provides
  fun provideFoodFactsRepository(client: OkHttpClient): FoodFactsRepository {
    return OpenFoodFactsRepository(client)
  }
}