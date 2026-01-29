package com.ezycart.services.usb.com

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import android.util.Log
import com.ezycart.presentation.home.HomeViewModel
import com.ezycart.services.weightScaleService.CustomSerialProber
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlin.text.contains
import kotlin.text.toByteArray
@SuppressLint("StaticFieldLeak")
object LedSerialConnection {
    private var usbConnection: UsbDeviceConnection? = null
    private var usbEndpointOut: UsbEndpoint? = null
    private var usbEndpointIn: UsbEndpoint? = null // Added IN endpoint

    @Volatile private var isSendingUsbCommand = false
    @Volatile private var pendingUsbCommand: String? = null
    private var currentOutputBitmask = 0

    fun connect(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = usbManager.deviceList.values.find {
            it.vendorId == 1240 && it.productId == 58
        } ?: return

        val usbInterface = device.getInterface(0)
        // Find BOTH Out and In endpoints
        for (i in 0 until usbInterface.endpointCount) {
            val ep = usbInterface.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_OUT) usbEndpointOut = ep
                else if (ep.direction == UsbConstants.USB_DIR_IN) usbEndpointIn = ep
            }
        }

        usbConnection = usbManager.openDevice(device)
        usbConnection?.claimInterface(usbInterface, true)
    }

    fun updateOutput(index: Int, turnOn: Boolean) {
        if (turnOn) currentOutputBitmask = currentOutputBitmask or (1 shl index)
        else currentOutputBitmask = currentOutputBitmask and (1 shl index).inv()
        processOutput()
    }

    fun setAll(on: Boolean) {
        currentOutputBitmask = if (on) 0x3F else 0x00
        processOutput()
    }

    private fun processOutput() {
        val hexValue = String.format("%02X", currentOutputBitmask)
        sendCustomCommand("@I$hexValue*")
    }

    private fun sendCustomCommand(command: String) {
        val conn = usbConnection ?: return
        val epOut = usbEndpointOut ?: return

        synchronized(this) {
            if (isSendingUsbCommand) {
                pendingUsbCommand = command
                return
            }
            isSendingUsbCommand = true
        }

        Thread {
            var toSend: String? = command
            while (toSend != null) {
                try {
                    val buffer = toSend!!.toByteArray(Charsets.US_ASCII)
                    // 1. Send the command
                    conn.bulkTransfer(epOut, buffer, buffer.size, 1000)

                    // 2. CRITICAL: Read the response (Draining the buffer)
                    // The board sends a reply. If we don't read it, the next "Write" will fail.
                    val readBuffer = ByteArray(64)
                    usbEndpointIn?.let { epIn ->
                        conn.bulkTransfer(epIn, readBuffer, readBuffer.size, 500)
                    }

                } catch (e: Exception) {
                    Log.e("USB", "Transfer error: ${e.message}")
                }

                synchronized(this) {
                    toSend = pendingUsbCommand
                    pendingUsbCommand = null
                    isSendingUsbCommand = toSend != null
                }
            }
        }.start()
    }
}