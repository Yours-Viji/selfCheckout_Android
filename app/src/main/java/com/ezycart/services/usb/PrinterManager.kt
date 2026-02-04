package com.ezycart.services.usb

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import jpos.JposConst
import jpos.JposException
import jpos.POSPrinter
import jpos.POSPrinterConst
import jpos.config.JposEntry
import jpos.loader.JposServiceLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.coroutines.resume

/**
 * Singleton Manager for Bixolon BK3-33 Printer.
 * Handles rendering Web content to Bitmaps and direct PDF printing.
 */
class PrinterManager private constructor(private val context: Context) {

    private var posPrinter: POSPrinter = POSPrinter(context)
    private val TAG = "BixolonPrinter"

    // Ensure this matches the <jposEntry logicalName="..."> in your jpos.xml
    private val logicalName = "BK3-3"

    companion object {
        @Volatile
        private var INSTANCE: PrinterManager? = null

        fun getInstance(context: Context): PrinterManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrinterManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Downloads a PDF from a URL and prints it directly.
     * Note: Bixolon SDK supports PDF printing in newer versions (v2.16+).
     */
    suspend fun printPdfFromUrl(pdfUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            /*val file = File(context.cacheDir, "temp_receipt.pdf")
            URL(pdfUrl).openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }*/

            openPrinter()

            // Using the printPDF API (Added in BXL SDK v2.16)
            // Parameters: Path, Width (0 = default), Alignment, Page range (0 = all)
            posPrinter.printPDFFile(
                POSPrinterConst.PTR_S_RECEIPT,
                Uri.parse(pdfUrl),
                POSPrinterConst.PTR_PD_LEFT_TO_RIGHT, // Alignment
                0,                          // Width (0 for automatic)
                0                           // Start page (0 for all)
            )

            // Feed and Cut
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n\n\n\n\n")
            posPrinter.cutPaper(100)

            closePrinter()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "PDF Printing failed", e)
            closePrinter()
            Result.failure(e)
        }
    }

    /**
     * Renders a web URL to a bitmap and prints it.
     */
    suspend fun printUrlAsImage(url: String): Result<Unit> {
        return try {
            val bitmap = captureUrlToBitmap(url)
                ?: return Result.failure(Exception("Failed to render web receipt"))

            executePrint(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Image printing failed", e)
            Result.failure(e)
        }
    }

    private suspend fun executePrint(bitmap: Bitmap): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            openPrinter()

            posPrinter.asyncMode = false

            // Correction: Use the setter method instead of property access if the property is not visible
            // This handles the "Unresolved reference: binaryConversion"
            try {
              //  posPrinter.setBinaryConversion(0) // 0 is equivalent to PTR_BC_NONE
            } catch (e: Exception) {
                Log.w(TAG, "binaryConversion setting not supported by this driver version")
            }

            // Print the rendered bitmap
            posPrinter.printBitmap(
                POSPrinterConst.PTR_S_RECEIPT,
                bitmap,
                POSPrinterConst.PTR_BM_ASIS,
                POSPrinterConst.PTR_BC_CENTER
            )

            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n\n\n\n\n")
            posPrinter.cutPaper(100)

            closePrinter()
            Result.success(Unit)
        } catch (e: JposException) {
            Log.e(TAG, "Jpos Error: Code ${e.errorCode}", e)
            closePrinter()
            Result.failure(e)
        }
    }

    private suspend fun captureUrlToBitmap(url: String): Bitmap? = suspendCancellableCoroutine { continuation ->
        Handler(Looper.getMainLooper()).post {
            val webView = WebView(context)
            webView.layout(0, 0, 576, 5000)

            val settings = webView.settings
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            val contentHeight = (view.contentHeight * view.scale).toInt().coerceAtLeast(100)
                            val bitmap = Bitmap.createBitmap(576, contentHeight, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            view.draw(canvas)
                            continuation.resume(bitmap)
                        } catch (e: Exception) {
                            Log.e(TAG, "Bitmap capture error", e)
                            continuation.resume(null)
                        }
                    }, 800)
                }
            }
            webView.loadUrl(url)
        }
    }

    private fun openPrinter() {
        /*try {
            // Fix for "Unresolved reference 'entries'":
            // Some Bixolon JPOS versions require accessing the service manager via JposServiceLoader
            val manager = JposServiceLoader.getManager()
            val entries = manager.entries

            var found = false
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as? JposEntry
                if (entry?.logicalName == logicalName) {
                    found = true
                    break
                }
            }

            if (!found) {
                Log.e(TAG, "Logical name '$logicalName' not found in registry. Ensure jpos.xml is in assets.")
            }

            if (posPrinter.state == JposConst.JPOS_S_CLOSED) {
                posPrinter.open(logicalName)
                posPrinter.claim(1500)
                posPrinter.deviceEnabled = true
            }
        } catch (e: JposException) {
            Log.e(TAG, "Open Printer failed: ${e.message}", e)
            throw e
        }*/
    }

    private fun closePrinter() {
        try {
            if (posPrinter.state != JposConst.JPOS_S_CLOSED) {
                posPrinter.deviceEnabled = false
                posPrinter.release()
                posPrinter.close()
            }
        } catch (e: JposException) {
            Log.e(TAG, "Close Printer failed", e)
        }
    }
}