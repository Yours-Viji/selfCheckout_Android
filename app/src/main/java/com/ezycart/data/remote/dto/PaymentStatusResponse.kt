package com.ezycart.data.remote.dto

data class PaymentStatusResponse(
    val approvalCode: String,
    val orderId: String,
    val status: Int,
    val statusMessage: String,
    val transactionDate:String,
    val transactionRef:String
)