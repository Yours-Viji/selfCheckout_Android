package com.ezycart.domain.usecase

import com.ezycart.domain.repository.AuthRepository

import javax.inject.Inject

class SaveAuthTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String) {
        authRepository.saveAuthToken(token)
    }
}