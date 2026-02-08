package com.ezycart.payment.terminal

sealed class PaymentState {
    object Idle : PaymentState()
    object Processing : PaymentState()
    data class ShowQr(val qrData: String) : PaymentState()
    object Success : PaymentState()
    data class Failed(val reason: String) : PaymentState()
    object Timeout : PaymentState()
}
