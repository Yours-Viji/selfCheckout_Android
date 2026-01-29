package com.ezycart.services.usb.com

import android.content.Context
import com.ezycart.presentation.home.HomeViewModel
import com.hoho.android.usbserial.util.SerialInputOutputManager

object WeightScaleManager {
    private var isInitialized = false
    private var commonListener: SerialInputOutputManager.Listener? = null

    fun init(viewModel: HomeViewModel) {
        if (commonListener == null) {
            commonListener = LoginWeightScaleSerialPort.createCommonListener(viewModel)
        }
    }

    fun connect(context: Context) {
        try {
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
