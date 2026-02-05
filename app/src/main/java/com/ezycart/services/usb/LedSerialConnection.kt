package com.ezycart.services.usb

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.text.toByteArray
@SuppressLint("StaticFieldLeak")
object LedSerialConnection {
    private var usbConnection: UsbDeviceConnection? = null
    private var usbEndpointOut: UsbEndpoint? = null
    private var usbEndpointIn: UsbEndpoint? = null // Added IN endpoint


    // Use the same action string as your manifest
    private const val ACTION_USB_PERMISSION = "com.ezycart.USB_PERMISSION"
    @Volatile private var isSendingUsbCommand = false
    @Volatile private var pendingUsbCommand: String? = null
    private var currentOutputBitmask = 0

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pulseRunnable: Runnable? = null
    private var isPulseActive = false
    private var pulseState = false

    const val MASK_PRINTER = 0x01
    const val MASK_SCANNER = 0x02
    const val MASK_GREEN_LED = 0x04
    const val MASK_RED_LED = 0x08

   /* fun connect(context: Context) {
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
    }*/

    fun connect(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        // VID 0x04D8 = 1240, PID 0x003A = 58
        val device = usbManager.deviceList.values.find {
            it.vendorId == 1240 && it.productId == 58
        } ?: return

        // --- ADDED PERMISSION CHECK START ---
        if (!usbManager.hasPermission(device)) {
            Log.d("LED_USB", "Requesting permission for LED Board...")
            val intent = Intent(ACTION_USB_PERMISSION).setPackage(context.packageName)
            val permissionIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE // Required for Android 12+
            )
            usbManager.requestPermission(device, permissionIntent)
            return // Exit and wait for the user to click "Allow"
        }
        // --- ADDED PERMISSION CHECK END ---

        val usbInterface = device.getInterface(0)
        for (i in 0 until usbInterface.endpointCount) {
            val ep = usbInterface.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_OUT) usbEndpointOut = ep
                else if (ep.direction == UsbConstants.USB_DIR_IN) usbEndpointIn = ep
            }
        }

        usbConnection = usbManager.openDevice(device)
        usbConnection?.claimInterface(usbInterface, true)
        Log.d("LED_USB", "LED Board Connected successfully")
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

    fun setScenario(scenario: AppScenario) {
        val mask = when (scenario) {
            AppScenario.START_SHOPPING -> {
                // LEDs 2,3,4,5 -> Bits 1,2,3,4
                (1 shl 1) or (1 shl 2) or (1 shl 3) or (1 shl 4)
            }
            AppScenario.ERROR -> {
                // LEDs 2,4 -> Bits 1,3
                (1 shl 1) or (1 shl 3)
            }
            AppScenario.PAYMENT_SUCCESS -> {
                // LED 3 -> Bit 2
                (1 shl 2)
            }
            AppScenario.PRINTING -> {
                // LEDs 1,3 -> Bits 0,2
                (1 shl 0) or (1 shl 2)
            }
            AppScenario.ALL_OFF -> 0x00
        }

        currentOutputBitmask = mask
        processOutput()
    }

   private fun startPulse(targetMask: Int, interval: Long = 600L) {
        if (isPulseActive) stopPulse() // Reset if already running

        isPulseActive = true
        pulseRunnable = object : Runnable {
            override fun run() {
                if (!isPulseActive) return

                if (pulseState) {
                    // Turn TARGET LEDs ON (using OR)
                    currentOutputBitmask = currentOutputBitmask or targetMask
                } else {
                    // Turn TARGET LEDs OFF (using AND NOT)
                    currentOutputBitmask = currentOutputBitmask and targetMask.inv()
                }

                processOutput()
                pulseState = !pulseState
                mainHandler.postDelayed(this, interval)
            }
        }
        mainHandler.post(pulseRunnable!!)
    }

    fun stopPulse() {
        isPulseActive = false
        pulseRunnable?.let { mainHandler.removeCallbacks(it) }
        pulseRunnable = null

    }

    fun startAnimate(target: Int) {
        startPulse(targetMask = target, interval = 600L)
    }


}

enum class AppScenario {
    START_SHOPPING, ERROR, PAYMENT_SUCCESS, PRINTING, ALL_OFF
}