package com.android.shelfLife.di

import com.android.shelfLife.model.newRecipe.RecipeGeneratorOpenAIRepository
import com.android.shelfLife.model.newRecipe.RecipeGeneratorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecipeGeneratorRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRecipeGeneratorRepository(
        impl: RecipeGeneratorOpenAIRepository
    ): RecipeGeneratorRepository
}