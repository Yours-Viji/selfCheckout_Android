package com.ezycart.services.usb.com

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.util.Log
import com.ezycart.presentation.home.HomeViewModel
import com.ezycart.services.weightScaleService.CustomSerialProber
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import kotlin.collections.indices
import kotlin.text.contains
import kotlin.text.lowercase
import kotlin.text.toByteArray


@SuppressLint("StaticFieldLeak")
object LoginWeightScaleSerialPort {
    private var mUsbManager: UsbManager? = null

    var mSerialIoManager: SerialInputOutputManager? = null
    //var ledSerialIoManager: SerialInputOutputManager? = null
    private val TAG: String = "--Shopping--"
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    var serialPort: UsbSerialPort? = null
    //private var ledSerialPort: UsbSerialPort? = null
    //private var scannerSerialPort: UsbSerialPort? = null
    private var weightScaleConnectionCount = 0

    private suspend fun doUsbManagerConnectionTask(): UsbSerialPort? {
        return GlobalScope.async(Dispatchers.IO) {
            try {
                val usbDefaultProber = UsbSerialProber.getDefaultProber()
                val usbCustomProber: UsbSerialProber = CustomSerialProber.getCustomSerialProber()!!

                for (device in mUsbManager!!.deviceList.values) {
                    var driver = usbDefaultProber.probeDevice(device)
                    if (driver == null) {
                        driver = usbCustomProber.probeDevice(device)
                    }
                    if (driver != null) {
                        for (port in driver.ports.indices) {
                            if (driver.device.productName!!.toString()
                                    .lowercase(Locale.getDefault())
                                    .contains("sparkfun") || driver.device.productName!!.toString()
                                    .lowercase(Locale.getDefault())
                                    .contains("arduino")
                            ) {
                                //ledSerialPort = driver.ports[port]
                            } else if (driver.device.productName!!.toString()
                                    .lowercase(Locale.getDefault())
                                    .contains("newland")
                            ) {
                               // scannerSerialPort = driver.ports[port]
                            } else {
                                serialPort = driver.ports[port]
                            }

                        }

                    }
                }
            } catch (e: Exception) {
                Log.i("-->>Port Null", "null")
            }
            if (serialPort != null)
                return@async serialPort
            else return@async null
        }.await()!!

    }

    private suspend fun doWeightScaleConnectionTask(
        context: Context,
        listener: SerialInputOutputManager.Listener
    ) {
        return GlobalScope.async(Dispatchers.IO) {
            initSerialPort(context, listener)
        }.await()
    }

    fun initWeightScaleSerialPort(context: Context, listener: SerialInputOutputManager.Listener) {
        try {
            mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            // refreshDeviceList()
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) { doUsbManagerConnectionTask() }
                } catch (e: Exception) {
                    // initHardwareIssue(context)
                }
                doWeightScaleConnectionTask(context, listener)
                //initLEDSerialPort(context, listener)
              //  initScannerPermission(context)
            }
        } catch (e: Exception) {
            Log.i("Weight-->>", "Init Error" + e.message)
        }

    }

   /* private fun initScannerPermission(context: Context) {
        try {
            if (scannerSerialPort != null) {
                val device = scannerSerialPort?.driver?.device
                if (!mUsbManager!!.hasPermission(device)) {
                    val mPermissionIntent: PendingIntent =
                        PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION),
                            PendingIntent.FLAG_IMMUTABLE)
                    val filter = IntentFilter(ACTION_USB_PERMISSION)
                    mUsbManager?.requestPermission(device, mPermissionIntent)
                }
            }
        } catch (e: Exception) {
        }
    }
*/
    fun closeAllSerialPortConnection() {
        try {
            if (serialPort != null && serialPort!!.isOpen) {
                stopIoManager()
                //serialPort!!.close()
            }
           /* if (ledSerialPort != null && ledSerialPort!!.isOpen) {
                stopLEDIoManager()
                // ledSerialPort!!.close()
            }*/
        } catch (e: Exception) {
        }
    }

    private fun initSerialPort(context: Context, listener: SerialInputOutputManager.Listener) {
        try {
            if (serialPort == null) {
                //initWeightScaleSerialPort(context, listener)
            } else {
                try {
                    Log.i("1", "Step")
                    val connection = mUsbManager?.openDevice(serialPort?.driver?.device)
                    Log.i("2", "Step")
                    val device = serialPort?.driver?.device
                    Log.i("3", "Step")
                    if (connection == null) {
                        /*if (!mUsbManager!!.hasPermission(device)) {
                        val mPermissionIntent: PendingIntent =
                            PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
                        val filter = IntentFilter(ACTION_USB_PERMISSION)
                        mUsbManager?.requestPermission(device, mPermissionIntent)
                    }*/
                        return
                    }
                    Log.i("4", "Step")
                    try {
                        Log.i("5", "Step")
                        if (serialPort!!.isOpen) {
                            //  resetWeightScale("1")
                        } else {
                            try {
                                Log.i("6", "Step")
                                serialPort?.open(connection)
                            } catch (e: Exception) {
                            }
                        }
                        Log.i("7", "Step")

                        try {
                            serialPort?.setParameters(
                                500000,
                                8,
                                UsbSerialPort.STOPBITS_1,
                                UsbSerialPort.PARITY_NONE
                            )
                            Log.i("8", "Step")
                            serialPort?.dtr = true
                            serialPort?.rts = true
                        } catch (e: Exception) {
                        }
                        if (!serialPort!!.isOpen) {
                            Log.i("Weightscale-->>1", "initHardwareIssue")
                            //initHardwareIssue(context)
                            if (weightScaleConnectionCount == 3) {
                                // initHardwareIssue(context)
                                startIoManager(listener)
                            } else {
                                initWeightScaleSerialPort(context, listener)

                            }
                            weightScaleConnectionCount++
                        }
                        Log.i("9", "Step")
                    } catch (e: IOException) {
                        Log.i("Weightscale-->>2", "initHardwareIssue")
                        Log.e(TAG, "Error setting up Weightscale device: " + e.message, e)

                        return
                    }
                    startIoManager(listener)
                } catch (e: Exception) {
                    Log.i("Weightscale-->>3", "initHardwareIssue")
                    Log.e(TAG, "Error init Weightscale SerialPort --2" + e.message)
                }
            }
        } catch (e: Exception) {
        }
    }

    /*private fun initLEDSerialPort(context: Context, listener: SerialInputOutputManager.Listener) {
        if (ledSerialPort == null) {
            //initWeightScaleSerialPort(context, listener)
        } else {
            try {
                val connection = mUsbManager?.openDevice(ledSerialPort?.driver?.device)

                val device = ledSerialPort?.driver?.device

                if (connection == null) {
                    *//*if (!mUsbManager!!.hasPermission(device)) {
                        val mPermissionIntent: PendingIntent =
                            PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
                        val filter = IntentFilter(ACTION_USB_PERMISSION)
                        mUsbManager?.requestPermission(device, mPermissionIntent)
                    }*//*
                    return
                }

                try {
                    if (ledSerialPort!!.isOpen) {
                        //  resetWeightScale("1")
                    } else {
                        try {
                            ledSerialPort?.open(connection)
                        } catch (e: Exception) {
                        }
                    }

                    ledSerialPort?.setParameters(
                        500000,
                        8,
                        UsbSerialPort.STOPBITS_1,
                        UsbSerialPort.PARITY_NONE
                    )

                    ledSerialPort?.dtr = true
                    ledSerialPort?.rts = true
                    if (!ledSerialPort!!.isOpen) {
                        Log.i("LED-->>1", "initHardwareIssue")
                        // initHardwareIssue(context)
                        startLedIoManager(listener)
                    }
                } catch (e: IOException) {
                    Log.i("LED-->>2", "initHardwareIssue")
                    Log.e(TAG, "Error setting up LED device: " + e.message, e)

                    return
                }
                startLedIoManager(listener)
            } catch (e: Exception) {
                Log.i("LED-->>3", "initHardwareIssue")
                Log.e(TAG, "Error init LED SerialPort --2" + e.message)
            }
        }
    }*/

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
                // stopIoManager()
                mSerialIoManager = SerialInputOutputManager(serialPort, listener)

                try {
                    mSerialIoManager!!.readBufferSize = serialPort!!.readEndpoint.maxPacketSize
                } catch (e: Exception) {
                }
                mSerialIoManager!!.readTimeout = 1000
                //mSerialIoManager.
                mSerialIoManager!!.start()
                // sendMessageToWeightScale("1\r\n")
                Log.e(TAG, "Weight Scale IoManager-->> Started")
            } catch (e: Exception) {
                Log.e(TAG, "Weight Scale startIoManager-->>" + e.message)
            }
        }
    }

    /*fun startLedIoManager(listener: SerialInputOutputManager.Listener) {
        if (ledSerialPort != null) {
            try {
                //  stopLEDIoManager()
                ledSerialIoManager = SerialInputOutputManager(ledSerialPort, null)

                try {
                    ledSerialIoManager!!.readBufferSize = ledSerialPort!!.readEndpoint.maxPacketSize
                } catch (e: Exception) {
                }
                ledSerialIoManager!!.readTimeout = 1000
                ledSerialIoManager!!.start()
                Log.e(TAG, "LED IoManager-->> Started")
            } catch (e: Exception) {
                Log.e(TAG, "startLEDIoManager-->>" + e.message)
            }
        }
    }*/

   /* fun stopLEDIoManager() {
        try {
            if (ledSerialIoManager != null) {
                ledSerialIoManager!!.stop()
                ledSerialIoManager = null
            }
        } catch (e: Exception) {
            //showToast("stopIoManager" + e.message)
        }
    }*/

    fun sendMessageToWeightScale(message: String) {
        try {
            mSerialIoManager!!.writeAsync(message.toByteArray())
        } catch (e: Exception) {
        }

    }

   /* fun sendMessageToLED(message: String) {
        try {
            ledSerialIoManager!!.writeAsync(message.toByteArray())
        } catch (e: Exception) {
        }

    }*/
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

    // 2. The Connection Method (The "Dumbbell" Logic)
    fun connectScale(context: Context, listener: SerialInputOutputManager.Listener) {
        // Disconnect existing if any to avoid port busy errors
        stopIoManager()

        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber = CustomSerialProber.getCustomSerialProber()!!

        var driver: UsbSerialDriver? = null
        for (device in mUsbManager!!.deviceList.values) {
            driver = usbDefaultProber.probeDevice(device) ?: usbCustomProber.probeDevice(device)
            if (driver != null) break
        }

        if (driver == null) return

        if (!mUsbManager!!.hasPermission(driver.device)) {
            val flags = PendingIntent.FLAG_MUTABLE
            val intent = Intent("com.android.example.USB_PERMISSION").setPackage(context.packageName)
            val permissionIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
            mUsbManager?.requestPermission(driver.device, permissionIntent)
            return
        }
        if (driver != null) {
            for (port in driver.ports.indices) {
                if (driver.device.productName!!.toString()
                        .lowercase(Locale.getDefault())
                        .contains("sparkfun") || driver.device.productName!!.toString()
                        .lowercase(Locale.getDefault())
                        .contains("arduino")
                ) {
                    //ledSerialPort = driver.ports[port]
                } else if (driver.device.productName!!.toString()
                        .lowercase(Locale.getDefault())
                        .contains("newland")
                ) {
                    // scannerSerialPort = driver.ports[port]
                } else {

                    try {
                        val connection = mUsbManager?.openDevice(driver.device)
                        serialPort = driver.ports[port]
                        serialPort!!.open(connection)

                        serialPort!!.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                        serialPort!!.dtr = true
                        serialPort!!.rts = true
                        mSerialIoManager = SerialInputOutputManager(serialPort, listener)
                        mSerialIoManager?.start()
                        mSerialIoManager = SerialInputOutputManager(serialPort, listener)
                        mSerialIoManager?.start()
                    } catch (e: Exception) {
                    }
                }

            }

        }

       /* try {
            val connection = mUsbManager?.openDevice(driver.device)
            val port = driver.ports[0]
            port.open(connection)
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            port.dtr = true
            port.rts = true

            serialPort = port
            mSerialIoManager = SerialInputOutputManager(serialPort, listener)
            mSerialIoManager?.start()
        } catch (e: Exception) {
            Log.e("USB", "Connect Failed: ${e.message}")
        }*/
    }

   /* fun stopIoManager() {
        mSerialIoManager?.stop()
        mSerialIoManager = null
        serialPort?.close()
        serialPort = null
    }*/
}



