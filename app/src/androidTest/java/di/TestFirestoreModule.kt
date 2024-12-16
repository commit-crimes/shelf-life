package di

import com.android.shelfLife.di.FirestoreModule
import com.android.shelfLife.di.HouseholdRepositoryModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirestoreModule::class]
)
object TestFirestoreModule {
    @Provides
    @Singleton
    fun provideMockFirestore(): FirebaseFirestore {
        return mock(FirebaseFirestore::class.java)
    }

    @Provides
    @Singleton
    fun provideMockFirebaseAuth(): FirebaseAuth {
        return mock(FirebaseAuth::class.java)
    }
}