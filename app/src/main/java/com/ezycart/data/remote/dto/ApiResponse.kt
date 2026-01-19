package com.ezycart.data.remote.dto

import com.google.gson.annotations.SerializedName

// 1. Generic API response wrapper
data class ApiResponse<T>(
    val data: T? = null,
    val error: ApiError? = null,
    val message: String? = null
)

data class ApiError(
    val code: Int? = null,
    val message: String? = null
)

// 2. Sealed result wrapper
sealed class NetworkResponse<out T> {
    data class Success<T>(val data: T) : NetworkResponse<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResponse<Nothing>()
}

data class GenericErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val publicMessage: String? = null,
    val key: String? = null
)
