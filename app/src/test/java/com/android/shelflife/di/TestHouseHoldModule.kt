import com.android.shelfLife.model.newhousehold.HouseHoldRepository
import com.android.shelfLife.model.newhousehold.HouseholdRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.mockito.Mockito.mock

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
