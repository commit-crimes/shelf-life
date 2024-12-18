package di

import com.android.shelfLife.di.HouseholdRepositoryModule
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [HouseholdRepositoryModule::class])
object TestHouseholdModule {

  @Provides
  @Singleton
  fun provideHouseHoldRepository(): HouseHoldRepository {
    return mockk<HouseHoldRepository>(relaxed = true)
  }
}
