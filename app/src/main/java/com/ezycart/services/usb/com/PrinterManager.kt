package com.ezycart.services.usb.com

import android.content.Context
import android.hardware.usb.*
import android.util.Log

class PrinterManager(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var connection: UsbDeviceConnection? = null
    private var endpointOut: UsbEndpoint? = null

    // Standard ESC/POS Commands
    private val ESC_INIT = byteArrayOf(0x1B, 0x40)       // Initialize printer
    private val ESC_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    private val LF = byteArrayOf(0x0A)                   // Line Feed
    private val PAPER_CUT = byteArrayOf(0x1D, 0x56, 0x41, 0x00) // Full cut

    fun connectPrinter() {
        val deviceList = usbManager.deviceList
        // Filter for the printer (Check your device list for VID 0x154f or similar)
        val printerDevice = deviceList.values.firstOrNull { it.vendorId == 0x154f || it.productId == 0x0547 }
            ?: deviceList.values.firstOrNull { it.deviceName.contains("usb", true) } // Fallback

        printerDevice?.let { device ->
            val usbInterface = device.getInterface(0)
            endpointOut = (0 until usbInterface.endpointCount)
                .map { usbInterface.getEndpoint(it) }
                .find { it.direction == UsbConstants.USB_DIR_OUT }

            connection = usbManager.openDevice(device)
            connection?.claimInterface(usbInterface, true)
            Log.d("Printer", "Printer Connected!")
        }
    }

    fun printTestReceipt(text: String) {
        if (connection == null || endpointOut == null) return

        Thread {
            // Build the byte stream
            val data = ESC_INIT +
                    ESC_ALIGN_CENTER +
                    text.toByteArray(Charsets.US_ASCII) +
                    LF + LF + LF +
                    PAPER_CUT

            connection?.bulkTransfer(endpointOut, data, data.size, 5000)
        }.start()
    }
}