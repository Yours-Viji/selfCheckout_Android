package com.ezycart.services.usb

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import jpos.JposConst
import jpos.JposException
import jpos.POSPrinter
import jpos.POSPrinterConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.coroutines.resume

/**
 * Singleton Manager for Bixolon BK3-33 Printer.
 * Enhanced with detailed trace logging for debugging.
 */
class PrinterManager private constructor(private val context: Context) {

    private var posPrinter: POSPrinter = POSPrinter(context)
    private val TAG = "BixolonPrinterTrace"
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
     * Downloads a PDF and prints via DirectIO.
     */
    suspend fun printPdfFromUrl(pdfUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        Log.d(TAG, "== START printPdfFromUrl == URL: $pdfUrl")
        try {
            // 1. Download
            tempFile = File(context.cacheDir, "print_job_${System.currentTimeMillis()}.pdf")
            Log.d(TAG, "Downloading PDF to: ${tempFile.absolutePath}")

            URL(pdfUrl).openStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val bytesCopied = input.copyTo(output)
                    Log.d(TAG, "Download complete. Bytes copied: $bytesCopied")
                }
            }

            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw Exception("Downloaded file is empty or missing")
            }

            // 2. Open Printer
            openPrinter()

            // 3. DirectIO Execution
            Log.d(TAG, "Preparing DirectIO (Command 211) for PDF printing...")
            val data = IntArray(1)
            val obj = arrayOf<Any>(
                tempFile.absolutePath, // Path
                0,                     // Width (0 = auto)
                0,                     // Alignment (0 = Left)
                0                      // Page (0 = All)
            )

            try {
                Log.d(TAG, "Executing posPrinter.directIO(211, ...)")
                posPrinter.directIO(211, data, obj)
                Log.d(TAG, "directIO executed successfully")
            } catch (e: JposException) {
                Log.e(TAG, "directIO JposException: Code ${e.errorCode}, Message: ${e.message}")
                throw e
            }

            // 4. Feed and Cut
            Log.d(TAG, "Sending Feed and Cut commands...")
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n\n\n\n\n")
            posPrinter.cutPaper(100)
            Log.d(TAG, "Feed and Cut completed")

            closePrinter()
            Log.d(TAG, "== SUCCESS printPdfFromUrl ==")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "== FAILED printPdfFromUrl == Error: ${e.message}", e)
            closePrinter()
            Result.failure(e)
        } finally {
            if (tempFile?.exists() == true) {
                val deleted = tempFile.delete()
                Log.d(TAG, "Temporary file deleted: $deleted")
            }
        }
    }

    /**
     * Renders URL to Bitmap and prints.
     */
    suspend fun printUrlAsImage(url: String): Result<Unit> {
        Log.d(TAG, "== START printUrlAsImage == URL: $url")
        return try {
            val bitmap = captureUrlToBitmap(url)
            if (bitmap == null) {
                Log.e(TAG, "Bitmap generation returned NULL")
                return Result.failure(Exception("Failed to render web receipt"))
            }
            Log.d(TAG, "Bitmap captured: Width=${bitmap.width}, Height=${bitmap.height}")

            executePrint(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "== FAILED printUrlAsImage == Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun executePrint(bitmap: Bitmap): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            openPrinter()

            Log.d(TAG, "Setting asyncMode = false")
            posPrinter.asyncMode = false

            Log.d(TAG, "Executing posPrinter.printBitmap(...)")
            posPrinter.printBitmap(
                POSPrinterConst.PTR_S_RECEIPT,
                bitmap,
                POSPrinterConst.PTR_BM_ASIS,
                POSPrinterConst.PTR_BC_CENTER
            )
            Log.d(TAG, "Bitmap printed successfully")

            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n\n\n\n\n")
            posPrinter.cutPaper(100)
            Log.d(TAG, "Feed and Cut completed")

            closePrinter()
            Log.d(TAG, "== SUCCESS executePrint ==")
            Result.success(Unit)
        } catch (e: JposException) {
            Log.e(TAG, "Jpos Error in executePrint: Code ${e.errorCode}", e)
            closePrinter()
            Result.failure(e)
        }
    }

    private suspend fun captureUrlToBitmap(url: String): Bitmap? = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Switching to MainThread for WebView rendering...")
        Handler(Looper.getMainLooper()).post {
            try {
                val webView = WebView(context)
                webView.layout(0, 0, 576, 5000)

                webView.settings.javaScriptEnabled = true
                webView.settings.useWideViewPort = true
                webView.settings.loadWithOverviewMode = true

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        Log.d(TAG, "WebView page loaded. Waiting 1s for layout stabilization...")
                        Handler(Looper.getMainLooper()).postDelayed({
                            try {
                                val contentHeight = (view.contentHeight * view.scale).toInt().coerceAtLeast(100)
                                Log.d(TAG, "Rendering WebView to Bitmap. ContentHeight: $contentHeight")
                                val bitmap = Bitmap.createBitmap(576, contentHeight, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                view.draw(canvas)
                                Log.d(TAG, "Bitmap rendering complete")
                                continuation.resume(bitmap)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error during Canvas draw: ${e.message}")
                                continuation.resume(null)
                            }
                        }, 1000)
                    }

                    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                        Log.e(TAG, "WebView Error ($errorCode): $description")
                        continuation.resume(null)
                    }
                }
                Log.d(TAG, "Loading URL into WebView: $url")
                webView.loadUrl(url)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize WebView: ${e.message}")
                continuation.resume(null)
            }
        }
    }

    private fun openPrinter() {
        Log.d(TAG, "Printer State Check: Current State = ${posPrinter.state}")
        if (posPrinter.state != JposConst.JPOS_S_CLOSED) {
            Log.d(TAG, "Printer already open or in use. Skipping open().")
            return
        }

        try {
            Log.d(TAG, "Attempting posPrinter.open('$logicalName')...")
            posPrinter.open(logicalName)
            Log.d(TAG, "Open successful. Attempting claim(1500)...")
            posPrinter.claim(1500)
            Log.d(TAG, "Claim successful. Attempting deviceEnabled = true...")
            posPrinter.deviceEnabled = true
            Log.d(TAG, "Printer is now READY (Enabled).")
        } catch (e: JposException) {
            Log.e(TAG, "Failed to initialize printer: ${e.message} (Code: ${e.errorCode})")
            throw e
        }
    }

    private fun closePrinter() {
        try {
            if (posPrinter.state != JposConst.JPOS_S_CLOSED) {
                Log.d(TAG, "Closing printer resources...")
                posPrinter.deviceEnabled = false
                posPrinter.release()
                posPrinter.close()
                Log.d(TAG, "Printer closed successfully.")
            }
        } catch (e: JposException) {
            Log.e(TAG, "Error while closing printer: ${e.message}")
        }
    }
}