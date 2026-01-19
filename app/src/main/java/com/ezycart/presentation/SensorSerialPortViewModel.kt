package com.ezycart.presentation

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.services.usb.SensorSerialPortCommunication
import com.ezycart.services.usb.UsbEvent
import com.ezycart.services.usb.UsbListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class SensorSerialPortViewModel @Inject constructor(
    private val usbListener: UsbListener,
    private val preferencesManager: PreferencesManager,
    private val application: Application
) : ViewModel() {

    private val _weightState = MutableStateFlow(WeightUpdate())
    val weightState = _weightState.asStateFlow()

    private val _usbData = MutableStateFlow("Load Cell Data")
    val usbData = _usbData.asStateFlow()
    val connectionLog: StateFlow<String> = SensorSerialPortCommunication.sensorMessage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Waiting..."
        )
    init {
        observeUsbHardware()
        viewModelScope.launch {

            /* SensorSerialPortCommunication.weightDataFlow
                .map { data ->
                    val jsonString = String(data)
                    // Update raw data for logging purposes
                    _usbData.value = jsonString

                    try {
                        val json = JSONObject(jsonString)
                        if (json.has("status")) {
                            WeightUpdate(
                                status = json.optString("status", "0").toInt(),
                                weight = json.optString("item_load", ""),
                                totalWeight = json.optString("total_load", ""),
                                message = json.toString()

                            )
                        } else {
                            WeightUpdate(message = json.toString())
                        }
                    } catch (e: Exception) {
                        Log.e("VM", "JSON Parse Error: ${e.message}")
                        WeightUpdate(message = "Parse Error") // Fallback state
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000), // Stop processing 5s after UI goes away
                    initialValue = WeightUpdate()
                )*/


           /* SensorSerialPortCommunication.weightDataFlow.collect { data ->
                val jsonString = String(data)
                _usbData.value = jsonString
                try {
                    val json = JSONObject(jsonString)
                    _weightState.value = WeightUpdate(
                        status = 0,
                        weight = json.optString("item_load", ""),
                        totalWeight = json.optString("total_load", "")
                    )
                    *//*if (json.has("status")){
                        val status =  json.optString("status", "")
                        _weightState.value = WeightUpdate(
                            status = status.toInt(),
                            weight = json.optString("item_load", ""),
                            totalWeight = json.optString("total_load", "")
                        )
                    }else{
                        _weightState.value = WeightUpdate(
                            status = 0,
                            weight = "",
                            totalWeight = "",
                            message = json.toString()
                        )
                    }*//*


                } catch (e: Exception) {
                    Log.e("VM", "JSON Parse Error")
                }
            }*/


        }
    }

    private fun observeUsbHardware() {
        Toast.makeText(application,"USB observe", Toast.LENGTH_SHORT).show()
        viewModelScope.launch {
            // Collect the Flow from the listener we optimized
            usbListener.usbEvents.collect { event ->
                when (event) {
                    is UsbEvent.Attached -> {
                        // Logic to init port when plugged in
                        Toast.makeText(application,"USB attached", Toast.LENGTH_SHORT).show()
                        connect()
                    }
                    is UsbEvent.Detached -> {
                       // disconnectManually()
                    }
                }
            }
        }
    }

    fun connect() {
        // Use applicationContext to avoid leaking Activity context
        SensorSerialPortCommunication.initAllPorts(application.applicationContext)
        Toast.makeText(application,"USB Conected", Toast.LENGTH_SHORT).show()
    }

    fun sendCommand(command: String) {
        SensorSerialPortCommunication.sendMessageToWeightScale(command)
    }
    fun disconnectManually() {
        SensorSerialPortCommunication.closeAllConnections()
    }
    override fun onCleared() {
        super.onCleared()
        // Ensure ports are closed when ViewModel is destroyed
        SensorSerialPortCommunication.closeAllConnections()
    }
}

data class WeightUpdate(
    val status: Int = 0,
    val weight: String = "",
    val totalWeight: String = "",
    val message: String = ""
)