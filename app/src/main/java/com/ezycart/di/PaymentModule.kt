package com.ezycart.di

import android.content.Context
import com.ezycart.payment.nearpay.NearPayService
import com.ezycart.payment.nearpay.NearPayServiceImpl
import com.ezycart.payment.terminal.GhlPaymentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideGhlPaymentRepository(@ApplicationContext context: Context): GhlPaymentRepository {
        return GhlPaymentRepository(context)
    }
}