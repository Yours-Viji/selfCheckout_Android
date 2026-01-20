package com.ezycart.services.usb.com

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.ezycart.services.weightScaleService.CustomSerialProber
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Based on CustomProber.java and SerialService.java logic
object UsbSerialManager {
    private const val TAG = "UsbSerialManager"
    private const val ACTION_USB_PERMISSION = "com.ezycart.USB_PERMISSION"

    private var serialPort: UsbSerialPort? = null
    private var usbConnection: UsbDeviceConnection? = null
    private var ioManager: SerialInputOutputManager? = null

    // Use SharedFlow to broadcast data to the UI/Toast
    private val _serialData = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val serialData = _serialData.asSharedFlow()

    fun connect(context: Context, device: UsbDevice) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        // 1. Find the driver using the CustomProber logic from the GitHub repo
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
            ?: CustomSerialProber.getCustomSerialProber()!!.probeDevice(device)
            ?: return

        val port = driver.ports[0] // Most devices use port 0

        try {
            usbConnection = manager.openDevice(driver.device)
            serialPort = port
            serialPort?.open(usbConnection)

            // Set parameters same as the terminal app defaults
            serialPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            // Start the I/O Manager (handles the background thread reading)
            ioManager = SerialInputOutputManager(serialPort, object : SerialInputOutputManager.Listener {
                override fun onNewData(data: ByteArray) {
                    val message = String(data)
                    _serialData.tryEmit(message)
                }

                override fun onRunError(e: Exception) {
                    Log.e(TAG, "Serial Error: ${e.message}")
                    disconnect()
                }
            })
            ioManager?.start()

        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
        }
    }

    fun disconnect() {
        ioManager?.stop()
        ioManager = null
        serialPort?.close()
        serialPort = null
        usbConnection?.close()
        usbConnection = null
    }
}