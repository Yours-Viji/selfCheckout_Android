package com.ezycart.services.usb.com

import android.content.Context
import com.ezycart.presentation.home.HomeViewModel
import com.hoho.android.usbserial.util.SerialInputOutputManager

object WeightScaleManager {
    private var isInitialized = false
    private var commonListener: SerialInputOutputManager.Listener? = null
    private var commonLedListener: SerialInputOutputManager.Listener? = null

    fun init(viewModel: HomeViewModel) {
        try {
            if (commonListener == null) {
                commonListener = LoginWeightScaleSerialPort.createCommonListener(viewModel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }



    fun connect(context: Context) {
        try {
            connectLed(context)
            commonListener?.let {listener ->
                LoginWeightScaleSerialPort.connectPicoScaleDirectly(
                    context,
                    listener
                )
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun connectLed(context: Context){
        try {
            LedSerialConnection.connect(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initOnce(viewModel: HomeViewModel) {
        if (!isInitialized) {
            init(viewModel)
            isInitialized = true
        }
    }

    fun connectSafe(context: Context) {
        if (!isConnected()) {
            connect(context)
        }
    }

    private fun isConnected(): Boolean {
        // implement a simple flag or check serial port state
        return false
    }
}
