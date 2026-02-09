package com.ezycart.payment.maybank


import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.itbizflow.ecrsdkhelper.EcrSdkHelper
import com.itbizflow.ecrsdkhelper.EcrClient
import org.json.JSONObject

class PaymentTerminalManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: PaymentTerminalManager? = null

        fun getInstance(context: Context): PaymentTerminalManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PaymentTerminalManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val TAG = "PaymentTerminalManager"
    private var isSdkInitialized = false

    init {
        initializeSdk(context)
    }

    /**
     * Initialize the SDK - MUST be called before any other operations
     */
    private fun initializeSdk(context: Context) {
        try {
            if (!EcrSdkHelper.isInitialized()) {
                EcrSdkHelper.initializeSdk(context)
                isSdkInitialized = true
                Log.d(TAG, "SDK initialized successfully")
                Log.d(TAG, "SDK Version: ${EcrSdkHelper.getSdkHelperVersion()}")
            } else {
                isSdkInitialized = true
                Log.d(TAG, "SDK already initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SDK: ${e.message}")
            isSdkInitialized = false
        }
    }

    /**
     * Check if terminal is reachable
     */
    fun pingTerminal(terminalIp: String, terminalPort: Int, callback: (Boolean) -> Unit) {
        if (!validateSdk()) {
            callback(false)
            return
        }

        AsyncTask.execute {
            try {
                val isReachable = EcrSdkHelper.ping(terminalIp, terminalPort)
                Log.d(TAG, "Ping result for $terminalIp:$terminalPort: $isReachable")
                callback(isReachable)
            } catch (e: Exception) {
                Log.e(TAG, "Ping failed: ${e.message}")
                callback(false)
            }
        }
    }

    /**
     * Logon to terminal (establish connection)
     */
    fun logonToTerminal(terminalIp: String, terminalPort: Int, callback: (Boolean) -> Unit) {
        if (!validateSdk()) {
            callback(false)
            return
        }

        AsyncTask.execute {
            try {
                val success = EcrSdkHelper.logon(terminalIp, terminalPort)
                Log.d(TAG, "Logon result for $terminalIp:$terminalPort: $success")
                callback(success)
            } catch (e: Exception) {
                Log.e(TAG, "Logon failed: ${e.message}")
                callback(false)
            }
        }
    }

    /**
     * Perform Sale Transaction
     * @param amount Amount in cents (e.g., RM 10.00 = 1000)
     */
    fun performSale(
        terminalIp: String,
        terminalPort: Int,
        amount: Long,
        callback: (PaymentResult) -> Unit
    ) {
        if (!validateSdk()) {
            callback(PaymentResult.error("SDK not initialized"))
            return
        }

        if (amount <= 0) {
            callback(PaymentResult.error("Invalid amount"))
            return
        }

        AsyncTask.execute {
            try {
                Log.d(TAG, "Initiating sale transaction: $amount cents")

                val result = EcrSdkHelper.performSaleTransaction(terminalIp, terminalPort, amount)

                // Parse the response
                val paymentResult = parseEcrResult(result)
                Log.d(TAG, "Sale transaction result: ${paymentResult.status}")

                callback(paymentResult)
            } catch (e: Exception) {
                Log.e(TAG, "Sale transaction failed: ${e.message}")
                callback(PaymentResult.error("Transaction failed: ${e.message}"))
            }
        }
    }

    /**
     * Perform QR Sale Transaction
     * @param amount Amount in cents (e.g., RM 10.00 = 1000)
     */
    fun performQrSale(
        terminalIp: String,
        terminalPort: Int,
        amount: Long,
        callback: (PaymentResult) -> Unit
    ) {
        if (!validateSdk()) {
            callback(PaymentResult.error("SDK not initialized"))
            return
        }

        if (amount <= 0) {
            callback(PaymentResult.error("Invalid amount"))
            return
        }

        AsyncTask.execute {
            try {
                Log.d(TAG, "Initiating QR sale transaction: $amount cents")

                val result = EcrSdkHelper.performQrSaleTransaction(terminalIp, terminalPort, amount)

                // Parse the response
                val paymentResult = parseEcrResult(result)
                Log.d(TAG, "QR sale transaction result: ${paymentResult.status}")

                callback(paymentResult)
            } catch (e: Exception) {
                Log.e(TAG, "QR sale transaction failed: ${e.message}")
                callback(PaymentResult.error("QR Transaction failed: ${e.message}"))
            }
        }
    }

    /**
     * Cancel current active transaction
     */
    fun cancelTransaction(
        terminalIp: String,
        terminalPort: Int,
        callback: (PaymentResult) -> Unit
    ) {
        if (!validateSdk()) {
            callback(PaymentResult.error("SDK not initialized"))
            return
        }

        AsyncTask.execute {
            try {
                Log.d(TAG, "Cancelling active transaction")

                val result = EcrSdkHelper.cancelTransaction(terminalIp, terminalPort)

                // Parse the response
                val paymentResult = parseEcrResult(result)
                Log.d(TAG, "Cancel transaction result: ${paymentResult.status}")

                callback(paymentResult)
            } catch (e: Exception) {
                Log.e(TAG, "Cancel transaction failed: ${e.message}")
                callback(PaymentResult.error("Cancel failed: ${e.message}"))
            }
        }
    }

    /**
     * Perform Void Transaction
     * @param invoice Invoice number to void
     */
    fun performVoid(
        terminalIp: String,
        terminalPort: Int,
        invoice: Int,
        callback: (PaymentResult) -> Unit
    ) {
        if (!validateSdk()) {
            callback(PaymentResult.error("SDK not initialized"))
            return
        }

        AsyncTask.execute {
            try {
                Log.d(TAG, "Initiating void transaction for invoice: $invoice")

                val result = EcrSdkHelper.performVoidTransaction(terminalIp, terminalPort, invoice)

                // Parse the response
                val paymentResult = parseEcrResult(result)
                Log.d(TAG, "Void transaction result: ${paymentResult.status}")

                callback(paymentResult)
            } catch (e: Exception) {
                Log.e(TAG, "Void transaction failed: ${e.message}")
                callback(PaymentResult.error("Void failed: ${e.message}"))
            }
        }
    }

    /**
     * Perform Settlement
     */
    fun performSettlement(
        terminalIp: String,
        terminalPort: Int,
        callback: (PaymentResult) -> Unit
    ) {
        if (!validateSdk()) {
            callback(PaymentResult.error("SDK not initialized"))
            return
        }

        AsyncTask.execute {
            try {
                Log.d(TAG, "Initiating settlement")

                val result = EcrSdkHelper.performSettlement(terminalIp, terminalPort)

                // Parse the response
                val paymentResult = parseEcrResult(result)
                Log.d(TAG, "Settlement result: ${paymentResult.status}")

                callback(paymentResult)
            } catch (e: Exception) {
                Log.e(TAG, "Settlement failed: ${e.message}")
                callback(PaymentResult.error("Settlement failed: ${e.message}"))
            }
        }
    }

    /**
     * Configure terminal screen to Customer Facing mode
     */
    fun setCustomerFacingMode(
        terminalIp: String,
        terminalPort: Int,
        callback: (PaymentResult) -> Unit
    ) {
        if (!validateSdk()) {
            callback(PaymentResult.error("SDK not initialized"))
            return
        }

        AsyncTask.execute {
            try {
                Log.d(TAG, "Setting terminal to Customer Facing mode")

                val result = EcrSdkHelper.configTerminalScreenToCustomerFacing(terminalIp, terminalPort)

                // Parse the response
                val paymentResult = parseEcrResult(result)
                Log.d(TAG, "Customer facing mode result: ${paymentResult.status}")

                callback(paymentResult)
            } catch (e: Exception) {
                Log.e(TAG, "Set customer facing mode failed: ${e.message}")
                callback(PaymentResult.error("Configuration failed: ${e.message}"))
            }
        }
    }

    /**
     * Configure terminal screen to Merchant Facing mode
     */
    fun setMerchantFacingMode(
        terminalIp: String,
        terminalPort: Int,
        callback: (PaymentResult) -> Unit
    ) {
        if (!validateSdk()) {
            callback(PaymentResult.error("SDK not initialized"))
            return
        }

        AsyncTask.execute {
            try {
                Log.d(TAG, "Setting terminal to Merchant Facing mode")

                val result = EcrSdkHelper.configTerminalScreenToMerchantFacing(terminalIp, terminalPort)

                // Parse the response
                val paymentResult = parseEcrResult(result)
                Log.d(TAG, "Merchant facing mode result: ${paymentResult.status}")

                callback(paymentResult)
            } catch (e: Exception) {
                Log.e(TAG, "Set merchant facing mode failed: ${e.message}")
                callback(PaymentResult.error("Configuration failed: ${e.message}"))
            }
        }
    }

    /**
     * Parse ECR result into PaymentResult
     */
    private fun parseEcrResult(ecrResult: EcrClient.EcrResult): PaymentResult {
        return try {
            val response = ecrResult.response
            val transactionId = ecrResult.transactionId

            if (response.isNullOrEmpty()) {
                return PaymentResult.error("Empty response from terminal")
            }

            // Parse JSON response
            val jsonResponse = JSONObject(response)

            // Check if it's a transaction query response
            if (jsonResponse.has("txnqueryresponse")) {
                val txnResponse = jsonResponse.getJSONObject("txnqueryresponse")
                return parseTransactionResponse(txnResponse, transactionId)
            }

            // Check if it's a direct transaction response
            if (jsonResponse.has("result")) {
                return parseSimpleResponse(jsonResponse, transactionId)
            }

            PaymentResult.error("Unknown response format")

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ECR result: ${e.message}")
            PaymentResult.error("Failed to parse response: ${e.message}")
        }
    }

    private fun parseTransactionResponse(
        txnResponse: JSONObject,
        transactionId: String?
    ): PaymentResult {
        val result = txnResponse.optString("result", "FAIL")
        val description = txnResponse.optString("description", "")

        return when (result.uppercase()) {
            "OK", "COMPLETE" -> {
                val amount = txnResponse.optString("amt", "")
                val approvalCode = txnResponse.optString("approval", "")
                val rrn = txnResponse.optString("rrn", "")
                val issuer = txnResponse.optString("issuer", "")

                PaymentResult.success(
                    transactionId = transactionId ?: "",
                    amount = amount,
                    approvalCode = approvalCode,
                    rrn = rrn,
                    issuer = issuer,
                    description = description
                )
            }
            "PROCESSING" -> {
                PaymentResult.processing(
                    transactionId = transactionId ?: "",
                    statusDescription = description
                )
            }
            "DECLINE", "HOST_DECLINE", "CARD_DECLINE", "FAIL" -> {
                PaymentResult.declined(
                    transactionId = transactionId ?: "",
                    reason = description
                )
            }
            "CANCEL" -> {
                PaymentResult.cancelled(
                    transactionId = transactionId ?: "",
                    reason = description
                )
            }
            else -> {
                PaymentResult.error("Unknown result: $result - $description")
            }
        }
    }

    private fun parseSimpleResponse(
        response: JSONObject,
        transactionId: String?
    ): PaymentResult {
        val result = response.optString("result", "FAIL")
        val description = response.optString("description", "")

        return when (result.uppercase()) {
            "SUCCESS" -> {
                PaymentResult.success(
                    transactionId = transactionId ?: "",
                    amount = "",
                    approvalCode = "",
                    rrn = "",
                    issuer = "",
                    description = description
                )
            }
            else -> {
                PaymentResult.error("Operation failed: $description")
            }
        }
    }

    /**
     * Validate SDK initialization
     */
    private fun validateSdk(): Boolean {
        if (!isSdkInitialized || !EcrSdkHelper.isInitialized()) {
            Log.e(TAG, "SDK not initialized. Call initializeSdk() first.")
            return false
        }
        return true
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            EcrSdkHelper.deInitializeSdk()
            isSdkInitialized = false
            Log.d(TAG, "SDK deinitialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error deinitializing SDK: ${e.message}")
        }
    }
}

/**
 * Payment Result Data Class
 */
data class PaymentResult(
    val status: Status,
    val transactionId: String,
    val amount: String,
    val approvalCode: String,
    val rrn: String,
    val issuer: String,
    val description: String,
    val errorMessage: String?
) {
    enum class Status {
        SUCCESS,
        PROCESSING,
        DECLINED,
        CANCELLED,
        ERROR
    }

    companion object {
        fun success(
            transactionId: String,
            amount: String,
            approvalCode: String,
            rrn: String,
            issuer: String,
            description: String
        ): PaymentResult {
            return PaymentResult(
                status = Status.SUCCESS,
                transactionId = transactionId,
                amount = amount,
                approvalCode = approvalCode,
                rrn = rrn,
                issuer = issuer,
                description = description,
                errorMessage = null
            )
        }

        fun processing(transactionId: String, statusDescription: String): PaymentResult {
            return PaymentResult(
                status = Status.PROCESSING,
                transactionId = transactionId,
                amount = "",
                approvalCode = "",
                rrn = "",
                issuer = "",
                description = statusDescription,
                errorMessage = null
            )
        }

        fun declined(transactionId: String, reason: String): PaymentResult {
            return PaymentResult(
                status = Status.DECLINED,
                transactionId = transactionId,
                amount = "",
                approvalCode = "",
                rrn = "",
                issuer = "",
                description = reason,
                errorMessage = null
            )
        }

        fun cancelled(transactionId: String, reason: String): PaymentResult {
            return PaymentResult(
                status = Status.CANCELLED,
                transactionId = transactionId,
                amount = "",
                approvalCode = "",
                rrn = "",
                issuer = "",
                description = reason,
                errorMessage = null
            )
        }

        fun error(message: String): PaymentResult {
            return PaymentResult(
                status = Status.ERROR,
                transactionId = "",
                amount = "",
                approvalCode = "",
                rrn = "",
                issuer = "",
                description = "",
                errorMessage = message
            )
        }
    }

    val isSuccess: Boolean get() = status == Status.SUCCESS
    val isProcessing: Boolean get() = status == Status.PROCESSING
    val isDeclined: Boolean get() = status == Status.DECLINED
    val isCancelled: Boolean get() = status == Status.CANCELLED
    val isError: Boolean get() = status == Status.ERROR
}