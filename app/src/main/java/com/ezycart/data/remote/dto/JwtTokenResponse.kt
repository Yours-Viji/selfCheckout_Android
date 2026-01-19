package com.ezycart.data.remote.dto

data class JwtTokenResponse(
    val amount: String,
    val cartId: String,
    val enableReceiptUI: Boolean,
    val enableResersal: Boolean,
    val enableUIDismiss: Boolean,
    val finishTimeout: Int,
    val orderId: String,
    val referenceNumber: String,
    val requestId: String,
    val token: String
)