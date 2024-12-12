package com.android.shelfLife.di

import com.aallam.openai.client.OpenAI
import com.android.shelfLife.model.recipe.RecipeGeneratorOpenAIRepository
import com.android.shelfLife.model.recipe.RecipeGeneratorRepository
import com.android.shelfLife.model.user.UserRepository
import com.android.shelfLife.model.user.UserRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
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
  fun provideRecipeGeneratorRepository(openai: OpenAI): RecipeGeneratorRepository{
    return RecipeGeneratorOpenAIRepository(openai)
  }
}