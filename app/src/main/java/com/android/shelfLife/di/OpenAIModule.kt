package com.android.shelfLife.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.android.shelfLife.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object OpenAIModule {

  @Singleton
  @Provides
  fun provideOpenAI(): OpenAI {
    return OpenAI(token = BuildConfig.OPENAI_API_KEY, timeout = Timeout(socket = 60.seconds))
  }

  @Singleton @Provides fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
