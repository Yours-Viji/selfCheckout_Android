package com.ezycart.payment.terminal

import kotlinx.serialization.Serializable


@Serializable
data class PaymentRequest(
    val mid: String,
    val tid: String,
    val invoiceNo: String,
    val amount: String,
    val currency: String = "MYR",
    val accountNo: String? = null,
    val productCode: String? = null
)
