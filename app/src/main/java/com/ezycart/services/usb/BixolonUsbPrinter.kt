package com.ezycart.services.usb


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.bxl.config.editor.BXLConfigLoader
import jpos.JposConst
import jpos.POSPrinter
import jpos.POSPrinterConst
import jpos.JposException
import java.io.File

/*
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
}*/


class BixolonUsbPrinter(private val context: Context) {
    private val posPrinter = POSPrinter(context.applicationContext)
    //private val posPrinter = POSPrinter(context)
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
                "" // Leaving this empty for auto-detection of USB
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

            // BK3-3 (80mm) setup
            val width = 576
            val alignment = POSPrinterConst.PTR_BM_CENTER

            // Some versions of Bixolon SDK require the absolute path string
            // others require Uri.toString(). We'll use absolutePath for JPOS.
            posPrinter.printPDFFile(
                POSPrinterConst.PTR_S_RECEIPT,
                pdfFile.absolutePath as Uri?,
                width,
                alignment,
                0
            )

            // BK3-3 Cut: Feed 3 lines then Cut to ensure the logo isn't cut off
            // \u001b|3lF = Feed 3 lines
            // \u001b|fP = Full Cut
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\u001b|3lF" + "\u001b|fP")

        } catch (e: JposException) {
            Log.e(TAG, "Jpos Error: ${e.errorCode} - ${e.message}")
        } finally {
            try {
                if (posPrinter.claimed) {
                    posPrinter.deviceEnabled = false
                    posPrinter.release()
                }
                posPrinter.close()
            } catch (e: Exception) {
                Log.e(TAG, "Close Error: ${e.message}")
            }
        }
    }
     fun setupPrinter(context: Context) {
        val bxlConfig = BXLConfigLoader(context)
        try {
            bxlConfig.openFile()
            // Clear old entries to avoid conflicts
            val entries = bxlConfig.getEntries()
            for (entry in entries) {
                bxlConfig.removeEntry(entry.logicalName)
            }

            // Add the new entry (Change parameters based on your connection)
            bxlConfig.addEntry(
                "Printer1",                       // Logical Name used in posPrinter.open()
                BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                BXLConfigLoader.PRODUCT_NAME_BK3_3, // Replace with your model, e.g., SRP_350III
                BXLConfigLoader.DEVICE_BUS_USB,       // Use DEVICE_BUS_BLUETOOTH if using BT
                ""                                    // Address (Leave empty for USB)
            )

            bxlConfig.saveFile()
        } catch (e: Exception) {
            Log.e("Printer", "Config Error: ${e.message}")
        } finally {
        }
    }
    fun triggerSelfTest(productName: String) {
        try {
            if (!configureUsb(productName)) return

            posPrinter.open(logicalName)
            posPrinter.claim(5000)
            posPrinter.deviceEnabled = true

            // ESC/POS Command to trigger Self-Test: GS ( A pL pH n m
            // Hex: 1D 28 41 02 00 00 02
            val selfTestCommand = "\u001d\u0028\u0041\u0002\u0000\u0000\u0002"

            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, selfTestCommand)

        } catch (e: JposException) {
            Log.e(TAG, "Self-test trigger failed: ${e.errorCode}")
        } finally {
            // Standard cleanup
            if (posPrinter.claimed) {
                posPrinter.deviceEnabled = false
                posPrinter.release()
            }
            posPrinter.close()
        }
    }

    fun printBitmapToBixolon(bitmap: Bitmap, context: Context) {
        // 1. DO NOT create a new 'val posPrinter' here.
        // Use the one already defined at the top of your class.

        try {
            // 2. Open using the Logical Name
            if (posPrinter.state == JposConst.JPOS_S_CLOSED) {
                posPrinter.open("Printer1")
            }

            // 3. Try to claim
            posPrinter.claim(1500)

            // 4. If claim succeeded, enable and print
            posPrinter.deviceEnabled = true
            posPrinter.printBitmap(
                POSPrinterConst.PTR_S_RECEIPT,
                bitmap,
                posPrinter.recLineWidth,
                POSPrinterConst.PTR_BM_LEFT
            )
            posPrinter.cutPaper(100)

        } catch (e: Exception) {
            Log.e("Printer", "Critical Print Error: ${e.message}")
            // IMPORTANT: Re-throw so your ViewModel knows it failed!
            throw e
        } finally {
            // 5. SMARTER CLEANUP
            try {
                if (posPrinter.state != JposConst.JPOS_S_CLOSED) {
                    // Only disable/release if we actually own the claim
                    if (posPrinter.claimed) {
                        posPrinter.deviceEnabled = false
                        posPrinter.release()
                    }
                    posPrinter.close()
                }
            } catch (cleanupError: Exception) {
                Log.e("Printer", "Cleanup failed: ${cleanupError.message}")
            }
        }
    }
}