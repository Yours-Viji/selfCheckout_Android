package com.ezycart.payment.terminal

import android.content.Context
import kotlinx.serialization.encodeToString

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GhlPaymentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val http = GhlHttpClient(context)
    private val signer = JoseSigner(KeyLoader.loadPrivateKey(context))

    val paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    private var currentInvoice: String = ""

    private val MID = "900768"
    private val TID = "62911768"

    suspend fun payByCard(amount: String) {
        paymentState.value = PaymentState.Processing
        currentInvoice = System.currentTimeMillis().toString()

        try {
            val response = payment(
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

    suspend fun payByQr(amount: String) {
        paymentState.value = PaymentState.Processing
        currentInvoice = System.currentTimeMillis().toString()

        try {
            val response = payment(
                PaymentRequest(
                    mid = MID,
                    tid = TID,
                    invoiceNo = currentInvoice,
                    amount = amount,
                    productCode = "QR"
                )
            )
            handlePaymentResponse(response)
        } catch (e: Exception) {
            paymentState.value = PaymentState.Failed(e.message ?: "Error")
        }
    }
    private suspend fun payment(request: PaymentRequest): String {
        val json = kotlinx.serialization.json.Json.encodeToString(request)
        val signed = signer.sign(json)

        repeat(3) { attempt ->
            try {
                return http.post("https://sandbox.ghlapps.com/epay/paymentRequest", signed)
            } catch (e: IOException) {
                if (attempt == 2) throw e
                kotlinx.coroutines.delay(2000) // wait 2s before retry
            }
        }
        throw IOException("Payment request failed after retries")
    }
    /*private suspend fun payment(request: PaymentRequest): String {
        val json = kotlinx.serialization.json.Json.encodeToString(request)
        val signed = signer.sign(json)
        return http.post("https://sandbox.ghlapps.com/epay/paymentRequest", signed)
    }*/

    private suspend fun queryTxn(invoiceNo: String): String {
        val payload = """{"invoiceNo":"$invoiceNo"}"""
        val signed = signer.sign(payload)
        return http.post("https://sandbox.ghlapps.com/epay/queryTxnRequest", signed)
    }

    private suspend fun handlePaymentResponse(response: String) {
        when {
            response.contains("\"txnMode\":\"1\"") -> {
                val qrData = extractQrData(response)
                paymentState.value = PaymentState.ShowQr(qrData)
                pollTransaction()
            }
            response.contains("\"respCode\":\"00\"") -> paymentState.value = PaymentState.Success
            else -> paymentState.value = PaymentState.Failed(response)
        }
    }

    private suspend fun pollTransaction() {
        repeat(30) {
            kotlinx.coroutines.delay(4000)
            val result = queryTxn(currentInvoice)
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

    private fun extractQrData(response: String): String {
        val key = "\"displayInfo\":\""
        val start = response.indexOf(key) + key.length
        val end = response.indexOf("\"", start)
        return response.substring(start, end)
    }
}
