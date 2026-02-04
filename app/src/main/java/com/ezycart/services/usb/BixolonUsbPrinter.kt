package com.ezycart.services.usb


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.bxl.config.editor.BXLConfigLoader
import jpos.JposConst
import jpos.POSPrinter
import jpos.POSPrinterConst
import jpos.JposException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

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


/*
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




}*/

class BixolonUsbPrinter(private val context: Context) {

    private val logicalName = "BK3_USB"
    private val posPrinter = POSPrinter(context.applicationContext)
    private val TAG = "BixolonPDF"

    private fun configure(): Boolean {
        return try {
            val loader = BXLConfigLoader(context)
            try {
                loader.openFile()
            } catch (e: Exception) {
                loader.newFile()
            }

            loader.removeEntry(logicalName)
            loader.addEntry(
                logicalName,
                BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                BXLConfigLoader.PRODUCT_NAME_BK3_3,
                BXLConfigLoader.DEVICE_BUS_USB,
                ""
            )
            loader.saveFile()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Config error", e)
            false
        }
    }

    fun printPdf(pdfFile: File) {
        if (!pdfFile.exists()) {
            Log.e("BixolonPDF", "PDF not found")
            return
        }

        try {
            val loader = BXLConfigLoader(context)
            try { loader.openFile() } catch (e: Exception) { loader.newFile() }

            loader.removeEntry("BK3_USB")
            loader.addEntry(
                "BK3_USB",
                BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                BXLConfigLoader.PRODUCT_NAME_BK3_3,
                BXLConfigLoader.DEVICE_BUS_USB,
                ""
            )
            loader.saveFile()

            posPrinter.open("BK3_USB")
            posPrinter.claim(5000)
            posPrinter.deviceEnabled = true

            val pdfPath = "file://${pdfFile.absolutePath}"
            Log.d("BixolonPDF", "Printing: $pdfPath")

            posPrinter.printPDFFile(
                POSPrinterConst.PTR_S_RECEIPT,
                Uri.parse(pdfPath),
                576,                               // BK3-3 width
                POSPrinterConst.PTR_BM_CENTER,
                1
            )

            posPrinter.printNormal(
                POSPrinterConst.PTR_S_RECEIPT,
                "\u001b|3lF\u001b|fP"             // feed + full cut
            )

        } catch (e: JposException) {
            Log.e("BixolonPDF", "JPOS ${e.errorCode}", e)
        } finally {
            try {
                if (posPrinter.claimed) {
                    posPrinter.deviceEnabled = false
                    posPrinter.release()
                }
                posPrinter.close()
            } catch (_: Exception) {}
        }
    }

    private fun renderPdfToBitmaps(pdfFile: File): List<Bitmap> {
        val result = mutableListOf<Bitmap>()

        val fd = ParcelFileDescriptor.open(
            pdfFile,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
        val renderer = PdfRenderer(fd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)

           /* val targetWidth = 576
            val targetHeight = (targetWidth * page.height) / page.width*/

            val targetWidth = 576  // full width of printer
            val scaleFactor = 1.5 // scale content 2x
            val targetHeight = ((targetWidth * page.height / page.width) * scaleFactor).toInt()

            // ✅ MUST be ARGB_8888 for PdfRenderer
            val tempBitmap = Bitmap.createBitmap(
                targetWidth,
                targetHeight,
                Bitmap.Config.ARGB_8888
            )

            // Force WHITE background (very important)
            val canvas = Canvas(tempBitmap)
            canvas.drawColor(Color.WHITE)

            page.render(
                tempBitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_PRINT
            )

            page.close()

            // ✅ Convert to thermal-friendly bitmap
            val monoBitmap = convertToMonochrome(tempBitmap)
            tempBitmap.recycle()

            result.add(monoBitmap)
        }

        renderer.close()
        fd.close()

        return result
    }
    private fun convertToMonochrome(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height

        val out = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = src.getPixel(x, y)

                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Luminance formula
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

                // Threshold – tune if needed
                val bw = if (gray < 160) Color.BLACK else Color.WHITE

                out.setPixel(x, y, bw)
            }
        }
        return out
    }
    fun printPdfAsBitmap(context: Context, pdfFile: File) {
        val posPrinter = POSPrinter(context)

        posPrinter.open("BK3_USB")
        posPrinter.claim(5000)
        posPrinter.deviceEnabled = true

        val bitmaps = renderPdfToBitmaps(pdfFile)

        for (bitmap in bitmaps) {
            posPrinter.printBitmap(
                POSPrinterConst.PTR_S_RECEIPT,
                bitmap,
                POSPrinterConst.PTR_BM_CENTER,
                576
            )
        }
       /* posPrinter.printNormal(
            POSPrinterConst.PTR_S_RECEIPT,
            "\u001b|2C" + // double width
                    "\u001b|2H" + // double height
                    "BIG TEXT HERE\n"
        )*/
        posPrinter.printNormal(
            POSPrinterConst.PTR_S_RECEIPT,
            "\u001b|3lF\u001b|fP"
        )

        posPrinter.deviceEnabled = false
        posPrinter.release()
        posPrinter.close()
    }

    private fun toMonochrome(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = src.getPixel(x, y)

                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Luminance formula
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

                // Threshold (tune if needed)
                val bw = if (gray < 160) Color.BLACK else Color.WHITE

                bmp.setPixel(x, y, bw)
            }
        }
        return bmp
    }
}

