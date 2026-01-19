package com.ezycart.data.remote.dto

data class WavPayQrPaymentStatus(
    val approvalCode: String,
    val orderId: String,
    val status: String,
    val statusMessage: String,
    val transactionDate: String
)