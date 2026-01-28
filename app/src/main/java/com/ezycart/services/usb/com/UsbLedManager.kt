package com.ezycart.services.usb.com

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.util.Log

class UsbLedManager private constructor(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var connection: UsbDeviceConnection? = null
    private var endpointOut: UsbEndpoint? = null
    private val ACTION_USB_PERMISSION = "com.example.USB_PERMISSION"

    companion object {
        @Volatile
        private var INSTANCE: UsbLedManager? = null

        fun getInstance(context: Context): UsbLedManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UsbLedManager(context).also { INSTANCE = it }
            }
        }
    }

    // 1. Connection & Permission Method
    fun connectAndPrepare() {
        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) {
            Log.e("USB_LED", "No USB devices found")
            return
        }

        // Find your specific board (usually the first one if only one is plugged in)
        val device = deviceList.values.first()

        if (usbManager.hasPermission(device)) {
            setupDevice(device)
        } else {
            val permissionIntent = PendingIntent.getBroadcast(
                context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    private fun setupDevice(device: UsbDevice) {
        val usbInterface = device.getInterface(0)
        for (i in 0 until usbInterface.endpointCount) {
            val ep = usbInterface.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                ep.direction == UsbConstants.USB_DIR_OUT) {
                endpointOut = ep
            }
        }

        connection = usbManager.openDevice(device)
        connection?.claimInterface(usbInterface, true)
        Log.d("USB_LED", "Device Ready")
    }

    // 2. The Sending Method (The one you will trigger)
    fun sendLedCommand(ledNumbers: List<Int>) {
        try {
            if (connection == null || endpointOut == null) {
                Log.e("USB_LED", "Not connected. Attempting reconnect...")
                connectAndPrepare()
                return
            }

            var bitmask = 0
            for (led in ledNumbers) {
                if (led in 1..6) {
                    bitmask = bitmask or (1 shl (led - 1))
                }
            }

            val hexValue = String.format("%02X", bitmask)
            val command = "@O$hexValue*" // Adding \r\n for board stability

            Thread {
                val buffer = command.toByteArray()
                val result = connection?.bulkTransfer(endpointOut, buffer, buffer.size, 1000)
                Log.d("USB_LED", "Sent: $command | Result: $result")
            }.start()
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
    }

    fun turnOffAll() = sendLedCommand(emptyList())
}