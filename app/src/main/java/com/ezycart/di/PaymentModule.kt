package com.ezycart.di

import com.ezycart.payment.nearpay.NearPayService
import com.ezycart.payment.nearpay.NearPayServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    @Singleton
    fun provideNearPayService(): NearPayService {
        return NearPayServiceImpl()
    }
}