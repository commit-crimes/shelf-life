package di

import com.android.shelfLife.di.RecipeGeneratorRepositoryModule
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [RecipeGeneratorRepositoryModule::class])
object TestRecipeGeneratorRepositoryModule {
  @Singleton
  @Provides
  fun provideTestRecipeGeneratorRepository(): RecipeGeneratorRepository {
    return mockk<RecipeGeneratorRepository>(relaxed = true)
  }
}
