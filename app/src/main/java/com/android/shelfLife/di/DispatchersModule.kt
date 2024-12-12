package com.android.shelfLife.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Define the qualifier
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

  @IoDispatcher @Provides @Singleton fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
