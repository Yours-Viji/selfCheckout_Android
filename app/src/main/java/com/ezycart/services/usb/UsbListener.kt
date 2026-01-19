package com.ezycart.services.usb

import androidx.core.content.ContextCompat
import com.ezycart.data.datastore.PreferencesManager



import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class UsbEvent {
    data class Attached(val device: UsbDevice) : UsbEvent()
    data class Detached(val device: UsbDevice) : UsbEvent()
}

class UsbListener(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private val grantedPermissions = mutableSetOf<String>()

    // Exposed Flow for ViewModels to collect
    private val _usbEvents = MutableSharedFlow<UsbEvent>(extraBufferCapacity = 5)
    val usbEvents = _usbEvents.asSharedFlow()

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> handleAttachment(intent)
                UsbManager.ACTION_USB_DEVICE_DETACHED -> handleDetachment(intent)
                ACTION_USB_PERMISSION -> handlePermissionResult(intent)
            }
        }
    }

    init {
        loadPermissions()
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(ACTION_USB_PERMISSION)
        }
        ContextCompat.registerReceiver(
            context,
            usbReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun handleAttachment(intent: Intent) {
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return
        Log.v(TAG, "Device attached: ${device.deviceName}")
        checkAndRequest(device)
    }

    private fun handleDetachment(intent: Intent) {
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return
        Log.v(TAG, "Device detached: ${device.deviceName}")
        scope.launch { _usbEvents.emit(UsbEvent.Detached(device)) }
    }

    private fun handlePermissionResult(intent: Intent) {
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return
        val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

        if (granted) {
            onDeviceReady(device)
        }
    }

    private fun checkAndRequest(device: UsbDevice) {
        if (usbManager.hasPermission(device) || isKnownDevice(device)) {
            onDeviceReady(device)
        } else {
            requestPermission(device)
        }
    }

    private fun onDeviceReady(device: UsbDevice) {
        val manufacturer = device.manufacturerName?.lowercase() ?: "unknown"
        grantedPermissions.add(manufacturer)

        scope.launch {
            savePermissions()
            _usbEvents.emit(UsbEvent.Attached(device))
        }
    }

    private fun requestPermission(device: UsbDevice) {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION), flag
        )
        usbManager.requestPermission(device, pendingIntent)
    }

    private fun loadPermissions() {
       // preferencesManager.getConnectedDeviceList().forEach { grantedPermissions.add(it) }
    }

    private suspend fun savePermissions() {
      //  preferencesManager.setConnectedDeviceList(ArrayList(grantedPermissions.toList()))
    }

    private fun isKnownDevice(device: UsbDevice): Boolean {
        return grantedPermissions.contains(device.manufacturerName?.lowercase())
    }

    fun dispose() {
        try {
            context.unregisterReceiver(usbReceiver)
            scope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing UsbListener", e)
        }
    }

    companion object {
        private val TAG = UsbListener::class.java.simpleName
        private val ACTION_USB_PERMISSION = "$TAG.ACTION_USB_PERMISSION"
    }
}