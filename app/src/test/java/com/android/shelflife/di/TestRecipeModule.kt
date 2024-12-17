package com.android.shelflife.di

import com.android.shelfLife.di.RecipeRepositoryModule
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.recipe.RecipeRepositoryFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [RecipeRepositoryModule::class]
)
object TestRecipeRepositoryModule {

    @Provides
    @Singleton
    fun provideRecipeRepository(db: FirebaseFirestore): RecipeRepository {
        return RecipeRepositoryFirestore(db)
    }
}
