package di

import com.android.shelfLife.di.FoodItemRepositoryModule
import com.android.shelfLife.di.HouseholdRepositoryModule
import com.android.shelfLife.model.foodItem.FoodItemRepository
import com.android.shelfLife.model.foodItem.FoodItemRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FoodItemRepositoryModule::class]
)
object TestFoodItemRepository {
    @Singleton
    @Provides
    fun provideFoodItemRepository(): FoodItemRepository {
        return mock(FoodItemRepository::class.java)
    }
}