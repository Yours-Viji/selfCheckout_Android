package com.ezycart.presentation.payment

data class PaymentState(
    val paymentAmount: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPaymentSuccess: Boolean = false,
    val isPaymentStarted: Boolean = false
)