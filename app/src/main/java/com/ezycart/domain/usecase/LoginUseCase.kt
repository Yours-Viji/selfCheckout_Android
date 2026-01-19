package com.ezycart.domain.usecase

import com.ezycart.data.remote.dto.DeviceDetailsResponse
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.domain.model.User
import com.ezycart.domain.repository.AuthRepository
import com.ezycart.model.CartActivationResponse
import com.ezycart.model.EmployeeLoginResponse
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(employeePin: String): NetworkResponse<EmployeeLoginResponse> {
        return authRepository.login(employeePin)
    }

    suspend  fun activate(deviceId: String, trolleyNumber: String): NetworkResponse<CartActivationResponse> {
        return authRepository.activateDevice(deviceId, trolleyNumber)
    }

    suspend  fun deviceDetails(deviceId: String): NetworkResponse<DeviceDetailsResponse> {
        return authRepository.getDeviceDetails(deviceId)
    }
}