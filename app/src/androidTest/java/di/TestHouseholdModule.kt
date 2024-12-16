package di

import android.util.Log
import com.android.shelfLife.di.HouseholdRepositoryModule
import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [HouseholdRepositoryModule::class]
)
object TestHouseholdModule {

    @Provides
    @Singleton
    fun provideHouseHoldRepository(): HouseHoldRepository {
        return mock(HouseHoldRepository::class.java)
    }
}