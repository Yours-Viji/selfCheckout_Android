package com.ezycart.di

import android.content.Context
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.services.usb.UsbListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UsbModule {

    @Provides
    @Singleton
    fun provideUsbListener(
        @ApplicationContext context: Context, // Hilt provides this automatically
        preferencesManager: PreferencesManager // Hilt finds this if it's already injectable
    ): UsbListener {
        return UsbListener(context, preferencesManager)
    }
}