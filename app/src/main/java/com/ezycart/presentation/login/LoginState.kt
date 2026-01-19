package com.ezycart.presentation.login

data class LoginState(
    val employeePin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false
)