package com.ezycart.payment.nearpay

import io.nearpay.sdk.utils.enums.TransactionData

interface NearPaymentListener {
    fun onPaymentSuccess(transactionData: TransactionData)
    fun onPaymentFailed(error: String)
}

sealed class PaymentStatus {
    data class onPaymentSuccess(val transactionData: TransactionData) : PaymentStatus()
    data class onPaymentFailed(val error: String) : PaymentStatus()
}