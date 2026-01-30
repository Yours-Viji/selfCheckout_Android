package com.ezycart.presentation.payment

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
) : ViewModel() {
    // State for payment amounts
    var totalAmount = mutableStateOf("0.00")
        private set

    var amountToBePaid = mutableStateOf("0.00")
        private set

    // State for tracking if a process is running
    var isProcessing = mutableStateOf(false)
        private set

    fun onPaymentMethodSelected(method: String) {
        isProcessing.value = true
        // Add your logic to trigger hardware LEDs here
        // Example: LedSerialConnection.startPulse(0x10) // Blue LED for processing
    }

    fun updateAmounts(total: String, toBePaid: String) {
        totalAmount.value = total
        amountToBePaid.value = toBePaid
    }

}