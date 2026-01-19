package com.ezycart.domain.usecase

import com.ezycart.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class GetCartIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<String> {
        return authRepository.getCartId()
    }
}