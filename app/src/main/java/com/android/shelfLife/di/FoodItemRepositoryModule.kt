// FoodItemRepositoryModule.kt
package com.android.shelfLife.di

import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FoodItemRepositoryModule {

  @Singleton
  @Provides
  fun provideFoodItemRepository(db: FirebaseFirestore): FoodItemRepository {
    return FoodItemRepositoryFirestore(db)
  }
}
