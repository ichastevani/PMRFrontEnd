package com.example.myapplication.module

import com.example.myapplication.data.AppContainer
import com.example.myapplication.data.PMRRepository
import com.example.myapplication.network.DefaultAppContainer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PMRModule {
    @Provides
    fun provideAppContainer(): AppContainer {
        return DefaultAppContainer()
    }

    @Provides
    fun providePMRRepository(appContainer: AppContainer): PMRRepository {
        return appContainer.pmrRepository
    }
}
