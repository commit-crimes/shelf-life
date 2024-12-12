package com.android.shelflife.di

import com.android.shelfLife.di.HouseholdRepositoryModule
import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.newhousehold.HouseholdRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [HouseholdRepositoryModule::class]
)
object TestHouseHoldModule {

  @Provides
  @Singleton
  fun provideHouseHoldRepository(firestore: FirebaseFirestore): HouseHoldRepository {
    return HouseholdRepositoryFirestore(firestore)
  }
}
