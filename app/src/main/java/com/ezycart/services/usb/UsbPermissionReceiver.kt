package com.ezycart.services.usb


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log

class UsbPermissionReceiver : BroadcastReceiver() {

    companion object {
        const val USB_PERMISSION = "com.ezycart.USB_PERMISSION"
        private const val TAG = "UsbPermissionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            USB_PERMISSION -> {
                val device = intent.getParcelableExtra<android.hardware.usb.UsbDevice>(UsbManager.EXTRA_DEVICE)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

                Log.d(TAG, "USB Permission ${if (granted) "GRANTED" else "DENIED"} for device: ${device?.deviceName}")

                // You can send a local broadcast or update a LiveData here
                val resultIntent = Intent("USB_PERMISSION_RESULT").apply {
                    putExtra("granted", granted)
                    putExtra("device_name", device?.deviceName)
                }
                context.sendBroadcast(resultIntent)
            }

            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                Log.d(TAG, "USB device attached")
                // Notify that a USB device was attached
            }

            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                Log.d(TAG, "USB device detached")
                // Notify that a USB device was detached
            }
        }
    }
}