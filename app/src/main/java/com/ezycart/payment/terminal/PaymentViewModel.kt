package com.ezycart.payment.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/*
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repo: GhlPaymentRepository
) : ViewModel() {

    val paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)

    private var currentInvoice: String = ""

    private val MID = "900768"
    private val TID = "62911768"

    // -------------------------------
    // CARD / TAP PAYMENT
    // -------------------------------
    fun payByCard(amount: String) {
        viewModelScope.launch {
            paymentState.value = PaymentState.Processing
            currentInvoice = generateInvoice()

            try {
                val response = repo.payment(
                    PaymentRequest(
                        mid = MID,
                        tid = TID,
                        invoiceNo = currentInvoice,
                        amount = amount
                    )
                )

                handlePaymentResponse(response)

            } catch (e: Exception) {
                paymentState.value = PaymentState.Failed(e.message ?: "Error")
            }
        }
    }

    // -------------------------------
    // QR PAYMENT
    // -------------------------------
    fun payByQr(amount: String) {
        viewModelScope.launch {
            paymentState.value = PaymentState.Processing
            currentInvoice = generateInvoice()

            try {
                val response = repo.payment(
                    PaymentRequest(
                        mid = MID,
                        tid = TID,
                        invoiceNo = currentInvoice,
                        amount = amount,
                        productCode = "QR" // depends on GHL config
                    )
                )

                handlePaymentResponse(response)

            } catch (e: Exception) {
                paymentState.value = PaymentState.Failed(e.message ?: "Error")
            }
        }
    }

    // -------------------------------
    // RESPONSE HANDLER
    // -------------------------------
    private suspend fun handlePaymentResponse(response: String) {

        // VERY SIMPLE parsing (improve later)
        when {
            response.contains("\"txnMode\":\"1\"") -> {
                val qrData = extractQrData(response)
                paymentState.value = PaymentState.ShowQr(qrData)
                pollTransaction()
            }

            response.contains("\"respCode\":\"00\"") -> {
                paymentState.value = PaymentState.Success
            }

            else -> {
                paymentState.value = PaymentState.Failed(response)
            }
        }
    }

    // -------------------------------
    // POLLING FOR QR
    // -------------------------------
    private suspend fun pollTransaction() {
        repeat(30) { // ~2 minutes
            delay(4000)

            val result = repo.queryTxn(currentInvoice)

            when {
                result.contains("\"SUCCESS\"") -> {
                    paymentState.value = PaymentState.Success
                    return
                }
                result.contains("\"FAIL\"") -> {
                    paymentState.value = PaymentState.Failed("Payment Failed")
                    return
                }
            }
        }

        paymentState.value = PaymentState.Timeout
    }

    private fun generateInvoice(): String =
        System.currentTimeMillis().toString()

    private fun extractQrData(response: String): String {
        // TEMP – improve with JSON parsing
        val key = "\"displayInfo\":\""
        val start = response.indexOf(key) + key.length
        val end = response.indexOf("\"", start)
        return response.substring(start, end)
    }
}
*/




