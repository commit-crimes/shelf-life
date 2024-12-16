package com.android.shelfLife.di

import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecipeRepositoryModule {

  @Singleton
  @Provides
  fun provideRecipeRepository(db: FirebaseFirestore): RecipeRepository {
    return RecipeRepositoryFirestore(db)
  }
}
