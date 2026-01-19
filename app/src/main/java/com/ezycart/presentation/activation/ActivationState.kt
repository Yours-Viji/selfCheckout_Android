package com.ezycart.presentation.activation

import com.ezycart.domain.model.AppMode

data class ActivationState(
    val activationCode: String = "",
    val trolleyNumber: String = "01",
    val appMode: AppMode = AppMode.EzyLite,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isActivationSuccessful: Boolean = false
)