import com.android.shelfLife.model.household.HouseHoldRepository
import com.android.shelfLife.model.household.HouseholdRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestHouseHoldModule {

    @Provides
    @Singleton
    fun provideMockFirestore(): FirebaseFirestore {
        return mock(FirebaseFirestore::class.java)
    }

    @Provides
    @Singleton
    fun provideHouseHoldRepository(firestore: FirebaseFirestore): HouseHoldRepository {
        return HouseholdRepositoryFirestore(firestore)
    }
}