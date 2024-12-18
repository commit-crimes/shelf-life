package di

import com.android.shelfLife.di.RecipeGeneratorRepositoryModule
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import org.mockito.Mockito.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [RecipeGeneratorRepositoryModule::class])
object TestRecipeGeneratorRepositoryModule {
  @Provides
  @Singleton
  fun provideTestRecipeGeneratorRepository(): RecipeGeneratorRepository {
    return mock(RecipeGeneratorRepository::class.java)
  }
}
