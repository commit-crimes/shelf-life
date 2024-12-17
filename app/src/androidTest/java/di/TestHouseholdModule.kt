package di

import com.android.shelfLife.di.HouseholdRepositoryModule
import com.android.shelfLife.model.household.HouseHoldRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import org.mockito.Mockito.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [HouseholdRepositoryModule::class])
object TestHouseholdModule {

  @Provides
  @Singleton
  fun provideHouseHoldRepository(): HouseHoldRepository {
    return mock(HouseHoldRepository::class.java)
  }
}
