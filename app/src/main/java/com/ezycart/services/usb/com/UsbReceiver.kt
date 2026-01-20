package com.ezycart.services.usb.com

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                // Device plugged in!
                val permissionIntent = PendingIntent.getBroadcast(
                    context, 0, Intent("USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE
                )
                manager.requestPermission(device, permissionIntent)
            }
            "USB_PERMISSION" -> {
                synchronized(this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let { UsbSerialManager.connect(context, it) }
                    }
                }
            }
        }
    }
}