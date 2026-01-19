package com.ezycart.domain.usecase

import com.ezycart.data.remote.dto.CreateJwtTokenRequest
import com.ezycart.data.remote.dto.JwtTokenResponse
import com.ezycart.data.remote.dto.NearPaymentSessionResponse
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.data.remote.dto.ShoppingCartDetails
import com.ezycart.data.remote.dto.WavPayQrPaymentRequest
import com.ezycart.data.remote.dto.WavPayQrPaymentStatus
import com.ezycart.data.remote.dto.WavPayQrResponse
import com.ezycart.domain.repository.AuthRepository

import javax.inject.Inject

class PaymentUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

   /* suspend  fun invoke(){
        return authRepository.refreshInterceptor()
    }*/

    suspend  fun createNewJwtToken(jwtTokenRequest: CreateJwtTokenRequest): NetworkResponse<JwtTokenResponse> {
        return authRepository.createJwtToken(jwtTokenRequest)
    }

    suspend  fun createNearPaySession(): NetworkResponse<NearPaymentSessionResponse> {
        return authRepository.createNearPaySession()
    }

    suspend  fun initWavPayQRPayment(): NetworkResponse<WavPayQrResponse> {
        return authRepository.initWavPayQRPayment()
    }

    suspend  fun getWavPayQRPaymentStatus(): NetworkResponse<WavPayQrPaymentStatus> {
        return authRepository.getWavPayQRPaymentStatus()
    }


}