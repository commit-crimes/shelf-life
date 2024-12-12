import com.android.shelfLife.model.newFoodItem.FoodItemRepository
import com.android.shelfLife.model.newFoodItem.FoodItemRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.mockito.Mockito

@Module
@InstallIn(SingletonComponent::class)
object TestFoodItemModule {

  @Provides
  fun provideMockFirestore(): FirebaseFirestore {
    return Mockito.mock(FirebaseFirestore::class.java)
  }

  @Provides
  fun provideFoodItemRepository(db: FirebaseFirestore): FoodItemRepository {
    return FoodItemRepositoryFirestore(db)
  }
}
