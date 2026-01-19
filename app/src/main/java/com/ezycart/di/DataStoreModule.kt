package com.ezycart.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ezycart.data.datastore.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesManager(dataStore: DataStore<Preferences>): PreferencesManager {
        return PreferencesManager(dataStore)
    }
}