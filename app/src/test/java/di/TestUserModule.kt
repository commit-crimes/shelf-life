package di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.mockito.Mockito.*

@Module
@InstallIn(SingletonComponent::class)
object TestUserModule {

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
