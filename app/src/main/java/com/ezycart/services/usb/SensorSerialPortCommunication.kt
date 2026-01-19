package com.ezycart.services.usb


import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.ezycart.services.weightScaleService.CustomSerialProber
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.IOException
import java.util.*

object SensorSerialPortCommunication {
    private const val TAG = "SensorSerialPortCommunication"

    // Coroutine setup
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Reactive Flows for incoming data
    private val _weightDataFlow = MutableSharedFlow<ByteArray>( replay = 1,               // Add this: allows new listeners to get the last message
        extraBufferCapacity = 64 ,// Keeps existing buffer for performance
        onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val weightDataFlow = _weightDataFlow.asSharedFlow()

    private val _ledDataFlow = MutableSharedFlow<ByteArray>( replay = 1,               // Add this: allows new listeners to get the last message
        extraBufferCapacity = 64 ,// Keeps existing buffer for performance
        onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val ledDataFlow = _ledDataFlow.asSharedFlow()

    private var mUsbManager: UsbManager? = null
    private var serialPort: UsbSerialPort? = null
    private var ledSerialPort: UsbSerialPort? = null
    private var scannerSerialPort: UsbSerialPort? = null

    private var mSerialIoManager: SerialInputOutputManager? = null
    private var ledSerialIoManager: SerialInputOutputManager? = null

    private val weightBuffer = StringBuilder()

    private val _sensorMessage = MutableSharedFlow<String>(
        replay = 1,               // Add this: allows new listeners to get the last message
        extraBufferCapacity = 64 ,// Keeps existing buffer for performance
                onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sensorMessage = _sensorMessage.asSharedFlow()
    /**
     * Initializes all ports. Call this from your Service or Activity.
     */
    fun initAllPorts(context: Context) {

        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        scope.launch {
            try {
                _sensorMessage.emit("-Init-")
                discoverDevices()

                // Initialize Scale
                serialPort?.let {
                    setupPort(it, weightListener) { manager -> mSerialIoManager = manager }
                    _sensorMessage.emit("Connected - $it.device.productName")

                }

                // Initialize LED
                ledSerialPort?.let {
                    setupPort(it, ledListener) { manager -> ledSerialIoManager = manager }
                    _sensorMessage.emit("Connected - $it.device.productName")
                }
            } catch (e: Exception) {
                _sensorMessage.emit("Error on Connection")
                Log.e(TAG, "Failed to initialize ports: ${e.message}")
            }
        }
    }

    private fun discoverDevices() {
        val manager = mUsbManager ?: return
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber = CustomSerialProber.getCustomSerialProber() ?: usbDefaultProber
if (manager.deviceList.values.isNullOrEmpty()){
    _sensorMessage.tryEmit("No USB Device found")
}
        for (device in manager.deviceList.values) {
            val driver = usbDefaultProber.probeDevice(device) ?: usbCustomProber.probeDevice(device)

            driver?.let { d ->
                for (port in d.ports) {
                    val productName = (d.device.productName ?: "").lowercase(Locale.getDefault())

                    when {
                        productName.contains("sparkfun") || productName.contains("arduino") -> {
                            ledSerialPort = port
                        }
                        productName.contains("newland") -> {
                            scannerSerialPort = port
                        }
                        else -> {
                            serialPort = port
                        }
                    }
                }
            }
        }
    }

    private fun setupPort(
        port: UsbSerialPort,
        listener: SerialInputOutputManager.Listener,
        onManagerCreated: (SerialInputOutputManager) -> Unit
    ) {
        val manager = mUsbManager ?: return
        try {
            val connection = manager.openDevice(port.driver.device) ?: return

            if (!port.isOpen) {
                port.open(connection)
            }

            port.setParameters(500000, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            port.dtr = true
            port.rts = true

            val ioManager = SerialInputOutputManager(port, listener).apply {
                readTimeout = 1000
                start()
            }

           /* val ioManager = SerialInputOutputManager(port, weightListener).apply {
                readTimeout = 1000
                start()
            }*/
            onManagerCreated(ioManager)
            Log.d(TAG, "Port ${port.portNumber} setup successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up port: ${e.message}")
        }
    }

    // --- Listeners that emit to Flows ---

    private val weightListener = object : SerialInputOutputManager.Listener {
        override fun onNewData(data: ByteArray) {
            val message = String(data)
            weightBuffer.append(message)

            // Logic: Only emit when we see the end of a line (newline character)
            if (message.contains("\n")) {
                val fullMessage = weightBuffer.toString().trim()
                if (fullMessage.isNotEmpty()) {
                    // This sends the full JSON to your ViewModel
                   // _weightDataFlow.tryEmit(fullMessage.toByteArray())
                    _sensorMessage.tryEmit(fullMessage)
                }

                // Clear the "waiting room" for the next JSON packet
                weightBuffer.setLength(0)
            }
        }

        override fun onRunError(e: Exception) {
            Log.e(TAG, "Weight Scale IO Error: ${e.message}")
        }
    }

    private val ledListener = object : SerialInputOutputManager.Listener {
        override fun onNewData(data: ByteArray) {
            _ledDataFlow.tryEmit(data)
        }
        override fun onRunError(e: Exception) {
            Log.e(TAG, "LED IO Error: ${e.message}")
        }
    }

    // --- Outbound Communication ---

    fun sendMessageToWeightScale(message: String) {
        scope.launch {
            try {
                mSerialIoManager?.writeAsync(message.toByteArray())
            } catch (e: Exception) {
                Log.e(TAG, "Write to Scale failed: ${e.message}")
            }
        }
    }

    fun sendMessageToLED(message: String) {
        scope.launch {
            try {
                ledSerialIoManager?.writeAsync(message.toByteArray())
            } catch (e: Exception) {
                Log.e(TAG, "Write to LED failed: ${e.message}")
            }
        }
    }

    // --- Cleanup ---

    fun closeAllConnections() {
        mSerialIoManager?.stop()
        ledSerialIoManager?.stop()

        try {
            _sensorMessage.tryEmit("Disconnected - ${serialPort!!.device.productName}")

            serialPort?.close()
            ledSerialPort?.close()


        } catch (e: IOException) {
            Log.e(TAG, "Error closing ports: ${e.message}")
        } finally {
            serialPort = null
            ledSerialPort = null
            mSerialIoManager = null
            ledSerialIoManager = null
        }
    }
}