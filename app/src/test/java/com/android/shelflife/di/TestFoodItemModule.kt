package com.android.shelflife.di

import com.android.shelfLife.di.FoodItemRepositoryModule
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [FoodItemRepositoryModule::class])
object TestFoodItemModule {

  @Provides
  fun provideFoodItemRepository(db: FirebaseFirestore): FoodItemRepository {
    return FoodItemRepositoryFirestore(db)
  }
}
