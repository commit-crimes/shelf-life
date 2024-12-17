package com.android.shelfLife.di

import com.aallam.openai.client.OpenAI
import com.android.shelfLife.model.recipe.RecipeGeneratorOpenAIRepository
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecipeGeneratorRepositoryModule {

  @Singleton
  @Provides
  fun provideRecipeGeneratorRepository(openai: OpenAI): RecipeGeneratorRepository {
    return RecipeGeneratorOpenAIRepository(openai)
  }
}
