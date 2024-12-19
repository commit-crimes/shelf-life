package di

import com.android.shelfLife.di.RecipeRepositoryModule
import com.android.shelfLife.model.recipe.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RecipeRepositoryModule::class])
object TestRecipeRepositoryModule {
  @Singleton
  @Provides
  fun provideRecipeRepository(): RecipeRepository {
    return mockk<RecipeRepository>(relaxed = true)
  }
}
