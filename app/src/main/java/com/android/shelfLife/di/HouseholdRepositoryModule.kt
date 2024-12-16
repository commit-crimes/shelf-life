// HouseholdRepositoryModule.kt
package com.android.shelfLife.di

import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HouseholdRepositoryModule {

  @Singleton
  @Provides
  fun provideHouseholdRepository(db: FirebaseFirestore): HouseHoldRepository {
    return HouseholdRepositoryFirestore(db)
  }
}
