package com.ezycart.services.weightScaleService

import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialProber


internal object CustomSerialProber {

   /* val customProber: UsbSerialProber
        get() {
            val customTable = ProbeTable()
            customTable.addProduct(1155, 22336, CdcAcmSerialDriver::class.java)
            customTable.addProduct(6991, 37382, CdcAcmSerialDriver::class.java)
            return UsbSerialProber(customTable)
        }*/

    fun getCustomSerialProber(): UsbSerialProber? {
        val customTable = ProbeTable()
        customTable.addProduct(
            0x1234,
            0xabcd,
            FtdiSerialDriver::class.java
        )
        customTable.addProduct(1155, 22336, FtdiSerialDriver::class.java)
        customTable.addProduct(6991, 37382, FtdiSerialDriver::class.java)
        return UsbSerialProber(customTable)
    }
}