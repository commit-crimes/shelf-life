package com.android.shelfLife.di

import android.content.Context
import com.android.shelfLife.model.permission.PermissionRepository
import com.android.shelfLife.model.permission.SharedPrefPermissionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionRepositoryModule {

  @Singleton
  @Provides
  fun providePermissionRepository(@ApplicationContext appContext: Context): PermissionRepository {
    return SharedPrefPermissionRepository(appContext)
  }
}
