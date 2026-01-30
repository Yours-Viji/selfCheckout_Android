package com.ezycart.services.usb

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
object LoginWeightScaleSerialPort {
    private var mUsbManager: UsbManager? = null

    var mSerialIoManager: SerialInputOutputManager? = null
    private val TAG: String = "--Shopping--"
    var serialPort: UsbSerialPort? = null

     fun stopIoManager() {
        try {
            if (mSerialIoManager != null) {
                mSerialIoManager!!.stop()
                mSerialIoManager = null
            }
        } catch (e: Exception) {
            //showToast("stopIoManager" + e.message)
        }
    }

    private fun startIoManager(listener: SerialInputOutputManager.Listener) {
        if (serialPort != null) {
            try {
                // 1. ALWAYS stop and nullify the old manager first
                mSerialIoManager?.stop()
                mSerialIoManager = null

                // 2. Initialize the new manager
                mSerialIoManager = SerialInputOutputManager(serialPort, listener)

                // 3. Buffer and Timeout adjustments
                // Instead of maxPacketSize, use a standard 16KB buffer for stability
                mSerialIoManager!!.readBufferSize = 16384

                // A shorter timeout (e.g., 100ms) is better for high-speed microcontrollers like Pico
                mSerialIoManager!!.readTimeout = 100

                // 4. Set Thread Priority
                // This ensures the background listener doesn't get killed by the UI thread
                mSerialIoManager!!.setThreadPriority(Thread.MAX_PRIORITY)

                mSerialIoManager!!.start()

                Log.d(TAG, "Weight Scale IoManager started on Port: ${serialPort!!.portNumber}")
            } catch (e: Exception) {
                Log.e(TAG, "Weight Scale startIoManager Error: ${e.message}")
            }
        }
    }


    fun sendMessageToWeightScale(message: String) {
        try {
            mSerialIoManager!!.writeAsync(message.toByteArray())
        } catch (e: Exception) {
        }

    }

// Common Buffer to handle data chunks
   private val weightBuffer = StringBuilder()

    // 1. Common Method: The actual parsing logic used everywhere
    fun createCommonListener(viewModel: HomeViewModel): SerialInputOutputManager.Listener {
        return object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray) {
                val chunk = String(data)
                weightBuffer.append(chunk)

                while (weightBuffer.contains("\n")) {
                    val indexOfNewline = weightBuffer.indexOf("\n")
                    val fullMessage = weightBuffer.substring(0, indexOfNewline).trim()

                    if (fullMessage.isNotEmpty()) {
                        // Optional: Keep Toast for debugging
                        // DynamicToast.make(context, fullMessage).show()
                        try {
                            viewModel.handleRawUsbData(fullMessage)
                        } catch (e: Exception) {
                            Log.e("USB", "Parse error: ${e.message}")
                        }
                    }
                    weightBuffer.delete(0, indexOfNewline + 1)
                }
            }

            override fun onRunError(e: Exception) {
                Log.e("USB_ERROR", "Serial error: ${e.message}")
                viewModel.setErrorMessage("Serial error: ${e.message}")
            }
        }
    }
    fun connectPicoScaleDirectly(context: Context, listener: SerialInputOutputManager.Listener) {
        // 1. Clean up any existing connection
        stopIoManager()

        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        // Hardware identifiers for your Raspberry Pi Pico
        val PICO_VID = 11914 // 0x2E8A in decimal
        val PICO_PID = 10    // 0x000A in decimal

        // 2. Find the Pico specifically in the device list
        val device = mUsbManager?.deviceList?.values?.find {
            it.vendorId == PICO_VID && it.productId == PICO_PID
        }

        if (device == null) {
            Log.e("USB_SCALE", "Raspberry Pi Pico not found on USB Hub!")
            return
        }

        // 3. Probing the device
        // Since it's a Pico, the default prober usually identifies it as CDC/ACM
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
            ?: CustomSerialProber.getCustomSerialProber()!!.probeDevice(device)

        if (driver == null) {
            Log.e("USB_SCALE", "No serial driver found for Pico")
            return
        }

        // 4. Request Permission if not already granted
        if (!mUsbManager!!.hasPermission(device)) {
            Log.d("USB_SCALE", "Requesting permission for Pico...")
            val flags = PendingIntent.FLAG_MUTABLE
            val intent = Intent("com.android.example.USB_PERMISSION").setPackage(context.packageName)
            val permissionIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
            mUsbManager?.requestPermission(device, permissionIntent)
            return
        }

        // 5. Open the specific Port
        try {
            val connection = mUsbManager?.openDevice(device)

            // Raspberry Pi Pico usually uses the first available port (index 0)
            serialPort = driver.ports[0]
            serialPort!!.open(connection)

            // Standard Pico/LoadCell settings: 115200 Baud, 8 Data bits, 1 Stop bit, No Parity
            serialPort!!.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            // DTR and RTS are often required for Pico to start sending serial data
            serialPort!!.dtr = true
            serialPort!!.rts = true

            // 6. Start the IO Manager to listen for weight data
            startIoManager(listener)
            Log.d("USB_SCALE", "Connected directly to Raspberry Pi Pico (Load Cell)")

        } catch (e: Exception) {
            Log.e("USB_SCALE", "Failed to open Pico connection: ${e.message}")
        }
    }

}


