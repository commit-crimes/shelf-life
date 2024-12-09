package com.android.shelfLife.model.foodItem

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
class FoodItemRepositoryModule {
  @Singleton
  @Provides
  fun provideFoodItemRepository(db: FirebaseFirestore): FoodItemRepository {
    return FoodItemRepositoryFirestore(db)
  }
}
