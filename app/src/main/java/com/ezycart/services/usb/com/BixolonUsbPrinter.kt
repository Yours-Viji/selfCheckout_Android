package com.ezycart.services.usb.com


import android.content.Context
import android.net.Uri
import android.util.Log
import com.bxl.config.editor.BXLConfigLoader
import jpos.POSPrinter
import jpos.POSPrinterConst
import jpos.JposException
import java.io.File

class BixolonUsbPrinter(private val context: Context) {

    private val posPrinter = POSPrinter(context)
    private val logicalName = "USB_Printer"
    private val TAG = "BixolonPrinter"

    private fun configureUsb(productName: String): Boolean {
        val configLoader = BXLConfigLoader(context)
        try {
            try {
                configLoader.openFile()
            } catch (e: Exception) {
                configLoader.newFile()
            }
            configLoader.removeEntry(logicalName)
            configLoader.addEntry(
                logicalName,
                BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                productName,
                BXLConfigLoader.DEVICE_BUS_USB,
                ""
            )
            configLoader.saveFile()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Config Error: ${e.message}")
            return false
        }
    }

    fun printReceiptPdf(productName: String, pdfFile: File) {
        if (!pdfFile.exists()) {
            Log.e(TAG, "File not found: ${pdfFile.absolutePath}")
            return
        }

        try {
            if (!configureUsb(productName)) return

            posPrinter.open(logicalName)
            posPrinter.claim(5000)
            posPrinter.deviceEnabled = true

            // Use Uri.fromFile for the SDK requirement
            val pdfUri = Uri.fromFile(pdfFile)

            // BK3-3 (80mm) = 576 dots width
            val width = 576
            val alignment = POSPrinterConst.PTR_BM_CENTER

            // Print Command
            posPrinter.printPDFFile(
                POSPrinterConst.PTR_S_RECEIPT,
                pdfUri,
                width,
                alignment,
                0 // Prints all pages
            )

            // BK3-3 Cut Command: Feed and Full Cut
            // \u001b|fP is the standard escape for cut
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\u001b|fP")

        } catch (e: JposException) {
            Log.e(TAG, "Jpos Error: ${e.errorCode}")
        } finally {
            try {
                if (posPrinter.claimed) {
                    posPrinter.deviceEnabled = false
                    posPrinter.release()
                }
                posPrinter.close()
            } catch (e: Exception) {}
        }
    }
}