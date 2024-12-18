package di

import com.android.shelfLife.di.RecipeRepositoryModule
import com.android.shelfLife.model.recipe.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import org.mockito.Mockito.mock

@Module
@TestInstallIn(
  components = [SingletonComponent::class], replaces = [RecipeRepositoryModule::class])
object TestRecipeRepositoryModule {
  @Provides
  @Singleton
  fun provideTestRecipeRepository(): RecipeRepository {
    return mock(RecipeRepository::class.java)
  }
}
