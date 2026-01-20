package com.ezycart.presentation.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ezycart.presentation.SensorSerialPortViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
) : ViewModel() {

    private val _totalAmount = MutableStateFlow(33.90)
    val totalAmount = _totalAmount.asStateFlow()

    fun onPaymentMethodSelected(method: String) {
        // According to your flow, clicking Pay Now should verify weight
        // Sending CMD 80 to get Status 10 from the load cell
        //sensorViewModel.sendCommand("80")

        // Navigate to specific payment processing logic here
        Log.d("Payment", "Selected: $method. Triggering Weight Verification...")
    }
}