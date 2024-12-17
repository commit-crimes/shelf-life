package com.android.shelflife.di

import com.android.shelfLife.di.CoroutineScopeModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CoroutineScopeModule::class]
)
object TestCoroutineScopeModule {

    @Singleton
    @Provides
    fun provideTestCoroutineScope(): CoroutineScope {
        val testDispatcher = StandardTestDispatcher()
        return TestScope(SupervisorJob() + testDispatcher)
    }
}

