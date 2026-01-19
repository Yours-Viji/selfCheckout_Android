package com.ezycart.di

import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.data.remote.api.AuthApi
import com.ezycart.data.repository.AuthRepositoryImpl
import com.ezycart.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        preferencesManager: PreferencesManager,
    ): AuthRepository {
        return AuthRepositoryImpl(authApi, preferencesManager)
    }

}